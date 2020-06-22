package com.simplequery;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

/**
 * @author carlos.araujo
 * @since 6 de set de 2017
 */
@Component
@Setter
@Getter
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SimpleEntityRecoveryImpl implements SimpleEntityRecovery {

  private static final String ROOT = "this";

  private EntityManagerFactory emf;
  @Autowired ApplicationContext appContext;
  @Autowired private ObjectMapper mapper;

  private String persistenceUnit;

  public SimpleEntityRecoveryImpl() {}

  public void setPersistenceUnit(String PU) {
    persistenceUnit = PU;
    emf = (EntityManagerFactory) appContext.getAutowireCapableBeanFactory().getBean(PU);
  }

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  ProjectionUtils projectionUtils = new ProjectionUtils();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> List<T> find(Class<T> clazz, Specification specification) {
	 EntityManager em = getEntityManager();
	 AliasToBeanMultiLevelNestedResultTransformer transformer = new AliasToBeanMultiLevelNestedResultTransformer(clazz);
	 specification.setProjection(buildProjection(clazz, specification));
	 StringBuilder sql = buildQuery(clazz, specification);
	 List<T> result = new ArrayList<T>();
	 TypedQuery<List> query = em.createQuery(sql.toString(), List.class);
	 applySelectionValues(clazz, query, specification.getSelection());
	 applyPagination(specification, query);
	 query.getResultList().forEach(resultItem -> {
	   result.add((T) transformer.transformTuple(resultItem.toArray(), specification.getProjection()));
	 });
	 em.close();
	 return result;
	}

  private void applySelectionValues(Class<?> clazz, TypedQuery<?> query, List<Selection> selections) {
    int i = 1;
    for (Selection selection : selections) {
      if (selection.getOperator().hasValue(selection.getValue())) {
        Object value = selection.isArrayValue() ? selection.getValue() : convertValue(clazz, selection);
        query.setParameter("param" + i, value);
        i++;
      }
    }
  }

  private Object convertValue(Class<?> clazz, Selection selection) {
    Object value = selection.getValue();
    Field attrField = getAttributeFieldBasedOnDotNotation(clazz, selection.getField());
    if (attrField == null) {
      return value;
    }
    /*Object converterResult = convertWithConverter(value, attrField);
    if (converterResult != null) {
      return converterResult;
    }*/
    Class<?> attrClass = attrField.getType();
    if ((value == null || attrClass == value.getClass())) {
      return value;
    }
    if (attrClass.isAssignableFrom(Long.class)) {
      return new Long((Integer) value);
    }
    if (attrClass.isAssignableFrom(Integer.class)) {
      return (Integer) ((Long) value).intValue();
    }

    if (attrClass.isAssignableFrom(List.class) && value instanceof Map) {
      attrClass = getListType(attrField);
      try {
        return mapper.convertValue(value, attrClass);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return value;
  }

  private Object convertWithConverter(Object value, Field attrField) {
    Convert convert = attrField.getAnnotation(Convert.class);
    if (convert != null) {
      try {
        AttributeConverter<Object, Object> converter = (AttributeConverter<Object, Object>) convert.converter().newInstance();
        return converter.convertToDatabaseColumn(value);
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  private Class<?> getListType(Field attrField) {
    return (Class<?>) ((ParameterizedType) attrField.getGenericType()).getActualTypeArguments()[0];
  }

  public <T> Page<T> findPage(Class<T> clazz, Specification specification) {
    EntityManager em = getEntityManager();
    StringBuilder sql = new StringBuilder();
    specification.setProjection(buildProjection(clazz, specification));
    sql.append("SELECT COUNT(*) ");
    applyFilters(clazz, specification, sql, false);
    TypedQuery<Long> query = em.createQuery(sql.toString(), Long.class);
    applySelectionValues(clazz, query, specification.getSelection());
    Page<T> result = new Page<T>(find(clazz, specification), query.getSingleResult(), specification.getPageNumber(), specification.getPageSize());
    em.close();
    return result;
  }

  @SuppressWarnings({"rawtypes"})
  private void applyPagination(Specification specification, TypedQuery<List> query) {
    if (specification.getPageSize() != null && specification.getPageNumber() != null) {
      query.setFirstResult(specification.getPageSize() * specification.getPageNumber());
      query.setMaxResults(specification.getPageSize());
    }
  }

  private String[] buildProjection(Class<?> clazz, Specification specification) {
    if (specification.projectionIsEmpty()) {
      specification.setProjection(projectionUtils.buildEntityProjection(clazz).toArray(new String[0]));
    } else {
      List<String> resultProjection = new ArrayList<>();
      for (int i = 0; i < specification.getProjection().length; i++) {
        String projection = specification.getProjection()[i];
        if (projection.contains(ROOT.concat(".All"))) {
          resultProjection.addAll(projectionUtils.buildEntityProjection(clazz));
        } else if (projection.endsWith(".All")) {
          String fieldName = projection.substring(0, projection.lastIndexOf(".All"));
          resultProjection.addAll(buildFieldFullProjection(clazz, specification, fieldName));
        } else {
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
    return projectionUtils.buildEntityProjection(getAttributeClassBasedOnDotNotation(clazz, fieldName), fieldName);
  }

  private Class<?> getAttributeClassBasedOnDotNotation(Class<?> clazz, String fieldName) {
    return getAttributeFieldBasedOnDotNotation(clazz, fieldName).getType();
  }

  private Field getAttributeFieldBasedOnDotNotation(Class<?> clazz, String fieldName) {
    try {
      String[] hierarchyDotSplit = fieldName.split("\\.");
      Field fieldClass = null;
      for (String classHierarchy : hierarchyDotSplit) {
        fieldClass = getDeclaredFieldWithDepth(fieldClass != null ? fieldClass.getType() : clazz, classHierarchy);
      }
      return fieldClass;
    } catch (Exception e) {
      return null;
    }
  }

  private Field getDeclaredFieldWithDepth(Class<?> clazz, String field) throws NoSuchFieldException {
    try {
      return clazz.getDeclaredField(field);
    } catch (NoSuchFieldException ex) {
      if (clazz.getSuperclass() != null) {
        return clazz.getSuperclass().getDeclaredField(field);
      } else {
        throw ex;
      }
    }
  }

  public <T, ID> T findOne(ID id, Class<T> clazz, String... projection) {
    List<T> result = this.find(clazz, new Specification(new Selection("id", id), projection));
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  private <T> StringBuilder buildQuery(Class<T> clazz, Specification spec) {
    StringBuilder sql = new StringBuilder();
    applyProjection(clazz, sql, spec);
    applyFilters(clazz, spec, sql, true);
    return sql;
  }

  private <T> void applyFilters(Class<T> clazz, Specification spec, StringBuilder sql, boolean applyAgregation) {
    sql.append(" FROM ");
    sql.append(clazz.getName()).append(" AS ").append(ROOT);
    applyJoin(clazz, spec, sql);
    applySelection(spec, sql);
    if (applyAgregation) {
      applyAgregation(spec.getAgregations(), sql);
    }
  }

  private void applyJoin(Class<?> clazz, Specification spec, StringBuilder sql) {
    addJoinsBasedOnProjectionAndDefaultJoinAnnotation(clazz, spec);
    List<Join> joins = spec.getJoins().stream().distinct().collect(Collectors.toList());
    joins.forEach(join -> {
      Join root = getRoot(spec, join.getEntity());
      sql.append(String.format(" %s %s.%s %s", join.getType(), root.getAlias(), join.getEntity().replaceFirst(root.entity + "\\.", ""), join.getAlias()));
    });
  }

  private void addJoinsBasedOnProjectionAndDefaultJoinAnnotation(Class<?> clazz, Specification spec) {
    for (String projection : spec.getProjection()) {
      if (!projection.endsWith(".id")) {
        String[] hierarchyDotSplit = projection.split("\\.");
        try {
          String currentJoinValue = "";
          for (String classHierarchy : hierarchyDotSplit) {
            currentJoinValue += currentJoinValue.isEmpty() ? classHierarchy : ("." + classHierarchy);
          }
        } catch (Exception e) {
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

  private void applySelection(Specification spec, StringBuilder sql) {
    List<Selection> selections = spec.getSelection();
    if (!selections.isEmpty()) {
      sql.append(" WHERE ");
      int i = 1;
      for (Selection selection : selections) {
        if (i != 1) {
          sql.append(String.format(" %s ", selection.getCondition()));
        }
        sql.append(selection.buildSelectionWildcard(getRoot(spec, selection.getField()), i));
        i++;
      }
    }
  }

  private Join getRoot(Specification spec, String field) {
    Join[] matchJoin = new Join[1];
    if (field.contains(".")) {
      String fieldMatcher = field.substring(0, field.lastIndexOf("."));
      for (Join join : spec.getJoins()) {
        if (fieldMatcher.equals(join.getEntity())) {
          matchJoin[0] = join;
        }
      }
      if (matchJoin[0] == null) {
        return getRoot(spec, fieldMatcher);
      }
    }
    return matchJoin[0] != null ? matchJoin[0] : new Join(ROOT, null);
  }

  private void applyProjection(Class<?> clazz, StringBuilder sql, Specification spec) {
    String[] projection = spec.getProjection();
    sql.append("SELECT new list(");
    Arrays.asList(projection).forEach(projectionItem -> {
      Join root = getRoot(spec, projectionItem);
      sql.append(String.format("%s.%s, ", root.getAlias(), projectionItem.replaceFirst(root.entity + "\\.", "")));
    });
    if (sql.toString().endsWith(", ")) {
      sql.replace(sql.length() - 2, sql.length(), "");
    }
    sql.append(") ");
  }

  @Override
  @Transactional
  public <T, ID> void delete(Class<T> clazz, ID[] entityIds) {
    getEntityManager().createQuery("DELETE FROM " + clazz.getSimpleName() + " r where r.id IN (:ids)").setParameter("ids", Arrays.asList(entityIds)).executeUpdate();
  }
}
