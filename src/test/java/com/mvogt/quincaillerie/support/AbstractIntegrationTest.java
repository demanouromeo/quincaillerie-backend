package com.mvogt.quincaillerie.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.ObjectMapper;

import com.mvogt.quincaillerie.auth.LoginRequest;
import com.mvogt.quincaillerie.auth.LoginResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base des tests d'integration : demarre un vrai conteneur MySQL (memes migrations Flyway et
 * meme dialecte qu'en production) et expose un MockMvc branche sur la vraie chaine de filtres
 * de securite (JWT). Chaque test tourne dans une transaction annulee a la fin (rollback), donc
 * les donnees creees par un test ne fuitent pas vers les suivants.
 *
 * <p>Conteneur demarre manuellement ("singleton container", pattern documente par Testcontainers)
 * plutot que via {@code @Testcontainers}/{@code @Container} : cette extension JUnit5 arrete le
 * conteneur dans son {@code afterAll}, meme quand le champ static est herite et partage par
 * plusieurs classes de test — ce qui casse la connexion DB des classes suivantes des que la
 * premiere classe termine. Demarre une seule fois pour toute la JVM de test, arrete par le
 * resource reaper de Testcontainers a la fin du process.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void configurerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String obtenirToken(String login, String motDePasse) throws Exception {
        String corps = objectMapper.writeValueAsString(new LoginRequest(login, motDePasse));
        String reponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(reponse, LoginResponse.class).token();
    }
}
