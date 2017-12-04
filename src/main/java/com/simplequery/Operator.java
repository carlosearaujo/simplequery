package com.simplequery;

import lombok.Getter;

/**@author carlos.araujo
   @since  19 de out de 2017*/
@Getter
public enum Operator {
	
	EQUALS(" = "), LIKE(" LIKE "), IN(" IN ", " ( ", " )"), NEQUALS(" != "), NOTIN(" NOT IN "), BIGTHAN(" > "), 
	
	BIGEQTHAN(" >= "), LESSTHAN(" < "), LESSEQTHAN(" <= ");
	
	static final Operator DEFAULT_OPERATOR_TYPE = Operator.EQUALS;
	
	private String value;
	private String prefix;
	private String sufix;
	
	private Operator(String value){
		this(value, "", "");
	}
	
	private Operator(String value, String prefix, String sufix){
		this.value = value;
		this.prefix = prefix;
		this.sufix = sufix;
	}
}
