// src/main/java/com/empresa/ecommerce_backend/repository/TagRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.model.Tag;

public interface TagRepository extends BaseRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String fragment);
}
