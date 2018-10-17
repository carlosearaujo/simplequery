package com.simplequery;

/**@author carlos.araujo
   @since  19 de out de 2017*/
public enum Operator {
	
	EQUALS(" = "), LESS_EQ(" <= "), LIKE(" LIKE "), IN(" IN "), NEQUALS(" != "), NOTIN(" NOT IN "), MEMBER_OF(" MEMBER OF ");
	
	static final Operator DEFAULT_OPERATOR_TYPE = Operator.EQUALS;
	
	private String value;
	
	private Operator(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
}
