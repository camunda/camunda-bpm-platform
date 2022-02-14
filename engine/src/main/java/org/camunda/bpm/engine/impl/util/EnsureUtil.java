/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Sebastian Menski
 * @author Roman Smirnov
 */
public final class EnsureUtil {

  private static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static void ensureNotNull(String variableName, Object value) {
    ensureNotNull("", variableName, value);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> exceptionClass, String variableName, Object value) {
    ensureNotNull(exceptionClass, null, variableName, value);
  }

  public static void ensureNotNull(String message, String variableName, Object value) {
    ensureNotNull(NullValueException.class, message, variableName, value);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Object value) {
    if (value == null) {
      throw generateException(exceptionClass, message, variableName, "is null");
    }
  }

  public static void ensureNull(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Object value) {
    if (value != null) {
      throw generateException(exceptionClass, message, variableName, "is not null");
    }
  }

  public static void ensureNotNull(String variableName, Object... values) {
    ensureNotNull("", variableName, values);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> exceptionClass, String variableName, Object... values) {
    ensureNotNull(exceptionClass, null, variableName, values);
  }

  public static void ensureNotNull(String message, String variableName, Object... values) {
    ensureNotNull(NullValueException.class, message, variableName, values);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Object... values) {
    if(values == null) {
      throw generateException(exceptionClass, message, variableName, "is null");
    }
    for (Object value : values) {
      if(value == null) {
        throw generateException(exceptionClass, message, variableName, "contains null value");
      }
    }
  }

