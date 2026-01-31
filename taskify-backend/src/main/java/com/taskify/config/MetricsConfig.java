package com.taskify.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom metrics configuration for business monitoring.
 */
@Configuration
public class MetricsConfig {

    /**
     * Counter for task creation
     */
    @Bean
    public Counter taskCreatedCounter(MeterRegistry registry) {
        return Counter.builder("taskify.tasks.created")
                .description("Total number of tasks created")
                .tag("type", "task")
                .register(registry);
    }

    /**
     * Counter for task completion
     */
    @Bean
    public Counter taskCompletedCounter(MeterRegistry registry) {
        return Counter.builder("taskify.tasks.completed")
                .description("Total number of tasks completed")
                .tag("type", "task")
                .register(registry);
    }

    /**
     * Counter for appointment creation
     */
    @Bean
    public Counter appointmentCreatedCounter(MeterRegistry registry) {
        return Counter.builder("taskify.appointments.created")
                .description("Total number of appointments created")
                .tag("type", "appointment")
                .register(registry);
    }

    /**
     * Counter for user registrations
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("taskify.users.registered")
                .description("Total number of user registrations")
                .tag("type", "user")
                .register(registry);
    }

    /**
     * Counter for login attempts
     */
    @Bean
    public Counter loginAttemptCounter(MeterRegistry registry) {
        return Counter.builder("taskify.users.login.attempts")
                .description("Total number of login attempts")
                .tag("type", "auth")
                .register(registry);
    }

    /**
     * Counter for successful logins
     */
    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("taskify.users.login.success")
                .description("Total number of successful logins")
                .tag("type", "auth")
                .register(registry);
    }
}
