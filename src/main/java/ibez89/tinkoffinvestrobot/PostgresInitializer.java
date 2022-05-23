package ibez89.tinkoffinvestrobot;

import com.sun.istack.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static PostgreSQLContainer postgresContainer;

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
        if (PostgresInitializer.postgresContainer == null) {
            PostgresInitializer.postgresContainer = new PostgreSQLContainer<>("postgres:14.2");
            PostgresInitializer.postgresContainer.start();
        }
        System.setProperty("POSTGRES_HOST", PostgresInitializer.postgresContainer.getHost());
        System.setProperty("POSTGRES_PORT", PostgresInitializer.postgresContainer.getMappedPort(POSTGRESQL_PORT).toString());
        System.setProperty("POSTGRES_USERNAME", PostgresInitializer.postgresContainer.getUsername());
        System.setProperty("POSTGRES_PASSWORD", PostgresInitializer.postgresContainer.getPassword());
    }
}
