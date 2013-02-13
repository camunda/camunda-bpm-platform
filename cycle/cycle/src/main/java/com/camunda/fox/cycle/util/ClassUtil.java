package com.camunda.fox.cycle.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author nico.rehwaldt
 */
public class ClassUtil {

  /**
   * Returns the parameterized type of a class, e.g. the 
   * <T> part of a generic class definition as a Class<T>. 
   * 
   * That inference is not possible except we do it at run-time: 
   * http://blog.xebia.com/2009/02/07/acessing-generic-types-at-runtime-in-java/
   * 
   * @param cls
   * @return 
   */
  public static Class<?> extractParameterizedType(Class<?> cls) {
    
    Type type = cls.getGenericSuperclass();
    if (type instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) type;
      
      Type[] typeArguments = ptype.getActualTypeArguments();
      if (typeArguments.length > 0) {
        return (Class<?>) typeArguments[0];
      }
    }
    
    return null;
  }
}
