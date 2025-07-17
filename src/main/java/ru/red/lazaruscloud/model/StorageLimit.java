package ru.red.lazaruscloud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="storage_limit")
@Getter
@Setter
public class StorageLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JoinColumn(name = "users_id")
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @Column(nullable = false)
    private long quotaLimit;

    @Column(nullable = false)
    private long quotaUsedLimit;
}
