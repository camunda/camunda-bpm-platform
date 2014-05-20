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
package org.camunda.spin.logging;

import org.camunda.spin.SpinFileNotFoundException;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.spi.SpinDataFormatException;


/**
 * The Logger for the core api.
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinCoreLogger extends SpinLogger {

  public IllegalArgumentException parameterIsNullException(String parameterName) {
    return new IllegalArgumentException(exceptionMessage("001", "Parameter '{}' is null", parameterName));
  }

  public SpinRuntimeException unableToInstantiateClass(String className, Exception cause) {
    return new SpinRuntimeException(exceptionMessage("002", "Unable to instantiate class '{}'", className), cause);
  }

  public IllegalArgumentException unsupportedInputParameter(Class<?> parameterClass) {
    return new IllegalArgumentException(exceptionMessage("003", "Unsupported input of type '{}'", parameterClass.getName()));
  }

  public IllegalArgumentException unsupportedNullInputParameter() {
    return new IllegalArgumentException(exceptionMessage("005", "Unsupported input: input is 'null'"));
  }

  public SpinFileNotFoundException fileNotFoundException(String filename, Throwable cause) {
    return new SpinFileNotFoundException(exceptionMessage("004", "Unable to find file with path '{}'", filename), cause);
  }

  public SpinFileNotFoundException fileNotFoundException(String filename) {
    return fileNotFoundException(filename, null);
  }

  public SpinRuntimeException unableToReadInputStream(Exception e) {
    return new SpinRuntimeException("Unable to read input stream", e);
  }

  public SpinDataFormatException wrongDataFormatException(String requestedDataformat, String givenDataformat) {
    return new SpinDataFormatException(exceptionMessage("Wrong data format: requested '{}', given '{}'", requestedDataformat, givenDataformat));
  }

}
