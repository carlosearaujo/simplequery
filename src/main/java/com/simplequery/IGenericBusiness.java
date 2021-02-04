package com.simplequery;

import java.util.List;

public interface IGenericBusiness<T> {
  public List<T> find(Specification specification);
  public T findOne(Specification specification);
  public T findById(Long id, String ...projection);
  public Page<T> findPage(Specification specification);
  public <ID> void delete(ID[] entityIds);
  public String getPersistenceUnit();
  public List<T> save(List<T> entity);
  public T save(T entity);
}
