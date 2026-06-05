package com.daniel.registry.reputation.infrastructure.entity;

import jakarta.persistence.*;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "user_reputation",
        indexes = @Index(name = "idx_user_reputation_userid", columnList = "userId", unique = true)
)
public class UserReputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private long xp;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private String title;

    protected UserReputation() {
        // JPA
    }

    public UserReputation(String userId) {
        this.userId = userId;
        this.xp = 0;
        this.level = 1;
        this.title = "Inovador Iniciante";
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public long getXp() { return xp; }
    public int getLevel() { return level; }
    public String getTitle() { return title; }

    public void apply(long xp, int level, String title) {
        this.xp = xp;
        this.level = level;
        this.title = title;
    }
}
