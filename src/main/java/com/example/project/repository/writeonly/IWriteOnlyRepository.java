package com.example.project.repository.writeonly;

import java.util.Collection;
import com.example.project.entity.BaseEntity;
import com.example.project.core.NotFoundException;

public interface IWriteOnlyRepository 
{
    <T extends BaseEntity, ID> T save(T entity);
    <T extends BaseEntity, ID> Collection<T> saveAll(Collection<T> entities);
    <T extends BaseEntity, ID> void deleteById(Class<T> entityClass, ID id);
    <T extends BaseEntity, ID> void delete(T entity);
    <T extends BaseEntity, ID> void deleteAll(Collection<T> entities);
    <T extends BaseEntity, ID> void deleteAllById(Class<T> entityClass, Collection<ID> ids);
    <T extends BaseEntity, ID> void restoreById(Class<T> entityClass, ID id) throws NotFoundException;
    <T> T executeInTransaction(ITransactionOperation<T> operation) throws RuntimeException;
}
