package net.ximatai.muyun.platform.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "test_table")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "varchar")
    public String id;

    @Column(columnDefinition = "varchar")
    public String name;

    @CreationTimestamp
    @Column(name = "t_create", updatable = false)
    public LocalDateTime tCreate;

}
