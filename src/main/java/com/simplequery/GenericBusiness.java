package com.simplequery;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**@author carlos.araujo
   @since  17 de nov de 2017*/
public class GenericBusiness<T> {
	
	private Class<T> persistentClass;
	
	@SuppressWarnings("unchecked")
	public GenericBusiness() {
		this.persistentClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	@Autowired private SimpleEntityRecovery simpleEntityRecovery;
	
	public List<T> find(Specification specification, String ...agregation) {
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
	
	private Class<T> getClassType(){
		return persistentClass;
	}
}
