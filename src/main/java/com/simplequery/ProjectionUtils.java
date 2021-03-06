package com.simplequery;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
		for(Field field : getAllFields(clazz)){
			if(!Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Transient.class)){
				result.addAll(addFieldProjection(field, prefix));
			}
		}
		return result;
	}
	
	private List<String> addFieldProjection(Field field, String prefix) {
		List<String> result = new ArrayList<>();
		String formattedPrefix = (prefix.isEmpty() ? prefix : prefix.concat(".")) + field.getName();
		if(field.isAnnotationPresent(Embedded.class)) {
			return buildEntityProjection(field.getType(), formattedPrefix);
		}
		else {
			String sufix = getSufix(field);
			if(sufix != null){
				result.add(formattedPrefix.concat(sufix));
			}
		}
		return result;
	}

	private String getSufix(Field field) {
		String sufix = isObject(field) ? getIdAttribute(field.getType()) : "";
		return sufix == null ? null : sufix.isEmpty() ? sufix : "." + sufix;
	}

	public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

	private String getIdAttribute(Class<?> clazz) {
		for(Field field : clazz.getDeclaredFields()){
			if(field.isAnnotationPresent(Id.class)){
				return field.getName();
			}
		}
		return null;
	}

	//TODO Checar se isso � realmente necessário após a troca para JPQL
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
		return !(isPrimitive(field.getType()) || field.getType().isPrimitive()) && !field.getType().isEnum();
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
        ret.add(LocalDate.class);
        ret.add(LocalDateTime.class);
        ret.add(String.class);
        ret.add(BigInteger.class);
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
