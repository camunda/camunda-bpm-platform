package org.camunda.bpm.engine.rest.dto;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.converter.StringToTypeConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * Defines common query sorting options and validation.
 * Also allows to access its setter methods based on {@link CamundaQueryParam} annotations which is
 * used for processing Http query parameters.
 * 
 * @author Thorben Lindhauer
 *
 */
public abstract class SortableParameterizedQueryDto {
  
  protected static final String SORT_ORDER_ASC_VALUE = "asc";
  protected static final String SORT_ORDER_DESC_VALUE = "desc";
  
  private static final List<String> VALID_SORT_ORDER_VALUES;
  static {
    VALID_SORT_ORDER_VALUES = new ArrayList<String>();
    VALID_SORT_ORDER_VALUES.add(SORT_ORDER_ASC_VALUE);
    VALID_SORT_ORDER_VALUES.add(SORT_ORDER_DESC_VALUE);
  }

  protected String sortBy;
  protected String sortOrder;
  
  @CamundaQueryParam("sortBy")
  public void setSortBy(String sortBy) {
    if (!isValidSortByValue(sortBy)) {
      throw new InvalidRequestException("sortBy parameter has invalid value.");
    }
    this.sortBy = sortBy;
  }

  @CamundaQueryParam("sortOrder")
  public void setSortOrder(String sortOrder) {
    if (!VALID_SORT_ORDER_VALUES.contains(sortOrder)) {
      throw new InvalidRequestException("sortOrder parameter has invalid value.");
    }
    this.sortOrder = sortOrder;
  }
  
  protected abstract boolean isValidSortByValue(String value);

  protected boolean sortOptionsValid() {
    return (sortBy != null && sortOrder != null) || (sortBy == null && sortOrder == null);
  }
  
  /**
   * Finds the methods that are annotated with a {@link CamundaQueryParam} with a value that matches the key parameter.
   * Before invoking these methods, the annotated {@link StringToTypeConverter} is used to convert the String value to the desired Java type.
   * @param key
   * @param value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  public void setValueBasedOnAnnotation(String key, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
    List<Method> matchingMethods = findMatchingAnnotatedMethods(key);
    for (Method method : matchingMethods) {
      Class<? extends StringToTypeConverter<?>> converterClass = findAnnotatedTypeConverter(method);
      if (converterClass == null) {
        continue;
      }
      
      StringToTypeConverter<?> converter = converterClass.newInstance();
      Object convertedValue = converter.convertQueryParameterToType(value);
      method.invoke(this, convertedValue);
    }
  }
  
  private List<Method> findMatchingAnnotatedMethods(String parameterName) {
    List<Method> result = new ArrayList<Method>();
    Method[] methods = this.getClass().getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      Annotation[] methodAnnotations = method.getAnnotations();
      
      for (int j = 0; j < methodAnnotations.length; j++) {
        Annotation annotation = methodAnnotations[j];
        if (annotation instanceof CamundaQueryParam) {
          CamundaQueryParam parameterAnnotation = (CamundaQueryParam) annotation;
          if (parameterAnnotation.value().equals(parameterName)) {
            result.add(method);
          }
        }
      }
    }
    return result;
  }
  
  private Class<? extends StringToTypeConverter<?>> findAnnotatedTypeConverter(Method method) {
    Annotation[] methodAnnotations = method.getAnnotations();
    
    for (int j = 0; j < methodAnnotations.length; j++) {
      Annotation annotation = methodAnnotations[j];
      if (annotation instanceof CamundaQueryParam) {
        CamundaQueryParam parameterAnnotation = (CamundaQueryParam) annotation;
        return parameterAnnotation.converter();
      }
    }
    return null;
  }
}
