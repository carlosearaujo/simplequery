package com.simplequery;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**@author carlos.araujo
   @since  26 de set de 2017*/
@Setter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
public class Specification {
	
	public Specification(List<Selection> selections, String ...projection){
		this.selection = selections;
		this.projection = projection;
	}
	
	public Specification(Selection selection, String ...projection){
		List<Selection> selections = new ArrayList<>();
		selections.add(selection);
		this.selection = selections;
		this.projection = projection;
	}
	
	private List<Selection> selection;
	private String[] projection;
	private List<Join> joins;
	
	public String[] getProjection(){
		if(projection == null){
			return new String[0];
		}
		return projection;
	}
	
	public List<Join> getJoins(){
		if(joins == null){
			joins = new ArrayList<>();
		}
		return joins;
	}
	
	public List<Selection> getSelection(){
		if(selection == null){
			selection = new ArrayList<>();
		}
		return selection;
	}
	
	public boolean projectionIsEmpty(){
		return getProjection().length == 0;
	}
	
	@JsonProperty("selectionAsArray")
	public void setSelectionArray(List<Object[]> selections){
		this.selection = new ArrayList<>();
		selections.forEach(selection -> {
			SelectionCondition type = selection.length > 2 ? SelectionCondition.valueOf((String)selection[2]) : SelectionCondition.DEFAULT_SELECTION_TYPE;
			this.selection.add(new Selection((String)selection[0], selection[1], type));
		});
	}
	
}
