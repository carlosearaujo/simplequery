package com.simplequery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

/**
 * @author carlos.araujo
   @since  6 de set de 2017 */
@Repository
public class SimpleEntityRecovery {
	
	private static final  String ROOT = "this";
	
	@PersistenceContext
	private EntityManager entityManager;
	
	ProjectionUtils projectionUtils = new ProjectionUtils();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> find(Class<T> clazz, Specification specification) {
		AliasToBeanMultiLevelNestedResultTransformer transformer = new AliasToBeanMultiLevelNestedResultTransformer(clazz);
		specification.setProjection(buildProjection(clazz, specification));
		StringBuilder sql = buildQuery(clazz, specification);
		List<T> result = new ArrayList<T>();
		TypedQuery<List> query = entityManager.createQuery(sql.toString(), List.class);
		query.getResultList().forEach(resultItem -> {
			result.add((T) transformer.transformTuple(resultItem.toArray(), specification.getProjection()));
		});
		return result;
	}
	
	private String[] buildProjection(Class<?> clazz, Specification specification) {
		if(specification.projectionIsEmpty()){
			return projectionUtils.buildPlanProjection(clazz).toArray(new String[0]);
		}
		else{
			List<String> resultProjection = new ArrayList<>();
			for(int i = 0 ; i < specification.getProjection().length ; i ++){
				String projection = specification.getProjection()[i];
				if(projection.contains(ROOT.concat(".All"))){
					resultProjection.addAll(projectionUtils.buildPlanProjection(clazz));
				}
				else if(projection.endsWith(".All")){
					resultProjection.addAll(buildFieldFullProjection(clazz, specification, projection.substring(0, projection.lastIndexOf(".All"))));
				}
				else{
					resultProjection.add(projection);
				}
			}
			specification.setProjection(resultProjection.toArray(new String[0]));
		}
		return specification.getProjection();
	}

	private List<String> buildFieldFullProjection(Class<?> clazz, Specification specification, String fieldName) {
		try {
			String[] hierarchyDotSplit = fieldName.split("\\.");
			Class<?> fieldClass = clazz;
			for(String classHierarchy : hierarchyDotSplit){
				fieldClass = fieldClass.getDeclaredField(classHierarchy).getType();
			}
			return projectionUtils.buildEntityProjection(fieldClass, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		sql.append(" FROM ");
		sql.append(clazz.getSimpleName()).append(" AS ").append(ROOT);
		applyJoin(spec.getJoins(), sql);
		applySelection(spec.getSelection(), sql);
		applyAgregation(spec.getAgregations(), sql);
		return sql;
	}

	private void applyJoin(List<Join> joins, StringBuilder sql) {
		joins.forEach(join -> {
			sql.append(" ").append(join.getType()).append(" ").append(ROOT).append(".").append(join.getEntity());
		});
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
				sql.append(ROOT.concat(".") + selection.getSelectionStr());
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
