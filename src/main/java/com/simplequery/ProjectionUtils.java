package com.simplequery;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ProjectionUtils {

	public List<String>  buildEntityProjection(Class<?> clazz) {
		return buildEntityProjection(clazz, null);
	}

	public List<String>  buildEntityProjection(Class<?> clazz, String prefix) {
		if(prefix == null){
			prefix = "";
		}
		List<String> result = new ArrayList<>();
		for(Field field : clazz.getDeclaredFields()){
			if(!Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Transient.class)){
				if(!isMappedBy(field)){
					String property = field.getName();
					String sufix = "";
					if(isObject(field)){
						sufix = getIdAttribute(field.getType());
						if(sufix == null){
							continue;
						}
						sufix = "." + sufix;
					}
					property = prefix.isEmpty() ? property.concat(sufix) : prefix.concat(".").concat(property.concat(sufix));
					result.add(property);
				}
			}
		}
		return result;
	}

	private String getIdAttribute(Class<?> clazz) {
		for(Field field : clazz.getDeclaredFields()){
			if(field.isAnnotationPresent(Id.class)){
				return field.getName();
			}
		}
		return null;
	}

	//TODO Checar se isso é realmente necessário após a troca para JPQL
	private boolean isMappedBy(Field field) {
		OneToMany oneToMany = field.getAnnotation(OneToMany.class);
		if(oneToMany != null){
			return !oneToMany.mappedBy().isEmpty();
		}
		ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
		if(manyToMany != null){
			return !manyToMany.mappedBy().isEmpty();
		}
		return false;
	}

	private boolean isObject(Field field) {
		return !(isPrimitive(field.getType()) || field.getType().isPrimitive());
	}
	
	private static Set<Class<?>> getPrimitiveTypes(){
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(Date.class);
        ret.add(String.class);
        return ret;
    }
	
	private static final Set<Class<?>> WRAPPER_TYPES = getPrimitiveTypes();

    public static boolean isPrimitive(Class<?> clazz){
        return WRAPPER_TYPES.contains(clazz);
    }

	public <T> List<String> buildPlanProjection(Class<?> clazz) {
		return buildEntityProjection(clazz, "");
	}
	
	
}
