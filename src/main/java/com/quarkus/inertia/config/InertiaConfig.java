package com.quarkus.inertia.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "inertia")
public interface InertiaConfig {

    @WithDefault("index.html")
    String rootTemplate();

    @WithDefault("false")
    boolean ssrEnabled();

    @WithDefault("http://localhost:13714")
    String ssrUrl();

    @WithDefault("sha256")
    String versionStrategy();

    java.util.Optional<String> versionCustom();

    @WithDefault("false")
    boolean encryptHistory();

    @WithDefault("false")
    boolean camelizeProps();

    java.util.Optional<String> rootView();

    @WithDefault("false")
    boolean precognitionEnabled();

    java.util.Optional<String> ssrBundle();
}
