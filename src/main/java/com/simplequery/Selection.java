package com.simplequery;

import java.util.Arrays;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**@author carlos.araujo
   @since  26 de set de 2017*/
@Getter @Setter @NoArgsConstructor
public class Selection {
	
	private String field;
	private Object value;
	private SelectionCondition condition = SelectionCondition.DEFAULT_SELECTION_TYPE;
	private Operator operator = Operator.DEFAULT_OPERATOR_TYPE;
	private Boolean nestedSelectionStart;
	private Boolean nestedSelectionEnd;
	
	public Selection(String key, Object value, SelectionCondition type, Operator operator) {
		this.field = key;
		this.value = value;
		this.condition = (type == null ? SelectionCondition.DEFAULT_SELECTION_TYPE : type);
		this.operator = (operator == null ? Operator.DEFAULT_OPERATOR_TYPE  : operator);
	}
	
	public Selection(String key, Object value, SelectionCondition type) {
		this(key, value, type, null);
	}

	public Selection(String key, Object value) {
		this(key, value, null);
	}

	public String getSelectionStr() {
		StringBuilder builder = new StringBuilder(nestedSelectionStart != null && nestedSelectionStart ? "(" : "");
		builder.append(getField());
		if(getValue().getClass().isArray()){
			Object[] array = (Object[]) getValue();
			if(array.length > 1){
				builder.append(Operator.IN.getValue());
				builder.append(" ( ");
				Arrays.asList(array).forEach(value -> {
					builder.append(convertStrType(value) + ", ");
				});
				builder.replace(builder.length() - 2, builder.length(), " )");
			}
			else{
				builder.append(operator.getValue() + convertStrType(array[0]));
			}
		}
		else{
			builder.append(operator.getValue() + convertStrType(value));
		}
		builder.append(nestedSelectionEnd != null && nestedSelectionEnd ? ")" : "");
		return builder.toString();
	}

	private Object convertStrType(Object value) {
		if(value instanceof String){
			return "'" + value + "'";
		}
		return value;
	}
	
}
