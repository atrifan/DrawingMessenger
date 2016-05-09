package ro.atrifan.persistence.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.transaction.annotation.Transactional;
import ro.atrifan.persistence.dao.AbstractDao;


@Transactional
public abstract class AbstractDaoImpl<T> implements AbstractDao<T> {
	protected Class<T> entityClass;

	@PersistenceContext
	protected EntityManager entityManager;

	public AbstractDaoImpl(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public AbstractDaoImpl() {

	}

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

    public void persist(T entity) {
	    entityManager.persist(entity);
	}

    public void update (T entity) {
		entityManager.merge(entity);
	}

    public void remove(T entity) {
		entityManager.remove(entity);
	}

    public T findById(long id) {
		return entityManager.find(entityClass, id);
	}

    public List<T> findAll() {
		CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<T> items = criteriaQuery.from(entityClass);
		criteriaQuery.select(items);

		List<T> result = this.entityManager.createQuery(criteriaQuery).getResultList();

		return result;
	}
}
