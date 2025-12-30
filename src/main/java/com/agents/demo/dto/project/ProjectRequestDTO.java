package com.agents.demo.dto.project;

import com.agents.demo.entity.Project;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectRequestDTO {

    @NotBlank
    private String name;

    private String description;
    private String status;
    private LocalDate deadline;

    public ProjectRequestDTO(Project project) {
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus().name(); // enum → String
        this.deadline = LocalDate.from(project.getDeadline());
    }

}
