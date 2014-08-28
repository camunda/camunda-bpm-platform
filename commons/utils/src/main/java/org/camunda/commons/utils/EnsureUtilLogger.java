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
public class EnsureUtilLogger extends UtilsLogger {

  public IllegalArgumentException parameterIsNullException(String parameterName) {
    return new IllegalArgumentException(exceptionMessage("001", "Parameter '{}' is null", parameterName));
  }

  public IllegalArgumentException unsupportedParameterType(String parameterName, Object param, Class<?> expectedType) {
    return new IllegalArgumentException(exceptionMessage("002", "Unsupported parameter '{}' of type '{}'. Expected type '{}'.", parameterName, param.getClass(), expectedType.getName()));
  }
}
