package com.simplequery;

import javax.persistence.criteria.JoinType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**@author carlos.araujo
   @since  6 de fev de 2018*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultJoin {
	JoinType value();
}
