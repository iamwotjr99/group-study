package com.jaeseok.groupStudy.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class IntegrationTestSupport {
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    // @DynamicPropertySource: 테스트 실행 전에 동적으로 application.yml의 프로퍼티를 재설정
    // Testcontainers가 띄운 MySQL 컨테이너의 실제 JDBC URL, username, password를
    // Spring Boot의 datasource 설정에 주입
    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        if (!mySQLContainer.isRunning()) {
            mySQLContainer.start();
        }

        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }
}
