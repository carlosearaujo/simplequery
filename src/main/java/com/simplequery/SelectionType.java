package com.simplequery;

/**@author carlos.araujo
   @since  26 de set de 2017*/
public enum SelectionType {
	
	AND, OR, EXCLUSIVE_OR, EXCLUSIVE_AND, NEW_EXCLUSIVE_OR;
	
	static final SelectionType DEFAULT_SELECTION_TYPE = SelectionType.AND;
}
