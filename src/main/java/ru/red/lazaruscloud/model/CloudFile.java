package ru.red.lazaruscloud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users_files")
@Getter
@Setter
public class CloudFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "file_name")
    private String fileName;
    @Setter
    @ManyToOne
    @JoinColumn(name = "file_owner_id")
    private User fileOwner;

    @Column(name = "file_size")
    private long fileSize;

    @Column(nullable = false)
    private boolean isShared;

    @Column(name = "server_path", nullable = false)
    private String path;
}
