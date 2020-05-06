package com.simplequery.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface GenerateService {
	@SuppressWarnings("rawtypes")
	Class<? extends com.simplequery.GenericBusiness> customBusiness() default com.simplequery.GenericBusiness.class;
	boolean applyStereotype() default true;
}
