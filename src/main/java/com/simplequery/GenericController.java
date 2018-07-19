package com.simplequery;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**@author carlos.araujo
   @since  26 de set de 2017*/
public class GenericController<T> {
	
	private GenericBusiness<T> business;
	
	public GenericController(GenericBusiness<T> business){
		this.business = business;
	}
	
	@RequestMapping(value = "find", method = RequestMethod.POST)
	public List<T> get(@RequestBody Specification specification){
		return business.find(specification);
	}
	
	@RequestMapping(value = "findOne/{id}", method = RequestMethod.GET)
	public T get(@PathVariable Long id){
		return business.findById(id);
	}
	
	@RequestMapping(value = "findOne/{id}", method = RequestMethod.POST)
	public T get(@PathVariable Long id, @RequestBody Specification specification){
		specification.getSelection().add(new Selection("id", id));
		return business.findOne(specification);
	}
	
}
