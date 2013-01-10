package org.camunda.bpm.engine.rest.dto;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractQueryParameterDto {

  protected void setValueBasedOnAnnotation(String key, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Method[] methods = this.getClass().getMethods();
    
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      Annotation[] methodAnnotations = method.getAnnotations();
      
      for (int j = 0; j < methodAnnotations.length; j++) {
        Annotation annotation = methodAnnotations[j];
        if (annotation instanceof CamundaQueryParam) {
          CamundaQueryParam parameterAnnotation = (CamundaQueryParam) annotation;
          if (parameterAnnotation.value().equals(key)) {
            method.invoke(this, value);
          }
        }
      }
    }
  }
  
  public abstract void setPropertyFromParameterPair(String key, String value);
}
