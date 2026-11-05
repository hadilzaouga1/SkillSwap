package com.skillswap.skill_service.controller;

import com.skillswap.skill_service.entity.Categorie;
import com.skillswap.skill_service.repository.CategorieRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    @GetMapping
    public List<Categorie> lister() {
        return categorieRepository.findAll(Sort.by("nom"));
    }
}
