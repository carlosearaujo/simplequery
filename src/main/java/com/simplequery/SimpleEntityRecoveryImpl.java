package com.simplequery;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author carlos.araujo
   @since  6 de set de 2017 */
@Component
public class SimpleEntityRecoveryImpl implements SimpleEntityRecovery {
	
	private static final  String ROOT = "this";
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public SimpleEntityRecoveryImpl(){}
	
	public SimpleEntityRecoveryImpl(EntityManager entityManager){
		this.entityManager = entityManager;
	}
	
	ProjectionUtils projectionUtils = new ProjectionUtils();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> find(Class<T> clazz, Specification specification) {
		AliasToBeanMultiLevelNestedResultTransformer transformer = new AliasToBeanMultiLevelNestedResultTransformer(clazz);
		specification.setProjection(buildProjection(clazz, specification));
		StringBuilder sql = buildQuery(clazz, specification);
		List<T> result = new ArrayList<T>();
		TypedQuery<List> query = entityManager.createQuery(sql.toString(), List.class);
		applySelectionValues(query, specification.getSelection());
		applyPagination(specification, query);
		query.getResultList().forEach(resultItem -> {
			result.add((T) transformer.transformTuple(resultItem.toArray(), specification.getProjection()));
		});
		return result;
	}
	
	private void applySelectionValues(TypedQuery<?> query, List<Selection> selections) {
		int i = 1;
		for(Selection selection : selections){
			Object value = selection.isArrayValue() ? Arrays.asList((Object[])selection.getValue()) : selection.getValue();
			query.setParameter("param" + i, value instanceof Integer ? Long.valueOf((Integer)value) : value);
			i++;
		}
	}

	/*public <T> Page<T> findPage(Class<T> clazz, Specification specification){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) ");
		applyFilters(clazz, specification, sql);
		TypedQuery<Long> query = entityManager.createQuery(sql.toString(), Long.class);
		applySelectionValues(query, specification.getSelection());
		return new Page<T>(find(clazz, specification), query.getSingleResult());
	}*/
	
	@SuppressWarnings({ "rawtypes" })
	private void applyPagination(Specification specification, TypedQuery<List> query) {
		if(specification.getPageSize() != null && specification.getPageNumber() != null){
			query.setFirstResult(specification.getPageSize() * specification.getPageNumber());
			query.setMaxResults(specification.getPageSize());
		}
	}

	private String[] buildProjection(Class<?> clazz, Specification specification) {
		if(specification.projectionIsEmpty()){
			specification.setProjection(projectionUtils.buildEntityProjection(clazz).toArray(new String[0]));
		}
		else{
			List<String> resultProjection = new ArrayList<>();
			for(int i = 0 ; i < specification.getProjection().length ; i ++){
				String projection = specification.getProjection()[i];
				if(projection.contains(ROOT.concat(".All"))){
					resultProjection.addAll(projectionUtils.buildEntityProjection(clazz));
				}
				else if(projection.endsWith(".All")){
					String fieldName = projection.substring(0, projection.lastIndexOf(".All"));
					resultProjection.addAll(buildFieldFullProjection(clazz, specification, fieldName));
				}
				else{
					resultProjection.add(projection);
				}
			}
			specification.setProjection(resultProjection.toArray(new String[0]));
		}
		return removeDuplicateds(specification.getProjection());
	}

	private String[] removeDuplicateds(String[] projection) {
		Set<String> set = new HashSet<String>(Arrays.asList(projection));
		return set.toArray(new String[0]);
	}

	private List<String> buildFieldFullProjection(Class<?> clazz, Specification specification, String fieldName) {
		try {
			return projectionUtils.buildEntityProjection(getAttributeClassBasedOnDotNotation(clazz, fieldName), fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> getAttributeClassBasedOnDotNotation(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		String[] hierarchyDotSplit = fieldName.split("\\.");
		Class<?> fieldClass = clazz;
		for(String classHierarchy : hierarchyDotSplit){
			fieldClass = getDeclaredFieldWithDepth(fieldClass, classHierarchy).getType();
		}
		return fieldClass;
	}
	
	private Field getDeclaredFieldWithDepth(Class<?> clazz, String field) throws NoSuchFieldException{
		try{
			return clazz.getDeclaredField(field);
		}
		catch(NoSuchFieldException ex){
			if(clazz.getSuperclass() != null){
				return clazz.getSuperclass().getDeclaredField(field);
			}
			else{
				throw ex;
			}
		}
	}

	public <T, ID> T findOne(ID id, Class<T> clazz, String ...projection){
		List<T> result = this.find(clazz, new Specification(new Selection("id", id), projection));
		if(!result.isEmpty()){
			return result.get(0);
		}
		return null;
	}

	private <T> StringBuilder buildQuery(Class<T> clazz, Specification spec) {
		StringBuilder sql = new StringBuilder();
		applyProjection(clazz, sql, spec.getProjection());
		applyFilters(clazz, spec, sql);
		return sql;
	}

	private <T> void applyFilters(Class<T> clazz, Specification spec, StringBuilder sql) {
		sql.append(" FROM ");
		sql.append(clazz.getSimpleName()).append(" AS ").append(ROOT);
		applyJoin(clazz, spec , sql);
		applySelection(spec.getSelection(), sql);
		applyAgregation(spec.getAgregations(), sql);
	}

	private void applyJoin(Class<?> clazz, Specification spec, StringBuilder sql) {
		addJoinsBasedOnProjectionAndDefaultJoinAnnotation(clazz, spec);
		List<Join> joins = spec.getJoins();
		new HashSet<>(joins).forEach(join -> {
			sql.append(" ").append(join.getType()).append(" ").append(ROOT).append(".").append(join.getEntity());
		});
	}

	private void addJoinsBasedOnProjectionAndDefaultJoinAnnotation(Class<?> clazz, Specification spec) {
		for(String projection : spec.getProjection()){
			if(!projection.endsWith(".id")){
				String[] hierarchyDotSplit = projection.split("\\.");
				try{
					Class<?> fieldClass = clazz;
					String currentJoinValue = "";
					for(String classHierarchy : hierarchyDotSplit){
						currentJoinValue += currentJoinValue.isEmpty() ? classHierarchy : ("." + classHierarchy);
						Field field = getDeclaredFieldWithDepth(fieldClass, classHierarchy);
						DefaultJoin defaultJoin = field.getAnnotation(DefaultJoin.class);
						if(JoinType.LEFT.equals(Utils.safeEval(() -> defaultJoin.value()))){
							spec.getJoins().add(new LeftJoin(currentJoinValue));
						}
						fieldClass = field.getType();
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void applyAgregation(List<Agregation> agregations, StringBuilder sql) {
		agregations.forEach(agregation -> {
			sql.append(" " + agregation.getType().getValue() + " " + agregation.getValue());
		});
	}

	private void applySelection(List<Selection> selections, StringBuilder sql) {
		if(!selections.isEmpty()){
			sql.append(" WHERE ");
			int i = 1;
			for(Selection selection : selections){
				if(i != 1){
					sql.append(" " + selection.getCondition() + " ");
				}
				sql.append(ROOT.concat(".") + selection.buildSelectionWildcard(i));
				i++;
			}
		}
	}

	private void applyProjection(Class<?> clazz, StringBuilder sql, String... projection) {
		sql.append("SELECT new list(");
		Arrays.asList(projection).forEach(projectionItem -> { 
			sql.append(ROOT.concat(".") + projectionItem + ", "); 
		});
		sql.replace(sql.length() - 2, sql.length(), "");
		sql.append(") ");
	}
}
