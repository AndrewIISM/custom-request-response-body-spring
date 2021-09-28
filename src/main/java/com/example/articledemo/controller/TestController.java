package com.example.articledemo.controller;

import com.example.articledemo.core.annotation.CustomArg;
import com.example.articledemo.core.annotation.CustomRequestBody;
import com.example.articledemo.core.annotation.CustomResponseBody;
import com.example.articledemo.domain.FirstEntity;
import com.example.articledemo.domain.SmthInfo;
import com.example.articledemo.repository.FirstEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/entity")
public class TestController {

    private final FirstEntityRepository entityRepository;

    public TestController(FirstEntityRepository repository) {
        this.entityRepository = repository;
    }

    @GetMapping
    @CustomResponseBody
    public List<FirstEntity> getEntitiesWithInfo() {
        return entityRepository.findAll();
    }

    @GetMapping("/byPage")
    @CustomResponseBody
    public Page<FirstEntity> getEntitiesByPage() {
        return entityRepository.findAll(PageRequest.of(0, 1));
    }

    @GetMapping("/{id}")
    @CustomResponseBody
    public FirstEntity getEntityById(@PathVariable Long id) {
        return entityRepository.findById(id).orElseThrow();
    }

    @GetMapping("/withResponseEntity")
    @CustomResponseBody
    public ResponseEntity<?> getEntityByIdInResponseEntity() {
        return ResponseEntity.of(entityRepository.findById(1L));
    }

    @PostMapping
    public ResponseEntity<?> addEntityWithSmthInfo(@CustomRequestBody FirstEntity firstEntity, @CustomArg SmthInfo smthInfo) {
        System.out.println(firstEntity);
        System.out.println(smthInfo);
        return ResponseEntity.ok("OK");
    }

}
