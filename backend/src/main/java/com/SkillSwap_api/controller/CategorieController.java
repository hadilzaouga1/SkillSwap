package com.SkillSwap_api.controller;

import com.SkillSwap_api.entity.Categorie;
import com.SkillSwap_api.repository.CategorieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Liste des catégories .
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Catégories")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    @Operation(summary = "Lister les catégories (triées par nom)")
    @GetMapping
    public List<Categorie> lister() {
        return categorieRepository.findAll(Sort.by("nom"));
    }
}
