package it.dmi.data.api.repositories;

import it.dmi.data.entities.AEntity;
import it.dmi.structure.exceptions.impl.persistence.DatabaseConnectionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public abstract class ARepository<T extends AEntity> {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    private final Class<T> entityClass;

    protected ARepository() {
        this.entityClass = resolveGenericType();
    }

    protected @NotNull EntityManager getEm() {
        if (this.em == null) throw new IllegalStateException("EntityManager is not initialized.");
        return this.em;
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveGenericType() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        while (genericSuperclass instanceof Class) {
            genericSuperclass = ((Class<?>) genericSuperclass).getGenericSuperclass();
        }
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return (Class<T>) parameterizedType.getActualTypeArguments()[0];
        }
        throw new IllegalStateException("Cannot resolve generic type for " + getClass().getName());
    }

    public @Nullable T save(T entity) {
        return executeTransaction(() -> {
            getEm().persist(entity);
            getEm().flush();
            return entity;
        }, "saving");
    }

/*
    public @Nullable T save(T entity) {
        try {
            getEm().persist(entity);
            getEm().flush();
            return entity;
        } catch (Exception e) {
            log.error("Error saving {}: {}", entity.getClass().getSimpleName(), e.getMessage());
            log.debug("Error saving {}: ", entity.getClass().getSimpleName(), e);
            return null;
        }
    }
*/

/*
    public @Nullable T findByID(Long id) {
        if (id != null) {
            return getEm().find(entityClass, id);
        } return null;
    }
*/

    public @Nullable T findByID(Long id) {
        if (id == null) return null;
        return executeTransaction(() -> getEm().find(entityClass, id), "retrieving");
    }

/*
    public List<T> findAll() throws DatabaseConnectionException {
        try {
            CriteriaBuilder builder = getEm().getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(entityClass);
            Root<T> rootEntry = query.from(entityClass);
            query.select(rootEntry);
            return getEm().createQuery(query).getResultList();
        } catch (Exception e) {
            log.error("Encountered an error while communicating with database. {}", e.getMessage());
            log.debug("Encountered an error while communicating with database. ", e);
            throw new DatabaseConnectionException(e);
        }
    }
*/
    public List<T> findAll() throws DatabaseConnectionException {
        return executeTransaction(() -> {
            try {
                CriteriaBuilder builder = getEm().getCriteriaBuilder();
                CriteriaQuery<T> query = builder.createQuery(entityClass);
                Root<T> rootEntry = query.from(entityClass);
                query.select(rootEntry);
                return getEm().createQuery(query).getResultList();
            } catch (Exception e) {
                throw new DatabaseConnectionException(e);
            }
        }, "finding all entities");
    }

/*
    public @Nullable T update(T entity) {
        try {
            return getEm().merge(entity);
        } catch (Exception e) {
            log.error("Error updating {}: {}", entityClass.getSimpleName(), e.getMessage());
            log.debug("Error updating {}: ", entityClass.getSimpleName(), e);
            return null;
        }
    }
*/
    public @Nullable T update(T entity) {
        return executeTransaction(() -> getEm().merge(entity), "updating");
    }

/*
    public boolean delete(Long id) {
        T entity = findByID(id);
        if (entity != null) {
            getEm().remove(entity);
            return true;
        }
        return false;
    }
*/

    public boolean delete(Long id) {
        T entity = findByID(id);
        if (entity != null) {
            executeTransaction(() -> {
                getEm().remove(entity);
                return null;
            }, "deleting");
            return true;
        }
        return false;
    }

    private <R> @Nullable R executeTransaction(TransactionAction<R> action, String operation) {
        try {
            return action.execute();
        } catch (Exception e) {
            logError(operation, entityClass.getSimpleName(), e);
            return null;
        }
    }

    private void logError(String operation, String clasz, @NotNull Exception e) {
        log.error("Error {} {}: {}", operation, clasz, e.getMessage());
        log.debug("Error {}: ", operation, e);
    }

    @FunctionalInterface
    private interface TransactionAction<R> {
        R execute() throws Exception;
    }
}
