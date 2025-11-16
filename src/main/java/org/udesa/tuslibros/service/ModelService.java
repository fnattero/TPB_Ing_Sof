package org.udesa.tuslibros.service;

import org.udesa.tuslibros.model.ModelEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

public abstract class ModelService<
        M extends ModelEntity,
        R extends JpaRepository<M, Long>
        > {

    @Autowired
    protected R repository;

    @Transactional(readOnly = true)
    public Iterable<M> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public M getById(long id) {
        return getById(id, () -> {
            throw new RuntimeException(
                    "Object of class " + getModelClass() +
                    " with id " + id + " was not found."
            );
        });
    }

    @Transactional(readOnly = true)
    public M getById(long id, Supplier<? extends M> supplier) {
        return repository.findById(id).orElseGet(supplier);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @SuppressWarnings("unchecked")
    public Class<M> getModelClass() {
        return (Class<M>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
}
