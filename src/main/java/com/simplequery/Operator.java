package com.simplequery;

/**@author carlos.araujo
   @since  19 de out de 2017*/
public enum Operator {
	
	EQUALS(" = "), LIKE(" LIKE "), IN(" IN "), NEQUALS(" != "), NOTIN(" NOT IN "), BIGTHAN(" > "), 
	
	BIGEQTHAN(" >= "), LESSTHAN(" < "), LESSEQTHAN(" <= ");
	
	static final Operator DEFAULT_OPERATOR_TYPE = Operator.EQUALS;
	
	private String value;
	
	private Operator(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
}
