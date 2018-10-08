package com.simplequery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**@author carlos.araujo
   @since  26 de set de 2017*/
@Getter @Setter @NoArgsConstructor
public class Selection {
	
	private String field;
	private Object value;
	private SelectionCondition condition = SelectionCondition.DEFAULT_SELECTION_TYPE;
	private Boolean nestedSelectionStart;
	private Operator operator = Operator.DEFAULT_OPERATOR_TYPE;
	private Boolean nestedSelectionEnd;
	private String fieldFunction;
	
	public Selection(String key, Object value, SelectionCondition type, Operator operator) {
		this.field = key;
		this.value = value;
		this.condition = (type == null ? SelectionCondition.DEFAULT_SELECTION_TYPE : type);
		this.operator = (operator == null ? Operator.DEFAULT_OPERATOR_TYPE  : operator);
	}

	public Selection(String field, Object value, Operator operator) {
		this(field, value, null, operator);
	}
	
	public Selection(String key, Object value, SelectionCondition type) {
		this(key, value, type, null);
	}

	public Selection(String key, Object value) {
		this(key, value, null, null);
	}

	public String buildSelectionWildcard(String root, int position) {
		StringBuilder builder = new StringBuilder(nestedSelectionStart != null && nestedSelectionStart ? "(" : "");
		builder.append(applyFieldFunction(String.format("%s.%s", root, getField())));
		boolean isArray = isArrayValue();
		builder.append(operator.getValue());
		builder.append(isArray ? String.format("(:param%d)", position) : String.format(":param%d", position));
		builder.append(nestedSelectionEnd != null && nestedSelectionEnd ? ")" : "");
		return builder.toString();
	}

	public String applyFieldFunction(String value) {
		if(this.fieldFunction != null) {
			return String.format("%s(%s)", fieldFunction, value);
		}
		return value;
	}

	public static Selection[] removeNullValues(Selection[] selections) {
		return selections;
	}
	
	public boolean isArrayValue(){
		Object value = getValue();
		return value != null && getValue().getClass().isArray();
	}

	public static <T> Collection<? extends Selection> buildBasedOnFieldsNotNull(T object, String prefix) {
		List<Selection> selections = new ArrayList<>();
		for(Field field : object.getClass().getDeclaredFields()){
			Class<?> fieldClass = field.getType();
			if((field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class)) && !fieldClass.isArray()){
				Object attrValue = ReflectionUtils.getField(field, object);
				if(attrValue != null){
					if(BeanUtils.isSimpleValueType(fieldClass)){
						selections.add(new Selection(prefix + field.getName(), attrValue));
					}
					else{
						String newPrefix = prefix + field.getName() + ".";
						selections.addAll(buildBasedOnFieldsNotNull(attrValue, newPrefix));
					}
				}
			}
		}
		return selections;
	}

	public static <T> Collection<? extends Selection> buildBasedOnFieldsNotNull(T object) {
		return buildBasedOnFieldsNotNull(object, "");
	}
	
}
