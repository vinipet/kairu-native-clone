package com.agents.demo.entity;

import com.agents.demo.dto.project.ProjectRequestDTO;
import com.agents.demo.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    @OneToMany(mappedBy = "project")
    private List<Task> tasks;

    public Project(ProjectRequestDTO projectRequestDTO) {
    }

    // ✔ Regra de negócio válida
    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

}




