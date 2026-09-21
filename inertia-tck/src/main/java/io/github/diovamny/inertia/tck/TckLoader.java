package io.github.diovamny.inertia.tck;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Loads every {@code protocol-v3/*.yaml} spec from the classpath, in
 * filename order. Each file holds a list of {@link TckCase} maps.
 */
public final class TckLoader {

    private TckLoader() {
    }

    public static List<TckCase> loadAll() {
        var index = readIndex();
        var cases = new ArrayList<TckCase>();
        var yaml = new Yaml();
        for (var file : index) {
            var resource = "/protocol-v3/" + file;
            try (var in = TckLoader.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("TCK spec missing from classpath: " + resource);
                }
                for (var doc : yaml.loadAll(in)) {
                    if (doc instanceof List<?> list) {
                        for (var item : list) {
                            if (item instanceof Map<?, ?> raw) {
                                cases.add(new TckCase(cast(raw)));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        var ids = cases.stream().map(TckCase::id).distinct().toList();
        if (ids.size() != cases.size()) {
            throw new IllegalStateException("Duplicate TCK case ids detected");
        }
        return Collections.unmodifiableList(cases);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?, ?> raw) {
        return (Map<String, Object>) raw;
    }

    private static List<String> readIndex() {
        try (var in = TckLoader.class.getResourceAsStream("/protocol-v3/index.txt")) {
            if (in == null) {
                throw new IllegalStateException("TCK spec index missing: /protocol-v3/index.txt");
            }
            var content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return java.util.Arrays.stream(content.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
