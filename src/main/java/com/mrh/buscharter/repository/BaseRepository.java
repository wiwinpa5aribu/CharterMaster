package com.mrh.buscharter.repository;

import com.mrh.buscharter.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base Repository dengan generic CRUD operations.
 * Semua repository spesifik akan extend class ini.
 * 
 * @param <T> Tipe entity
 */
public abstract class BaseRepository<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Simpan entity baru atau update entity yang sudah ada.
     */
    public T save(T entity) {
        return executeInTransaction(em -> {
            if (getEntityId(entity) == null) {
                em.persist(entity);
                return entity;
            } else {
                return em.merge(entity);
            }
        });
    }

    /**
     * Cari entity berdasarkan ID.
     */
    public Optional<T> findById(Long id) {
        return executeWithEntityManager(em -> {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        });
    }

    /**
     * Ambil semua entity.
     */
    public List<T> findAll() {
        return executeWithEntityManager(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            return em.createQuery(cq).getResultList();
        });
    }

    /**
     * Ambil semua entity dengan filter tenant_id untuk multi-tenant isolation.
     */
    public List<T> findAllByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.tenant.id = :tenantId";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Hapus entity berdasarkan ID.
     */
    public void deleteById(Long id) {
        executeInTransactionVoid(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
        });
    }

    /**
     * Hapus entity.
     */
    public void delete(T entity) {
        executeInTransactionVoid(em -> {
            T managedEntity = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managedEntity);
        });
    }

    /**
     * Hitung jumlah entity.
     */
    public long count() {
        return executeWithEntityManager(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            cq.select(cb.count(cq.from(entityClass)));
            return em.createQuery(cq).getSingleResult();
        });
    }

    /**
     * Cek apakah entity dengan ID tertentu ada.
     */
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    /**
     * Refresh entity dari database.
     */
    public T refresh(T entity) {
        return executeWithEntityManager(em -> {
            T managedEntity = em.contains(entity) ? entity : em.merge(entity);
            em.refresh(managedEntity);
            return managedEntity;
        });
    }

    // ==================== Helper Methods ====================

    /**
     * Execute operation dengan EntityManager (read-only).
     */
    protected <R> R executeWithEntityManager(Function<EntityManager, R> operation) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return operation.apply(em);
        } finally {
            em.close();
        }
    }

    /**
     * Execute operation dalam transaction (write).
     */
    protected <R> R executeInTransaction(Function<EntityManager, R> operation) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = operation.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error dalam transaksi: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menjalankan operasi database", e);
        } finally {
            em.close();
        }
    }

    /**
     * Execute operation dalam transaction tanpa return value.
     */
    protected void executeInTransactionVoid(Consumer<EntityManager> operation) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            operation.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error dalam transaksi: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menjalankan operasi database", e);
        } finally {
            em.close();
        }
    }

    /**
     * Abstract method untuk mendapatkan ID dari entity.
     * Harus diimplementasikan oleh subclass.
     */
    protected abstract Long getEntityId(T entity);
}
