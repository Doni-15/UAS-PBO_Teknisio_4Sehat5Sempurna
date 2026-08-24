package com.teknisio.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:teknisio_development_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password="
})
@ActiveProfiles("development")
class DevelopmentH2ProfileTests {

  @Autowired
  private Environment environment;

  @DynamicPropertySource
  static void secureTestConfiguration(DynamicPropertyRegistry registry) {
    registry.add("app.jwt.secret", () -> UUID.randomUUID() + UUID.randomUUID().toString());
  }

  @Test
  void h2ConsoleIsLocalOnlyInDevelopment() {
    assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isTrue();
    assertThat(environment.getProperty("spring.h2.console.settings.web-allow-others", Boolean.class)).isFalse();
    assertThat(environment.getProperty("server.address")).isEqualTo("127.0.0.1");
  }
}
