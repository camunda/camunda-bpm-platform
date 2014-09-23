/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;

/**
 * @author Sebastian Menski
 * @author Roman Smirnov
 */
public final class EnsureUtil {

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

  public static void ensurePositive(String variableName, Integer value) {
    ensurePositive("", variableName, value);
  }

  public static void ensurePositive(Class<? extends ProcessEngineException> exceptionClass, String variableName, Integer value) {
    ensurePositive(exceptionClass, null, variableName, value);
  }

  public static void ensurePositive(String message, String variableName, Integer value) {
    ensurePositive(ProcessEngineException.class, message, variableName, value);
  }

  public static void ensurePositive(Class<? extends ProcessEngineException> exceptionClass, String message, String variableName, Integer value) {
    ensureNotNull(exceptionClass, variableName, value);
    if (value <= 0) {
      throw generateException(exceptionClass, message, variableName, "is not positive");
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

  protected static <T extends ProcessEngineException> T generateException(Class<T> exceptionClass, String message, String variableName, String description) {
    String formattedMessage = formatMessage(message, variableName, description);

    try {
      Constructor<T> constructor = exceptionClass.getConstructor(String.class);

      return constructor.newInstance(formattedMessage);

    } catch (Exception e) {
      throw new ProcessEngineException("Couldn't instantiate class " + exceptionClass.getName(), e);
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

}
