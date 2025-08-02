package com.example.crud.common.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenericRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    // List<T> findAll();
    // List<T> findAll(Map<String, Object> filters, String sortBy);
    Page<T> findAll(Pageable pageable, Map<String, Object> filters);
    int update(T entity);
    int deleteById(ID id);
}