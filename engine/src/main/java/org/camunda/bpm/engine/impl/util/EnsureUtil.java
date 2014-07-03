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

import java.util.Collection;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * @author Sebastian Menski
 */
public final class EnsureUtil {

  public static void ensureNotNull(String variableName, Object value) {
    ensureNotNull(null, variableName, value);
  }

  public static void ensureNotNull(String message, String variableName, Object value) {
    if (value == null) {
      throw generateException(message, variableName, "is null");
    }
  }

  public static void ensureNotNull(String variableName, Object... values) {
    ensureNotNull(null, variableName, values);
  }

  public static void ensureNotNull(String message, String variableName, Object... values) {
    if(values == null) {
      throw generateException(message, variableName, "is null");
    }
    for (Object value : values) {
      if(value == null) {
        throw generateException(message, variableName, "contains null value");
      }
    }
  }

  public static void ensureNotEmpty(String variableName, String value) {
    ensureNotEmpty(null, variableName, value);
  }

  public static void ensureNotEmpty(String message, String variableName, String value) {
    ensureNotNull(message, variableName, value);
    if (value.trim().isEmpty()) {
      throw generateException(message, variableName, "is empty");
    }
  }

  public static void ensureNotEmpty(String variableName, Collection collection) {
    ensureNotEmpty(null, variableName, collection);
  }

  public static void ensureNotEmpty(String message, String variableName, Collection collection) {
    ensureNotNull(message, variableName, collection);
    if (collection.isEmpty()) {
      throw generateException(message, variableName, "is empty");
    }
  }

  public static void ensurePositive(String variableName, Integer value) {
    ensurePositive(null, variableName, value);
  }

  public static void ensurePositive(String message, String variableName, Integer value) {
    ensureNotNull(variableName, value);
    if (value <= 0) {
      throw generateException(message, variableName, "is not positive");
    }
  }

  public static void ensureInstanceOf(String variableName, Object value, Class<?> expectedClass) {
    ensureInstanceOf(null, variableName, value, expectedClass);
  }

  public static void ensureInstanceOf(String message, String variableName, Object value, Class<?> expectedClass) {
    ensureNotNull(message, variableName, value);
    Class<?> valueClass = value.getClass();
    if (!expectedClass.isAssignableFrom(valueClass)) {
      throw generateException(message, variableName, "has class " + valueClass.getName() + " and not " + expectedClass.getName());
    }
  }

  public static void ensureOnlyOneNotNull(String message, Object... values) {
    boolean oneNotNull = false;
    for (Object value : values) {
      if (value != null) {
        if (oneNotNull) {
          throw generateException(null, null, message);
        }
        oneNotNull = true;
      }
    }
    if (!oneNotNull) {
      throw generateException(null, null, message);
    }
  }

  public static void ensureAtLeastOneNotNull(String message, Object... values) {
    for (Object value : values) {
      if (value != null) {
        return;
      }
    }
    throw generateException(null, null, message);
  }

  protected static ProcessEngineException generateException(String message, String variableName, String description) {
    return new ProcessEngineException(formatMessage(message, variableName, description));
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
