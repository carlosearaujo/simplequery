package com.simplequery;

/**@author carlos.araujo
   @since  26 de set de 2017*/
public enum SelectionCondition {
	
	AND, OR;
	
	static final SelectionCondition DEFAULT_SELECTION_TYPE = SelectionCondition.AND;
}
