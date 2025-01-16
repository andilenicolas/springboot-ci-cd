package com.example.project.repository.readonly;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import com.example.project.entity.BaseEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(readOnly = true)
public class ReadOnlyRepository implements IReadOnlyRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T extends BaseEntity, ID> Optional<T> findById(Class<T> entityClass, ID id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.detach(entity);
            // Only return non-deleted entities
            if (!entity.isDeleted()) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends BaseEntity, ID> List<T> findAll(Class<T> entityClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        // Add condition for non-deleted entities
        Predicate notDeleted = cb.equal(root.get("deleted"), false);
        query.select(root).where(notDeleted);
        
        List<T> results = entityManager.createQuery(query).getResultList();
        results.forEach(entityManager::detach);
        return results;
    }

    @Override
    public <T extends BaseEntity, ID> Page<T> findAll(Class<T> entityClass, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Count query for non-deleted entities
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        Predicate notDeletedCount = cb.equal(countRoot.get("deleted"), false);
        countQuery.select(cb.count(countRoot)).where(notDeletedCount);
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Select query
        CriteriaQuery<T> selectQuery = cb.createQuery(entityClass);
        Root<T> selectRoot = selectQuery.from(entityClass);
        Predicate notDeletedSelect = cb.equal(selectRoot.get("deleted"), false);
        selectQuery.select(selectRoot).where(notDeletedSelect);
        
        // Apply sorting if present in pageable
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    selectQuery.orderBy(cb.asc(selectRoot.get(order.getProperty())));
                } else {
                    selectQuery.orderBy(cb.desc(selectRoot.get(order.getProperty())));
                }
            });
        }
        
        List<T> results = entityManager.createQuery(selectQuery)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();
        
        results.forEach(entityManager::detach);
        
        return new PageImpl<>(results, pageable, total);
    }
    
    
    /**
     *  Build the dynamic query using a lambda, example
     *  List<User> users = repository.findAllByCondition(
     *  	User.class,
     *  	(cb, root) -> cb.and(
     *  		cb.equal(root.get(User_.id), 123),
     *  		cb.equal(root.get("deleted"), false)
     *  	)
     *  );
     * **/
    @Override
    public <T extends BaseEntity, ID> List<T> findAllByCondition(
        Class<T> entityClass,
        BiFunction<CriteriaBuilder, Root<T>, Predicate> conditionBuilder) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Apply the condition builder to create the predicate
        Predicate notDeleted = cb.equal(root.get("deleted"), false);
        cb.and(notDeleted);
        
        Predicate condition = conditionBuilder.apply(cb, root);
        query.select(root).where(condition);

        // Execute the query
        List<T> results = entityManager.createQuery(query).getResultList();

        // Detach results to prevent unintended changes
        results.forEach(entityManager::detach);

        return results;
    }


    @Override
    public <T extends BaseEntity, ID> List<T> findAllById(Class<T> entityClass, Iterable<ID> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        Predicate inIds = root.get("id").in(ids);
        Predicate notDeleted = cb.equal(root.get("deleted"), false);
        
        query.select(root).where(cb.and(inIds, notDeleted));
        
        List<T> results = entityManager.createQuery(query).getResultList();
        results.forEach(entityManager::detach);
        return results;
    }

    @Override
    public <T extends BaseEntity, ID> boolean existsById(Class<T> entityClass, ID id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        
        Predicate idEquals = cb.equal(root.get("id"), id);
        Predicate notDeleted = cb.equal(root.get("deleted"), false);
        
        query.select(cb.count(root)).where(cb.and(idEquals, notDeleted));
        
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public <T extends BaseEntity, ID> long count(Class<T> entityClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        
        // Only count non-deleted entities
        Predicate notDeleted = cb.equal(root.get("deleted"), false);
        query.select(cb.count(root)).where(notDeleted);
        
        return entityManager.createQuery(query).getSingleResult();
    }
}