  public static void ensureNotEmpty(String variableName, String value) {
    ensureNotEmpty("", variableName, value);
  }

  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String variableName, String value) {
    ensureNotEmpty(exceptionClass, null, variableName, value);
  }

  public static void ensureNotEmpty(String message, String variableName, String value) {
    ensureNotEmpty(ProcessEngineException.class, message, variableName, value);
  }

  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, String value) {
    ensureNotNull(exceptionClass, message, variableName, value);
    if (value.trim().isEmpty()) {
      throw generateException(exceptionClass, message, variableName, "is empty");
    }
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String variableName, Collection collection) {
    ensureNotEmpty("", variableName, collection);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String variableName, Collection collection) {
    ensureNotEmpty(exceptionClass, null, variableName, collection);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String message, String variableName, Collection collection) {
    ensureNotEmpty(ProcessEngineException.class, message, variableName, collection);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Collection collection) {
    ensureNotNull(exceptionClass, message, variableName, collection);
    if (collection.isEmpty()) {
      throw generateException(exceptionClass, message, variableName, "is empty");
    }
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String variableName, Map map) {
    ensureNotEmpty("", variableName, map);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String variableName, Map map) {
    ensureNotEmpty(exceptionClass, null, variableName, map);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String message, String variableName, Map map) {
    ensureNotEmpty(ProcessEngineException.class, message, variableName, map);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Map map) {
    ensureNotNull(exceptionClass, message, variableName, map);
    if (map.isEmpty()) {
      throw generateException(exceptionClass, message, variableName, "is empty");
    }
  }

  public static void ensureEquals(Class<? extends ProcessEngineException> exceptionClass, String variableName, long obj1, long obj2) {
    if (obj1 != obj2) {
      throw generateException(exceptionClass, "", variableName, "value differs from expected");
    }
  }

  public static void ensureEquals(String variableName, long obj1, long obj2) {
    ensureEquals(ProcessEngineException.class, variableName, obj1, obj2);
  }

  public static void ensurePositive(String variableName, Long value) {
    ensurePositive("", variableName, value);
  }

  public static void ensurePositive(Class<? extends ProcessEngineException> exceptionClass, String variableName, Long value) {
    ensurePositive(exceptionClass, null, variableName, value);
  }

  public static void ensurePositive(String message, String variableName, Long value) {
    ensurePositive(ProcessEngineException.class, message, variableName, value);
  }

  public static void ensurePositive(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Long value) {
    ensureNotNull(exceptionClass, variableName, value);
    if (value <= 0) {
      throw generateException(exceptionClass, message, variableName, "is not greater than 0");
    }
  }

  public static void ensureLessThan(String message, String variable, long value1, long value2) {
    if (value1 >= value2) {
      throw generateException(ProcessEngineException.class, message, variable, "is not less than" + value2);
    }
  }

  public static void ensureGreaterThanOrEqual(String variableName, long value1, long value2) {
    ensureGreaterThanOrEqual("", variableName, value1, value2);
  }

  public static void ensureGreaterThanOrEqual(String message, String variableName, long value1, long value2) {
    ensureGreaterThanOrEqual(ProcessEngineException.class, message, variableName, value1, value2);
  }

  public static void ensureGreaterThanOrEqual(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, long value1, long value2) {
    if (value1 < value2) {
      throw generateException(exceptionClass, message, variableName, "is not greater than or equal to " + value2);
    }
  }

  public static void ensureInstanceOf(String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf("", variableName, value, expectedClass);
  }

  public static void ensureInstanceOf(Class<? extends ProcessEngineException> exceptionClass, String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf(exceptionClass, null, variableName, value, expectedClass);
  }

  public static void ensureInstanceOf(String message, String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf(ProcessEngineException.class, message, variableName, value, expectedClass);
  }

  public static void ensureInstanceOf(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Object value, Class<?> expectedClass) {
    ensureNotNull(exceptionClass, message, variableName, value);
    Class<?> valueClass = value.getClass();
    if (!expectedClass.isAssignableFrom(valueClass)) {
      throw generateException(exceptionClass, message, variableName, "has class " + valueClass.getName() + " and not " + expectedClass.getName());
    }
  }

  public static void ensureOnlyOneNotNull(String message, Object... values) {
    ensureOnlyOneNotNull(NullValueException.class, message, values);
  }

  public static void ensureOnlyOneNotNull(Class<? extends ProcessEngineException> exceptionClass, String message, Object... values) {
    boolean oneNotNull = false;
    for (Object value : values) {
      if (value != null) {
        if (oneNotNull) {
          throw generateException(exceptionClass, null, null, message);
        }
        oneNotNull = true;
      }
    }
    if (!oneNotNull) {
      throw generateException(exceptionClass, null, null, message);
    }
  }

  public static void ensureAtLeastOneNotNull(String message, Object... values) {
    ensureAtLeastOneNotNull(NullValueException.class, message, values);
  }

  public static void ensureAtLeastOneNotNull(Class<? extends ProcessEngineException> exceptionClass, String message, Object... values) {
    for (Object value : values) {
      if (value != null) {
        return;
      }
    }
    throw generateException(exceptionClass, null, null, message);
  }

  public static void ensureAtLeastOneNotEmpty(String message, String... values) {
    ensureAtLeastOneNotEmpty(ProcessEngineException.class, message, values);
  }

  public static void ensureAtLeastOneNotEmpty(Class<? extends ProcessEngineException> exceptionClass, String message, String... values) {
    for (String value : values) {
      if (value != null && !value.isEmpty()) {
        return;
      }
    }
    throw generateException(exceptionClass, null, null, message);
  }

  public static void ensureNotContainsEmptyString(String variableName, Collection<String> values) {
    ensureNotContainsEmptyString((String) null, variableName, values);
  }

  public static void ensureNotContainsEmptyString(String message, String variableName, Collection<String> values) {
    ensureNotContainsEmptyString(NotValidException.class, message, variableName, values);
  }

  public static void ensureNotContainsEmptyString(Class<? extends ProcessEngineException> exceptionClass, String variableName, Collection<String> values) {
    ensureNotContainsEmptyString(exceptionClass, null, variableName, values);
  }

  public static void ensureNotContainsEmptyString(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Collection<String> values) {
    ensureNotNull(exceptionClass, message, variableName, values);
    for (String value : values) {
      if (value.isEmpty()) {
        throw generateException(exceptionClass, message, variableName, "contains empty string");
      }
    }
  }

  public static void ensureNotContainsNull(String variableName, Collection<?> values) {
    ensureNotContainsNull((String) null, variableName, values);
  }

  public static void ensureNotContainsNull(String message, String variableName, Collection<?> values) {
    ensureNotContainsNull(NullValueException.class, message, variableName, values);
  }

  public static void ensureNotContainsNull(Class<? extends ProcessEngineException> exceptionClass, String variableName, Collection<?> values) {
    ensureNotContainsNull(exceptionClass, null, variableName, values);
  }

  public static void ensureNotContainsNull(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Collection<?> values) {
    ensureNotNull(exceptionClass, message, variableName, values.toArray(new Object[values.size()]));
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNumberOfElements(String variableName, Collection collection, int elements) {
    ensureNumberOfElements("", variableName, collection, elements);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNumberOfElements(String message, String variableName, Collection collection, int elements) {
    ensureNumberOfElements(ProcessEngineException.class, message, variableName, collection, elements);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNumberOfElements(Class<? extends ProcessEngineException> exceptionClass, String variableName, Collection collection, int elements) {
    ensureNumberOfElements(exceptionClass, "", variableName, collection, elements);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNumberOfElements(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Collection collection, int elements) {
    ensureNotNull(exceptionClass, message, variableName, collection);
    if (collection.size() != elements) {
      throw generateException(exceptionClass, message, variableName, "does not have " + elements + " elements");
    }
  }

  public static void ensureValidIndividualResourceId(String message, String id) {
    ensureValidIndividualResourceId(ProcessEngineException.class, message, id);
  }

  public static void ensureValidIndividualResourceId(Class<? extends ProcessEngineException> exceptionClass, String message, String id) {
    ensureNotNull(exceptionClass, message, "id", id);
    if (Authorization.ANY.equals(id)) {
      throw generateException(exceptionClass, message, "id", "cannot be "
          + Authorization.ANY + ". " + Authorization.ANY + " is a reserved identifier.");
    }
  }

  public static void ensureValidIndividualResourceIds(String message, Collection<String> ids) {
    ensureValidIndividualResourceIds(ProcessEngineException.class, message, ids);
  }

  public static void ensureValidIndividualResourceIds(Class<? extends ProcessEngineException> exceptionClass, String message, Collection<String> ids) {
    ensureNotNull(exceptionClass, message, "id", ids);
    for (String id : ids) {
      ensureValidIndividualResourceId(exceptionClass, message, id);
    }
  }

  public static void ensureWhitelistedResourceId(CommandContext commandContext, String resourceType, String resourceId) {
    String resourcePattern = determineResourceWhitelistPattern(commandContext.getProcessEngineConfiguration(), resourceType);
    Pattern PATTERN = Pattern.compile(resourcePattern);

    if (!PATTERN.matcher(resourceId).matches()) {
      throw generateException(ProcessEngineException.class, resourceType + " has an invalid id", "'" + resourceId + "'", "is not a valid resource identifier.");
    }
  }


  public static void ensureTrue(String message, boolean value) {
    if (!value) {
      throw new ProcessEngineException(message);
    }
  }

  public static void ensureFalse(String message, boolean value) {
    ensureTrue(message, !value);
  }

  protected static String determineResourceWhitelistPattern(ProcessEngineConfiguration processEngineConfiguration, String resourceType) {
    String resourcePattern = null;

    if (resourceType.equals("User")) {
      resourcePattern = processEngineConfiguration.getUserResourceWhitelistPattern();
    }

    if (resourceType.equals("Group")) {
      resourcePattern =  processEngineConfiguration.getGroupResourceWhitelistPattern();
    }

    if (resourceType.equals("Tenant")) {
      resourcePattern =  processEngineConfiguration.getTenantResourceWhitelistPattern();
    }

    if (resourcePattern != null && !resourcePattern.isEmpty()) {
      return resourcePattern;
    }

    return processEngineConfiguration.getGeneralResourceWhitelistPattern();
  }

  protected static <T extends ProcessEngineException> T generateException(Class<T> exceptionClass, String message, String variableName, String description) {
    String formattedMessage = formatMessage(message, variableName, description);

    try {
      Constructor<T> constructor = exceptionClass.getConstructor(String.class);

      return constructor.newInstance(formattedMessage);

    }
    catch (Exception e) {
      throw LOG.exceptionWhileInstantiatingClass(exceptionClass.getName(), e);
    }

  }

  protected static String formatMessage(String message, String variableName, String description) {
    return formatMessageElement(message, ": ") + formatMessageElement(variableName, " ") + description;
  }

  protected static String formatMessageElement(String element, String delimiter) {
    if (element != null && !element.isEmpty()) {
      return element.concat(delimiter);
    }
    else {
      return "";
    }
  }

  public static void ensureActiveCommandContext(String operation) {
    if(Context.getCommandContext() == null) {
      throw LOG.notInsideCommandContext(operation);
    }
  }
}
