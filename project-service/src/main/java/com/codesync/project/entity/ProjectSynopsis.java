package com.codesync.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "project_synopsis")
public class ProjectSynopsis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ElementCollection
    @CollectionTable(name = "project_actors", joinColumns = @JoinColumn(name = "synopsis_id"))
    @Column(name = "actor")
    private List<String> actors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_use_cases", joinColumns = @JoinColumn(name = "synopsis_id"))
    @Column(name = "use_case")
    private List<String> useCases = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_requirements", joinColumns = @JoinColumn(name = "synopsis_id"))
    @Column(name = "requirement")
    private List<String> requirements = new ArrayList<>();
}
