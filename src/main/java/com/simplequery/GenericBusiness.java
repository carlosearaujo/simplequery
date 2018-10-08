package com.simplequery;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**@author carlos.araujo
   @since  17 de nov de 2017*/
public class GenericBusiness<T> {
	
	private Class<T> persistentClass;
	
	@SuppressWarnings("unchecked")
	public GenericBusiness() {
		this.persistentClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
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
}
