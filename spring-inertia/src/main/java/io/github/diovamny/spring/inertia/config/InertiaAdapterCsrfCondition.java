package io.github.diovamny.spring.inertia.config;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

import io.github.diovamny.spring.inertia.security.InertiaSecurityModes;

/**
 * Matches only when the adapter owns CSRF: the effective
 * {@code inertia.security.mode} resolves to {@code adapter}. In
 * {@code framework} or {@code disabled} modes the core
 * {@code InertiaCsrfFilter} must never be registered, so there is exactly one
 * CSRF owner.
 */
class InertiaAdapterCsrfCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        var rawMode = environment.getProperty("inertia.security.mode");
        InertiaSecurityModes.Mode configured;
        if (rawMode != null && !rawMode.isBlank()) {
            try {
                configured = InertiaSecurityModes.Mode.valueOf(rawMode.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition("InertiaAdapterCsrf")
                        .because("inertia.security.mode has an unsupported value"));
            }
        } else if (environment.containsProperty("inertia.csrf-enabled")) {
            var legacy = environment.getProperty("inertia.csrf-enabled", Boolean.class, Boolean.TRUE);
            configured = Boolean.TRUE.equals(legacy)
                ? InertiaSecurityModes.Mode.ADAPTER
                : InertiaSecurityModes.Mode.DISABLED;
        } else {
            configured = InertiaSecurityModes.Mode.AUTO;
        }
        var frameworkAvailable = ClassUtils.isPresent(
            "org.springframework.security.web.SecurityFilterChain", getClass().getClassLoader())
            && ClassUtils.isPresent(
                "io.github.diovamny.spring.inertia.security.InertiaSpringSecurity",
                getClass().getClassLoader());
        var effective = InertiaSecurityModes.effectiveMode(configured, frameworkAvailable);
        if (effective == InertiaSecurityModes.Mode.ADAPTER) {
            return ConditionOutcome.match(ConditionMessage.forCondition("InertiaAdapterCsrf")
                .because("effective inertia.security.mode is adapter"));
        }
        return ConditionOutcome.noMatch(ConditionMessage.forCondition("InertiaAdapterCsrf")
            .because("effective inertia.security.mode is " + effective.name().toLowerCase()));
    }
}
