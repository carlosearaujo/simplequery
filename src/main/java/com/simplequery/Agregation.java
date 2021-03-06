package com.simplequery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**@author carlos.araujo
   @since  20 de out de 2017*/
@Getter @Setter @NoArgsConstructor
public class Agregation {
	
	public Agregation(AgregationType type, Object value){
		this.type = type;
		this.value = value;
	}
	
	private AgregationType type;
	private Object value;
}
