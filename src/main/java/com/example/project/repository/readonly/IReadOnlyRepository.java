package com.example.project.repository.readonly;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.data.domain.Page;
import com.example.project.entity.BaseEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;

public interface IReadOnlyRepository 
{
    <T extends BaseEntity, ID> Optional<T> findById(Class<T> entityClass, ID id);
    
    <T extends BaseEntity, ID> List<T> findAll(Class<T> entityClass);
    
    <T extends BaseEntity, ID> Page<T> findAll(Class<T> entityClass, Pageable pageable);
    
    /**
     *  Build the dynamic query using a lambda, example
     *  
     *  List<User> users = repository.findAllByCondition(
     *  	User.class,
     *  	(cb, root) -> cb.and(
     *  		cb.equal(root.get(User_.id), 123),
     *  		cb.equal(root.get("deleted"), false)
     *  	)
     *  );
     * **/
    <T extends BaseEntity, ID> List<T> findAllByCondition(Class<T> entityClass, BiFunction<CriteriaBuilder, Root<T>, Predicate> conditionBuilder);
    
    <T extends BaseEntity, ID> List<T> findAllById(Class<T> entityClass, Iterable<ID> ids);
    
    <T extends BaseEntity, ID> boolean existsById(Class<T> entityClass, ID id);
    
    <T extends BaseEntity, ID> long count(Class<T> entityClass);
}