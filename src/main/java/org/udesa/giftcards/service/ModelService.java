package org.udesa.giftcards.service;

import org.udesa.giftcards.entities.ModelEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

public abstract class ModelService<
        M extends ModelEntity,
        R extends JpaRepository<M, Long>
        > {

    @Autowired
    protected R repository;

    @Transactional(readOnly = true)
    public List<M> findAll() {
        return StreamSupport.stream( repository.findAll().spliterator(), false ).toList();
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
    public M getById( long id, Supplier<? extends M> supplier ) {
        return repository.findById( id ).orElseGet( supplier );
    }

    public Class<M> getModelClass() {
        return (Class<M>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public M save( M model ) {
        return repository.save( model );
    }

    public void delete( long id ) {
        repository.deleteById( id );
    }

    public void delete( M model ) {
        repository.delete( model );
    }

    public abstract void deleteItemWithPrefix(String prefix);

}
