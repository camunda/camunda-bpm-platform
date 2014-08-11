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

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * @author Sebastian Menski
 * @author Roman Smirnov
 */
public final class EnsureUtil {

  public static void ensureNotNull(String variableName, Object value) {
    ensureNotNull(ProcessEngineException.class, null, variableName, value);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> cls, String variableName, Object value) {
    ensureNotNull(cls, null, variableName, value);
  }

  public static void ensureNotNull(String message, String variableName, Object value) {
    ensureNotNull(ProcessEngineException.class, message, variableName, value);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> cls, String message, String variableName, Object value) {
    if (value == null) {
      throw generateException(message, variableName, "is null", cls);
    }
  }

  public static void ensureNotNull(String variableName, Object... values) {
    ensureNotNull(ProcessEngineException.class, null, variableName, values);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> cls, String variableName, Object... values) {
    ensureNotNull(cls, null, variableName, values);
  }

  public static void ensureNotNull(String message, String variableName, Object... values) {
    ensureNotNull(ProcessEngineException.class, message, variableName, values);
  }

  public static void ensureNotNull(Class<? extends ProcessEngineException> cls, String message, String variableName, Object... values) {
    if(values == null) {
      throw generateException(message, variableName, "is null", cls);
    }
    for (Object value : values) {
      if(value == null) {
        throw generateException(message, variableName, "contains null value", cls);
      }
    }
  }

  public static void ensureNotEmpty(String variableName, String value) {
    ensureNotEmpty(null, variableName, value);
  }

  public static void ensureNotEmpty(String variableName, String value, Class<? extends ProcessEngineException> cls) {
    ensureNotEmpty(null, variableName, value, cls);
  }

  public static void ensureNotEmpty(String message, String variableName, String value) {
    ensureNotEmpty(message, variableName, value, ProcessEngineException.class);
  }

  public static void ensureNotEmpty(String message, String variableName, String value, Class<? extends ProcessEngineException> cls) {
    ensureNotNull(cls, message, variableName, value);
    if (value.trim().isEmpty()) {
      throw generateException(message, variableName, "is empty", cls);
    }
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String variableName, Collection collection) {
    ensureNotEmpty(null, variableName, collection);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String variableName, Collection collection, Class<? extends ProcessEngineException> cls) {
    ensureNotEmpty(null, variableName, collection, cls);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String message, String variableName, Collection collection) {
    ensureNotEmpty(message, variableName, collection, ProcessEngineException.class);
  }

  @SuppressWarnings("rawtypes")
  public static void ensureNotEmpty(String message, String variableName, Collection collection, Class<? extends ProcessEngineException> cls) {
    ensureNotNull(cls, message, variableName, collection);
    if (collection.isEmpty()) {
      throw generateException(message, variableName, "is empty", cls);
    }
  }

  public static void ensurePositive(String variableName, Integer value) {
    ensurePositive(null, variableName, value);
  }

  public static void ensurePositive(String variableName, Integer value, Class<? extends ProcessEngineException> cls) {
    ensurePositive(null, variableName, value, cls);
  }

  public static void ensurePositive(String message, String variableName, Integer value) {
    ensurePositive(message, variableName, value, ProcessEngineException.class);
  }

  public static void ensurePositive(String message, String variableName, Integer value, Class<? extends ProcessEngineException> cls) {
    ensureNotNull(cls, variableName, value);
    if (value <= 0) {
      throw generateException(message, variableName, "is not positive", cls);
    }
  }

  public static void ensureInstanceOf(String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf(null, variableName, value, expectedClass);
  }

  public static void ensureInstanceOf(String variableName, Object value, Class<?> expectedClass, Class<? extends ProcessEngineException> cls) {
    ensureInstanceOf(null, variableName, value, expectedClass, cls);
  }

  public static void ensureInstanceOf(String message, String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf(message, variableName, value, expectedClass, ProcessEngineException.class);
  }

  public static void ensureInstanceOf(String message, String variableName, Object value, Class<?> expectedClass, Class<? extends ProcessEngineException> cls) {
    ensureNotNull(cls, message, variableName, value);
    Class<?> valueClass = value.getClass();
    if (!expectedClass.isAssignableFrom(valueClass)) {
      throw generateException(message, variableName, "has class " + valueClass.getName() + " and not " + expectedClass.getName(), cls);
    }
  }

  public static void ensureOnlyOneNotNull(String message, Object... values) {
    ensureOnlyOneNotNull(message, ProcessEngineException.class, values);
  }

  public static void ensureOnlyOneNotNull(String message, Class<? extends ProcessEngineException> cls, Object... values) {
    boolean oneNotNull = false;
    for (Object value : values) {
      if (value != null) {
        if (oneNotNull) {
          throw generateException(null, null, message, cls);
        }
        oneNotNull = true;
      }
    }
    if (!oneNotNull) {
      throw generateException(null, null, message, cls);
    }
  }

  public static void ensureAtLeastOneNotNull(String message, Object... values) {
    ensureAtLeastOneNotNull(message, ProcessEngineException.class, values);
  }

  public static void ensureAtLeastOneNotNull(String message, Class<? extends ProcessEngineException> cls, Object... values) {
    for (Object value : values) {
      if (value != null) {
        return;
      }
    }
    throw generateException(null, null, message, cls);
  }

  protected static ProcessEngineException generateException(String message, String variableName, String description) {
    return generateException(message, variableName, description, ProcessEngineException.class);
  }

  protected static <T extends ProcessEngineException> T generateException(String message, String variableName, String description, Class<T> cls) {
    String formatedMessage = formatMessage(message, variableName, description);

    try {
      Constructor<T> constructor = cls.getConstructor(String.class);

      return constructor.newInstance(formatedMessage);

    } catch (Exception e) {
      throw new ProcessEngineException("Couldn't instantiate class " + cls.getName(), e);
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
