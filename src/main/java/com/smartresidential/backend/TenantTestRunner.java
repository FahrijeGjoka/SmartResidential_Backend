package com.smartresidential.backend;

import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantTestRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        try {
            System.out.println("=== TESTING TENANT SWITCH ===");

            TenantContext.set(1L, "test-tenant", "test-tenant", null, null);

            userRepository.findAll().forEach(user ->
                    System.out.println(user.getEmail())
            );

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TenantContext.clear();
        }
    }
}