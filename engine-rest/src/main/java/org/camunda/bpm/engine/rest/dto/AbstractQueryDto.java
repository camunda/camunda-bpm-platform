/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.converter.JacksonAwareStringToTypeConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringToTypeConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Defines common query operations, such as sorting options and validation.
 * Also allows to access its setter methods based on {@link CamundaQueryParam} annotations which is
 * used for processing Http query parameters.
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractQueryDto<T extends Query<?, ?>> {

  protected static final String SORT_ORDER_ASC_VALUE = "asc";
  protected static final String SORT_ORDER_DESC_VALUE = "desc";

  private static final List<String> VALID_SORT_ORDER_VALUES;
  static {
    VALID_SORT_ORDER_VALUES = new ArrayList<String>();
    VALID_SORT_ORDER_VALUES.add(SORT_ORDER_ASC_VALUE);
    VALID_SORT_ORDER_VALUES.add(SORT_ORDER_DESC_VALUE);
  }

  protected ObjectMapper objectMapper;

  protected String sortBy;
  protected String sortOrder;

  protected Map<String, String> expressions = new HashMap<String, String>();

  // required for populating via jackson
  public AbstractQueryDto() {

  }

  public AbstractQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    this.objectMapper = objectMapper;
    for (Entry<String, List<String>> param : queryParameters.entrySet()) {
      String key = param.getKey();
      String value = param.getValue().iterator().next();
      this.setValueBasedOnAnnotation(key, value);
    }
  }

  // note: with Jackson version >= 1.9, it would be better to use @JacksonInject and
  // configure the object mapper in the JacksonConfigurator class to be an injectable value.
  // then, explicitly calling this method with every query is not necessary any longer
  @JsonIgnore
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @CamundaQueryParam("sortBy")
  public void setSortBy(String sortBy) {
    if (!isValidSortByValue(sortBy)) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "sortBy parameter has invalid value: " + sortBy);
    }
    this.sortBy = sortBy;
  }

  @CamundaQueryParam("sortOrder")
  public void setSortOrder(String sortOrder) {
    if (!VALID_SORT_ORDER_VALUES.contains(sortOrder)) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "sortOrder parameter has invalid value: " + sortOrder);
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
   */
  protected void setValueBasedOnAnnotation(String key, String value) {
    List<Method> matchingMethods = findMatchingAnnotatedMethods(key);
    for (Method method : matchingMethods) {
      Class<? extends JacksonAwareStringToTypeConverter<?>> converterClass = findAnnotatedTypeConverter(method);
      if (converterClass == null) {
        continue;
      }

      JacksonAwareStringToTypeConverter<?> converter = null;
      try {
        converter = converterClass.newInstance();
        converter.setObjectMapper(objectMapper);
        Object convertedValue = converter.convertQueryParameterToType(value);
        method.invoke(this, convertedValue);
      } catch (InstantiationException e) {
        throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Server error.");
      } catch (IllegalAccessException e) {
        throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Server error.");
      } catch (InvocationTargetException e) {
        throw new InvalidRequestException(Status.BAD_REQUEST, e, "Cannot set query parameter '" + key + "' to value '" + value + "'");
      } catch (RestException e) {
        throw new InvalidRequestException(e.getStatus(), e,
            "Cannot set query parameter '" + key + "' to value '" + value + "': " + e.getMessage());
      }
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

  private Class<? extends JacksonAwareStringToTypeConverter<?>> findAnnotatedTypeConverter(Method method) {
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

  public T toQuery(ProcessEngine engine) {
    T query = createNewQuery(engine);
    applyFilters(query);

    if (!sortOptionsValid()) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only a single sorting parameter specified. sortBy and sortOrder required");
    }

    applySortingOptions(query);

    return query;
  }

  protected abstract T createNewQuery(ProcessEngine engine);

  protected abstract void applyFilters(T query);

  protected abstract void applySortingOptions(T query);
}
