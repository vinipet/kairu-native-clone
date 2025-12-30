package com.agents.demo.controller;

import com.agents.demo.dto.project.ProjectRequestDTO;
import com.agents.demo.dto.project.ProjectResponseDTO;
import com.agents.demo.entity.Project;
import com.agents.demo.repository.ProjectRepositiry;
import com.agents.demo.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {


    @Autowired
    private ProjectService projectService;


    @PostMapping("/create")
    public void  create(@RequestBody ProjectRequestDTO request)
    {projectService.createProject(request);}

    @GetMapping("/findall")
    public List<ProjectResponseDTO> getProjects(){
        return projectService.findAllProjects();
    }

    @GetMapping("/find/{id}")
    public ProjectRequestDTO findById(@PathVariable int id) {
        return projectService.findById(id);
    }


    @PostMapping
    public ResponseEntity<?> test(@RequestBody ProjectRequestDTO request) {
        System.out.println(request.getName());
        System.out.println(request.getDescription());
        return ResponseEntity.ok(request);
    }

}
