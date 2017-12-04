package com.simplequery;

import lombok.Getter;

/**@author carlos.araujo
   @since  20 de out de 2017*/
@Getter
public enum AgregationType {
	LIMIT("LIMIT"), ORDER_BY("ORDER BY");
	
	private String value;
	
	private AgregationType(String value){
		this.value = value;
	}
}
