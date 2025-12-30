package com.agents.demo.repository;


import com.agents.demo.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepositiry extends JpaRepository<Project, Integer> {
}
