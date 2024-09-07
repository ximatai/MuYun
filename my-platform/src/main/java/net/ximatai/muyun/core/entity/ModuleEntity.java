package net.ximatai.muyun.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "app_module")
public class ModuleEntity {

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
