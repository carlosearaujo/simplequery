package com.simplequery;

import org.springframework.stereotype.Repository;

import java.util.List;

/**@author carlos.araujo
   @since  26 de jan de 2018*/
@Repository
public interface SimpleEntityRecovery {

	<T> List<T> find(Class<T> classType, Specification specification);
	//<T> Page<T> findPage(Class<T> classType, Specification specification);
	<T, ID> T findOne(ID id, Class<T> classType, String... projection);
}
