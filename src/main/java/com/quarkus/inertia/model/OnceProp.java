package com.quarkus.inertia.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record OnceProp(String prop, Long expiresAt) {}