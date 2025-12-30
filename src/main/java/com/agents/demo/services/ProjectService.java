package com.agents.demo.services;

import com.agents.demo.dto.project.ProjectRequestDTO;
import com.agents.demo.dto.project.ProjectResponseDTO;
import com.agents.demo.entity.Project;
import com.agents.demo.enums.ProjectStatus;
import com.agents.demo.repository.ProjectRepositiry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepositiry projectRepository;

    // lista todos os projetos no BD
    public List<ProjectResponseDTO> findAllProjects() {
        return projectRepository.findAll().stream().map(ProjectResponseDTO::new).toList();
    }

    // criação de um projeto
    public void createProject(ProjectRequestDTO request) {

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.valueOf(request.getStatus()));
        project.setDeadline(request.getDeadline().atStartOfDay());
        project.setCreatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);

    }


    public ProjectRequestDTO findById(int id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        return new ProjectRequestDTO(project);
    }

    // arquivar projeto.
    public void archivedProject(int projectId) {

        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();

            project.setStatus(ProjectStatus.ARCHIVED);
            projectRepository.save(project);
        } else {
            throw new RuntimeException("Projeto não encontrado");
        }
    }

    public ProjectRequestDTO updatesProjectNameAndDescription(int projectId, String name,  String description) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();

            project.setName(name);
            project.setDescription(description);
            return new ProjectRequestDTO(projectRepository.save(project));

        } else {
            throw new RuntimeException("Projeto não encontrado");
        }
    }

}

