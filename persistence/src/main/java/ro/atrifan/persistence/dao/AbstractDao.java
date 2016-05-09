package ro.atrifan.persistence.dao;

import java.util.List;

public interface AbstractDao<T> {

	void persist(T entity);
	void remove(T entity);
	void update(final T entity);
	List<T> findAll();
	T findById(long id);
}
