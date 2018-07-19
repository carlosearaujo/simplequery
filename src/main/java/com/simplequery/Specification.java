package com.simplequery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**@author carlos.araujo
   @since  26 de set de 2017*/
@Setter @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
public class Specification {
	
	private List<Selection> selection;
	private String[] projection;
	private List<Join> joins;
	private List<Agregation> agregation;
	
	private Integer pageNumber; public Integer getPageNumber(){ return pageNumber; }
	private Integer pageSize; public Integer getPageSize(){ return pageSize; }
	
	public Specification(List<Selection> selections, String ...projection){
		this.selection = selections;
		this.projection = projection;
	}
	
	public Specification(Selection[] selections, String ...projection){
		this.selection = Arrays.asList(selections);
		this.projection = projection;
	}
	
	public Specification(Selection selection, String ...projection){
		List<Selection> selections = new ArrayList<>();
		selections.add(selection);
		this.selection = selections;
		this.projection = projection;
	}
	
	public Specification(Selection[] selections, Agregation[] agregations, String ...projection){
		this.agregation = Arrays.asList(agregations);
		this.selection = Arrays.asList(selections);
		this.projection = projection;
	}
	
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
	
	public List<Agregation> getAgregations(){
		if(agregation == null){
			agregation = new ArrayList<>();
		}
		return agregation;
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

	public Specification(List<Selection> selections, Integer pageNumber, Integer pageSize, String ...projection) {
		this(selections, projection);
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}
	
	@JsonProperty("leftProjection")
	public void setLeftProjection(String[] leftProjections){
		List<Join> joins = new ArrayList<>();
		for(String leftProjection : leftProjections){
			String leftTarget = leftProjection.substring(0, leftProjection.lastIndexOf("."));
			joins.add(new LeftJoin(leftTarget));
		}
		addJoins(joins);
		addProjection(leftProjections);
	}
	
	@JsonProperty("joins")
	public void addJoins(List<Join> joins){
		if(this.joins == null){
			this.joins = joins;
		}
		else{
			this.joins.addAll(joins);
		}
	}
	
	@JsonProperty("projection")
	public void addProjection(String[] projection){
		if(this.projection == null){
			this.projection = projection;
		}
		else{
			List<String> newProjection = new ArrayList<String>(Arrays.asList(this.projection));
			newProjection.addAll(Arrays.asList(projection));
			this.projection = newProjection.toArray(new String[0]);
		}
	}
	
}
