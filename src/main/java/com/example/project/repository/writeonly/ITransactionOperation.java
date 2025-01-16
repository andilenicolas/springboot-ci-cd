package com.example.project.repository.writeonly;

public interface ITransactionOperation<T> 
{
    T execute();
}