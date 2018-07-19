package com.simplequery;

import org.hibernate.HibernateException;
import org.hibernate.property.access.internal.PropertyAccessStrategyBasicImpl;
import org.hibernate.property.access.internal.PropertyAccessStrategyChainedImpl;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.internal.PropertyAccessStrategyMapImpl;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.hibernate.transform.ResultTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @author marcelo.mourao
 * @since  07/07/2017
 */
public class AliasToBeanMultiLevelNestedResultTransformer extends AliasedTupleSubsetResultTransformer {

    private static final long serialVersionUID = 1L;
    
    private static final int TUPE_INDEX = 0;
    private static final int ALISES_INDEX = 1;
    private static final int FIELDNAME_INDEX = 2;    
    
    private static final PropertyAccessStrategyChainedImpl propertyAccessStrategy = new PropertyAccessStrategyChainedImpl(
			PropertyAccessStrategyBasicImpl.INSTANCE,
			PropertyAccessStrategyFieldImpl.INSTANCE,
			PropertyAccessStrategyMapImpl.INSTANCE
	);

    private final Class<?> resultClass;

    private Object[] entityTuples;
    private String[] entityAliases;

    private Map<String, Class<?>> fieldToClass = new HashMap<String, Class<?>>();
    private Map<String, List<?>> subEntities = new HashMap<String, List<?>>();
    private List<String> nestedAliases = new ArrayList<String>();
    private Map<String, Class<?>> listFields = new HashMap<String, Class<?>>();
    
    private String[] aliases = null;

    public AliasToBeanMultiLevelNestedResultTransformer(Class<?> resultClass) {
    	this.resultClass = resultClass;
    }
    
    public AliasToBeanMultiLevelNestedResultTransformer(Class<?> resultClass, String[] aliases) {
    	this.resultClass = resultClass;
    	this.aliases = aliases;
    }
    
    public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
        return false;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
    	
    	if(this.aliases != null){
    		aliases = this.aliases;
    	}
    	
        handleSubEntities(tuple, aliases);
        
        cleanParams(tuple, aliases);
        
        ResultTransformer rootTransformer = new AliasToBeanResultTransformer(resultClass);
        
        Object root = rootTransformer.transformTuple(entityTuples,entityAliases);

        loadSubEntities(root);

        cleanMaps();
        
        return root;
    }

    private void handleSubEntities(Object[] tuple, String[] aliases) throws HibernateException {
        String fieldName = "";
        String aliasName = "";
        try {
            for (int i = 0; i < aliases.length; i++) {
                String alias = aliases[i];
                if (alias.contains(".")) {

                    String[] sp = alias.split("\\.");
                    StringBuilder aliasBuilder = new StringBuilder();
                    for (int j = 0; j < sp.length; j++) {
                        if (j == 0) {
                            fieldName = sp[j];
                        } else {
                            aliasBuilder.append(sp[j]);
                            aliasBuilder.append(".");
                        }
                    }
                    aliasName = aliasBuilder.substring(0, aliasBuilder.length() - 1);

                    nestedAliases.add(alias);
                    manageEntities(fieldName, aliasName, tuple[i]);
                }
            }
        } catch (NoSuchFieldException e) {
            throw new HibernateException("Could not instantiate resultclass: "
                    + resultClass.getName() + " for field name: " + fieldName
                    + " and alias name:" + aliasName);
        }
    }

    private Class<?> findClass(String fieldName) throws NoSuchFieldException, SecurityException {
        if (fieldToClass.containsKey(fieldName)) {
            return fieldToClass.get(fieldName);
        } else {
            Class<?> subclass = resultClass.getDeclaredField(fieldName).getType();

            if (subclass.equals(List.class) || subclass.equals(Set.class)) {
                if (subclass.equals(List.class)) {
                    listFields.put(fieldName, LinkedList.class);
                } else {
                    listFields.put(fieldName, HashSet.class);
                }
                Field field = resultClass.getDeclaredField(fieldName);
                
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                
                subclass = (Class<?>) genericType.getActualTypeArguments()[0];
            }
            fieldToClass.put(fieldName, subclass);
            return subclass;
        }
    }

    @SuppressWarnings("unchecked")
    private void manageEntities(String fieldName, String aliasName, Object tupleValue) throws NoSuchFieldException, SecurityException {
        Class<?> subclass = findClass(fieldName);
        
        if (!subEntities.containsKey(fieldName)) {
            List<Object> list = new ArrayList<Object>();
            list.add(new ArrayList<Object>());
            list.add(new ArrayList<String>());
            list.add(FIELDNAME_INDEX, subclass);
            subEntities.put(fieldName, list);
        }
        
        ((List<Object>) subEntities.get(fieldName).get(TUPE_INDEX)).add(tupleValue);
        
        ((List<String>) subEntities.get(fieldName).get(ALISES_INDEX)).add(aliasName);
    }

    private void cleanParams(Object[] tuple, String[] aliases) {
        entityTuples = new Object[aliases.length - nestedAliases.size()];
        entityAliases = new String[aliases.length - nestedAliases.size()];

        for (int j = 0, i = 0; j < aliases.length; j++) {
            if (!nestedAliases.contains(aliases[j])) {
                entityTuples[i] = tuple[j];
                entityAliases[i] = aliases[j];
                ++i;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void loadSubEntities(Object root) throws HibernateException {
        try {
            for (String fieldName : subEntities.keySet()) {
                Class<?> subclass = (Class<?>) subEntities.get(fieldName).get(FIELDNAME_INDEX);

                ResultTransformer subclassTransformer = new AliasToBeanMultiLevelNestedResultTransformer(
                        subclass);

                Object subObject = subclassTransformer.transformTuple(
                        	((List<Object>) subEntities.get(fieldName).get(0)).toArray(),
                        	((List<Object>) subEntities.get(fieldName).get(1)).toArray(new String[0])
                       );

                if (listFields.containsKey(fieldName)) {
                    Class<?> collectionClass = listFields.get(fieldName);
                    Collection subObjectList = (Collection) collectionClass.newInstance();
                    subObjectList.add(subObject);
                    
                    propertyAccessStrategy.buildPropertyAccess(resultClass, fieldName).getSetter().set(root, subObjectList, null);
                } else {
                	propertyAccessStrategy.buildPropertyAccess(resultClass, fieldName).getSetter().set(root, subObject, null);
                }
            }
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    private void cleanMaps() {
        fieldToClass = new HashMap<String, Class<?>>();
        subEntities = new HashMap<String, List<?>>();
        nestedAliases = new ArrayList<String>();
        listFields = new HashMap<String, Class<?>>();
    }

}
