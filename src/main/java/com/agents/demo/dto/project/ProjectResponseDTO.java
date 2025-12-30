package com.agents.demo.dto.project;

import com.agents.demo.entity.Project;
import com.agents.demo.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectResponseDTO {

    private int id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;

    public ProjectResponseDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = ProjectStatus.valueOf(project.getStatus().name());
        this.deadline = project.getDeadline();
        this.createdAt = project.getCreatedAt();
    }
}
