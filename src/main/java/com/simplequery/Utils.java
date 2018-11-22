package com.simplequery;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

/**
 * Created by Carlos on 18/07/2018.
 */
public class Utils {
    public static <T> T safeEval(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return result;
        }
        catch (NullPointerException e) {
            return null;
        }
    }

	public static <T> Class<T> getGenericType(Class<?> clazz) {
		return (Class<T>)((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}
}
