package com.simplequery;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**@author carlos.araujo
   @since  26 de set de 2017*/
public class GenericController<T, E extends IGenericBusiness<T>> {
	
	protected E business;
	
	public GenericController(E business){
		this.business = business;
	}

	@RequestMapping(value = "findPage", method = RequestMethod.POST)
	public Page<T> getPage(@RequestBody Specification specification){
		return business.findPage(specification);
	}
	
	@RequestMapping(value = "find", method = RequestMethod.POST)
	public List<T> get(@RequestBody Specification specification){
		return business.find(specification);
	}
	
	@RequestMapping(value = "findOne/{id}", method = RequestMethod.GET)
	public T get(@PathVariable Long id){
		return business.findById(id);
	}
	
	@PostMapping(value = "findOne/{id}")
	public T get(@PathVariable Long id, @RequestBody Specification specification){
		specification.getSelection().add(new Selection("id", id));
		return business.findOne(specification);
	}
	
	@DeleteMapping(value = "/{entityIds}")
	public void delete(@PathVariable Integer[] entityIds){
		business.delete(entityIds);
	}
	
	@PostMapping
	public T save(@RequestBody T entity) {
		return business.save(entity);
	}
	
	@PostMapping(value = "saveAll")
	public List<T> saveAll(@RequestBody List<T> entityList) {
		return business.save(entityList);
	}
	
}
