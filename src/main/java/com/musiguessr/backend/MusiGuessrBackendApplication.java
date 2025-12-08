package com.musiguessr.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class MusiGuessrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusiGuessrBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
		return args -> {
			System.out.println("\n========================================");
			System.out.println("Testing Database Connection...");
			System.out.println("========================================");
			
			try {
				// Test connection by querying a simple value
				jdbcTemplate.queryForObject("SELECT 1", Integer.class);
				System.out.println("✓ Database connection successful!");
				
				// Check if tables exist by querying the users table
				Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM musiguessr_schema.users", Long.class);
				System.out.println("✓ Found " + userCount + " users in database");
				
			} catch (Exception e) {
				System.err.println("✗ Database connection failed: " + e.getMessage());
			}
			
			System.out.println("========================================\n");
		};
	}

}
