package ibez89.tinkoffinvestrobot;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = {
        AbstractIntegrationTest.PostgresInitializer.class
})
public class AbstractIntegrationTest {

    private static PostgreSQLContainer postgresContainer;

    static class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            if (postgresContainer == null) {
                postgresContainer = new PostgreSQLContainer<>("postgres:14.2");
                postgresContainer.start();
            }
            System.setProperty("POSTGRES_HOST", postgresContainer.getHost());
            System.setProperty("POSTGRES_PORT", postgresContainer.getMappedPort(POSTGRESQL_PORT).toString());
            System.setProperty("POSTGRES_USERNAME", postgresContainer.getUsername());
            System.setProperty("POSTGRES_PASSWORD", postgresContainer.getPassword());
        }
    }
}
