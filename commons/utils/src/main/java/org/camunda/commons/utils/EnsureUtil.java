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
package org.camunda.commons.utils;

/**
 * @author Stefan Hentschel.
 */
public class EnsureUtil {

  private static final EnsureUtilLogger LOG = UtilsLogger.ENSURE_UTIL_LOGGER;

  /**
   * Ensures that the parameter is not null.
   *
   * @param parameterName the parameter name
   * @param value the value to ensure to be not null
   * @throws IllegalArgumentException if the parameter value is null
   */
  public static void ensureNotNull(String parameterName, Object value) {
    if(value == null) {
      throw LOG.parameterIsNullException(parameterName);
    }
  }

  /**
   * Ensure the object is of a given type and return the casted object
   *
   * @param objectName the name of the parameter
   * @param object the parameter value
   * @param type the expected type
   * @return the parameter casted to the requested type
   * @throws IllegalArgumentException in case object cannot be casted to type
   */
  @SuppressWarnings("unchecked")
  public static <T> T ensureParamInstanceOf(String objectName, Object object, Class<T> type) {
    if(type.isAssignableFrom(object.getClass())) {
      return (T) object;
    } else {
      throw LOG.unsupportedParameterType(objectName, object, type);
    }
  }
}
