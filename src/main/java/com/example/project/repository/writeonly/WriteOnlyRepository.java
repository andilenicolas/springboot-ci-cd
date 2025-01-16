package com.example.project.repository.writeonly;

import java.time.LocalDateTime;
import java.util.Collection;
import jakarta.persistence.EntityManager;
import com.example.project.entity.BaseEntity;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import com.example.project.core.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WriteOnlyRepository implements IWriteOnlyRepository 
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T extends BaseEntity, ID> T save(T entity) {
        if (entity.getId() == null) {
        	entity.setCreatedAt(LocalDateTime.now());
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public <T extends BaseEntity, ID> Collection<T> saveAll(Collection<T> entities) {
        return entities.stream()
            .map(this::save)
            .toList();
    }

    @Override
    public <T extends BaseEntity, ID> void deleteById(Class<T> entityClass, ID id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entity.delete();
            entityManager.merge(entity);
        }
    }

    @Override
    public <T extends BaseEntity, ID> void delete(T entity) {
        entity.delete();
        entityManager.merge(entity);
    }

    @Override
    public <T extends BaseEntity, ID> void deleteAll(Collection<T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public <T extends BaseEntity, ID> void deleteAllById(Class<T> entityClass, Collection<ID> ids) {
        ids.forEach(id -> deleteById(entityClass, id));
    }
    
    @Override
    public <T extends BaseEntity, ID> void restoreById(Class<T> entityClass, ID id) throws NotFoundException  
    {
    	T entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new NotFoundException(entityClass.getSimpleName() + " entity not found with id: " + id);
        }
        
        entity.restore();
        save(entity);
    }
    
    @Override
    public <T> T executeInTransaction(ITransactionOperation<T> operation) throws RuntimeException
    {
        try {
            return operation.execute();
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }
}


