package com.simplequery;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Getter
public class Page<T> {
	
	List<T> results;
	Long totalSize;

	public Page(List<T> results, Long totalSize) {
		this.results = results;
		this.totalSize = totalSize;
	}

}
