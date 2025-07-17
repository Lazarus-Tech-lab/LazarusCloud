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
    private String name;

    @Column(name = "server_filename", nullable = false)
    private String serverName;
    @Setter
    @ManyToOne
    @JoinColumn(name = "file_owner_id")
    private User fileOwner;

    @Column(nullable = false, name = "isFolder")
    private Boolean isFolder;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "file_size")
    private long fileSize;

    @Column(nullable = false)
    @Getter
    @Setter
    private boolean isShared;

    @Column(name = "server_path", nullable = false)
    private String path;
}
