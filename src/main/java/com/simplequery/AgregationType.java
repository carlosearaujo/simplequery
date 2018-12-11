package com.simplequery;

import lombok.Getter;

/**@author carlos.araujo
   @since  20 de out de 2017*/
@Getter
public enum AgregationType {
	LIMIT("LIMIT"), ORDER_BY("ORDER BY"), GROUP_BY("GROUP BY");
	
	private String value;
	
	private AgregationType(String value){
		this.value = value;
	}
}
