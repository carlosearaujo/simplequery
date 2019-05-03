package com.simplequery;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Getter
public class Page<T> {
	
	List<T> results;
	Long totalSize;
	Integer pageIndex;
	Integer pageSize;

	public Page(List<T> results, Long totalSize, Integer pageIndex, Integer pageSize) {
		this.results = results;
		this.totalSize = totalSize;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

}
