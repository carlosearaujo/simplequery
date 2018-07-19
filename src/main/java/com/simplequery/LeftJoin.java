package com.simplequery;

/**@author carlos.araujo
   @since  10 de jan de 2018*/
public class LeftJoin extends Join {

	public LeftJoin(String entity) {
		super(entity, "LEFT JOIN");
	}

}
