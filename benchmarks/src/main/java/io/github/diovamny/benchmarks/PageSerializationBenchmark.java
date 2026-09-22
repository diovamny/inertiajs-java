package io.github.diovamny.benchmarks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.diovamny.inertia.core.security.SafeJsonEncoder;

/**
 * H20: page-payload serialization cost (the hot path behind every Inertia
 * response). Serializes a page-like map with Jackson and runs the
 * script-context encoder, across small/medium/large payloads.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 2, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class PageSerializationBenchmark {

    @Param({"small", "medium", "large"})
    private String size;

    private String rawJson;
    private final tools.jackson.databind.ObjectMapper mapper =
        new tools.jackson.databind.ObjectMapper();

    @Setup
    public void setup() throws Exception {
        var props = new LinkedHashMap<String, Object>();
        switch (size) {
            case "small" -> {
                for (int i = 0; i < 10; i++) {
                    props.put("prop" + i, "value-" + i);
                }
            }
            case "medium" -> {
                for (int i = 0; i < 50; i++) {
                    props.put("prop" + i, List.of("a", "b", "c", i));
                }
            }
            default -> {
                props.put("bulk", "x".repeat(1_000_000));
                for (int i = 0; i < 50; i++) {
                    props.put("prop" + i, "value-" + i);
                }
            }
        }
        var page = new LinkedHashMap<String, Object>();
        page.put("component", "Bench/Index");
        page.put("props", props);
        page.put("url", "/bench");
        page.put("version", "bench-version");
        rawJson = mapper.writeValueAsString(page);
    }

    @Benchmark
    public String serializeAndEncode() {
        return SafeJsonEncoder.encodeForScript(rawJson);
    }

    /** Baseline without the script-context encoder. */
    @Benchmark
    public int rawLength() {
        return rawJson.length();
    }

    public static void main(String[] args) throws Exception {
        // Sanity sizes without JMH (payload bytes per shot).
        var bench = new PageSerializationBenchmark();
        for (var s : new String[]{"small", "medium", "large"}) {
            bench.size = s;
            bench.setup();
            var out = bench.serializeAndEncode();
            System.out.println(s + ": raw=" + bench.rawJson.length()
                + " encoded=" + out.length());
        }
    }
}
