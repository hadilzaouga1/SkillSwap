package com.SkillSwap_api.controller;

import com.SkillSwap_api.entity.Competence;
import com.SkillSwap_api.repository.CompetenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/competences")
public class CompetenceController {

    @Autowired
    private CompetenceRepository competenceRepository;

    @GetMapping
    public List<Competence> getAllCompetences() {
        return competenceRepository.findAll();
    }
}