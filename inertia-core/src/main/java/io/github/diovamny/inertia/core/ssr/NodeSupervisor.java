package io.github.diovamny.inertia.core.ssr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Supervises the Node.js SSR sidecar process: starts it, restarts unexpected
 * failures with exponential backoff (bounded), and stops it on shutdown
 * (Rails Puma-plugin inspiration).
 *
 * <p>Exit code {@code 0} is treated as an intentional stop (no restart);
 * any other exit triggers a bounded restart loop. All blocking waits run on
 * a dedicated daemon thread; {@link #stop()} terminates the process and the
 * watcher.</p>
 *
 * <p>Thread-safe.</p>
 */
public class NodeSupervisor {

    /**
     * Supervisor configuration.
     *
     * @param command      the executable (e.g. {@code node})
     * @param entry        the sidecar entry file (e.g. {@code dist-ssr/ssr.mjs})
     * @param workDir      working directory, or blank for the JVM directory
     * @param maxRestarts  restart attempts after the initial start
     * @param backoffBase  base backoff between restarts (doubles each time)
     * @param backoffUnit  unit of the base backoff
     */
    public record Config(String command, String entry, String workDir, int maxRestarts,
                         long backoffBase, TimeUnit backoffUnit) {
    }

    private final Config config;
    private final AtomicInteger attempts = new AtomicInteger();
    private volatile Process process;
    private volatile Thread watcher;
    private volatile boolean stopped;

    public NodeSupervisor(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    /**
     * Start the sidecar (and its watcher) unless already running.
     */
    public synchronized void start() {
        stopped = false;
        if (watcher != null && watcher.isAlive()) {
            return;
        }
        watcher = new Thread(this::supervise, "inertia-ssr-supervisor");
        watcher.setDaemon(true);
        watcher.start();
    }

    /**
     * Stop the sidecar and the watcher.
     */
    public synchronized void stop() {
        stopped = true;
        var active = process;
        if (active != null) {
            active.destroy();
        }
        var watching = watcher;
        if (watching != null) {
            watching.interrupt();
        }
    }

    /**
     * Whether the sidecar process is currently alive.
     */
    public boolean isRunning() {
        var active = process;
        return active != null && active.isAlive();
    }

    /**
     * How many times the process was launched (initial start + restarts).
     */
    public int attempts() {
        return attempts.get();
    }

    /**
     * Exponential backoff before attempt {@code restart} (1-based):
     * {@code base * 2^(restart-1)}.
     */
    public static long backoffMillis(long base, TimeUnit unit, int restart) {
        return unit.toMillis(base) * (1L << Math.min(restart - 1, 20));
    }

    List<String> commandLine() {
        var command = new ArrayList<String>();
        command.add(config.command());
        command.add(config.entry());
        return command;
    }

    private void supervise() {
        var restarts = 0;
        while (!stopped) {
            try {
                var builder = new ProcessBuilder(commandLine());
                if (config.workDir() != null && !config.workDir().isBlank()) {
                    builder.directory(new File(config.workDir()));
                }
                builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                builder.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = builder.start();
                attempts.incrementAndGet();
                var code = process.waitFor();
                process = null;
                if (stopped || code == 0 || restarts >= config.maxRestarts()) {
                    return;
                }
                restarts++;
                Thread.sleep(backoffMillis(config.backoffBase(), config.backoffUnit(), restarts));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                if (stopped || restarts >= config.maxRestarts()) {
                    return;
                }
                restarts++;
                try {
                    Thread.sleep(backoffMillis(config.backoffBase(), config.backoffUnit(), restarts));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
