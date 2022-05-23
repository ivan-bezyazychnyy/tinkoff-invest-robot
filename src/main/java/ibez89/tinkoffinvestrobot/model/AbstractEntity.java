package ibez89.tinkoffinvestrobot.model;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected UUID id;

    @Column
    protected Instant createdAt;

    @Column
    protected Instant updatedAt;

    public AbstractEntity() {
    }

    @PrePersist
    protected void prePersist() {
        updatedAt = createdAt = Instant.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
