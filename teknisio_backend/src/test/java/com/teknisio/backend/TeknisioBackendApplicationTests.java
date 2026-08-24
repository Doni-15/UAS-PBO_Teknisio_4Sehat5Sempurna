package com.teknisio.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:teknisio_default_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
	"spring.datasource.username=sa",
	"spring.datasource.password="
})
@AutoConfigureMockMvc
class TeknisioBackendApplicationTests {

	@Autowired
	private Environment environment;

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void secureTestConfiguration(DynamicPropertyRegistry registry) {
		registry.add("app.jwt.secret", () -> UUID.randomUUID() + UUID.randomUUID().toString());
	}

	@Test
	void contextLoads() {
	}

	@Test
	void h2ConsoleIsDisabledAndNotPublicByDefault() throws Exception {
		assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isFalse();
		assertThat(environment.getProperty("spring.h2.console.settings.web-allow-others", Boolean.class)).isFalse();

		mockMvc.perform(get("/h2-console/"))
			.andExpect(status().isUnauthorized());
	}

}
