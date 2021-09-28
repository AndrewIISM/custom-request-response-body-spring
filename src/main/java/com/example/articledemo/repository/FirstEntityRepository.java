package com.example.articledemo.repository;

import com.example.articledemo.domain.FirstEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirstEntityRepository extends JpaRepository<FirstEntity, Long> {
}
