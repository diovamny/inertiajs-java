package com.example.pingcrm;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class PingCrmSvelteReactiveApplication {

    public static void main(String... args) {
        Quarkus.run(args);
    }
}
