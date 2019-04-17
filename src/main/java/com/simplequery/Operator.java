package com.simplequery;

/**@author carlos.araujo
   @since  19 de out de 2017*/
public enum Operator {
	
	EQUALS(" = "), LESS_EQ(" <= "), GREAT(" > "), GREAT_EQ(" >= "), LIKE(" LIKE "), IN(" IN "), NEQUALS(" != "), NOTIN(" NOT IN "), MEMBER_OF(" MEMBER OF "), 
	IS_NULL(" IS NULL "), IS_NOT_NULL(" IS NOT NULL ");
	
	static final Operator DEFAULT_OPERATOR_TYPE = Operator.EQUALS;
	
	private String value;
	
	private Operator(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public String applyParam(Object value, String param) {
		if(value == null) {
			return Operator.IS_NULL.getValue();
		}
		if(this.equals(Operator.IS_NULL) || this.equals(Operator.IS_NOT_NULL)) {
			return this.value;
		}
		return this.value + param;
	}

	public boolean hasValue(Object value) {
		return !this.equals(Operator.IS_NULL) && !this.equals(Operator.IS_NOT_NULL) && value != null;
	}
}
