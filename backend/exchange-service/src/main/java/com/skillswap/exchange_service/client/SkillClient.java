package com.skillswap.exchange_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "skill-service" est résolu via Eureka (spring.application.name du skill-service).
@FeignClient(name = "skill-service")
public interface SkillClient {

    @GetMapping("/api/competences/{id}")
    SkillDto getSkill(@PathVariable("id") Long id);
}
