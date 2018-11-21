package com.simplequery;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.annotation.PostConstruct;

/**@author carlos.araujo
   @since  17 de nov de 2017*/
@Getter
public abstract class GenericBusiness<T> {
	
	private Class<T> persistentClass;
	private String persistenceUnitName;
	
	@SuppressWarnings("unchecked")
	public GenericBusiness(	) {
		this.persistentClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	public void setPersistenceUnit(String PU) {
		this.persistenceUnitName = PU;
		this.simpleEntityRecovery.setPersistenceUnit(PU);
	}
	
	@Autowired private SimpleEntityRecovery simpleEntityRecovery;
	
	public List<T> find(Specification specification) {
		 return simpleEntityRecovery.find(getClassType(), specification);
	}
	
	public T findOne(Specification specification){
		List<T> result = simpleEntityRecovery.find(getClassType(), specification);
		if(result != null && !result.isEmpty()){
			return result.get(0);
		}
		return null;
	}
	
	public T findById(Long id, String ...projection){
		return simpleEntityRecovery.findOne(id, getClassType(), projection);
	}
	
	public Page<T> findPage(Specification specification) {
		 return simpleEntityRecovery.findPage(getClassType(), specification);
	}
	
	private Class<T> getClassType(){
		return persistentClass;
	}

	public void delete(Long[] entityIds) {
		this.simpleEntityRecovery.delete(persistentClass, entityIds);
	}
	
	public T save(T entity) {
		throw new UnsupportedOperationException("Not supported yet");
	}
	
	public String getPersistenceUnit() {
		return null;
	}
	
	@PostConstruct
	private void setPU() {
		if(getPersistenceUnit() != null) {
			setPersistenceUnit(getPersistenceUnit());
		}
	}
}
