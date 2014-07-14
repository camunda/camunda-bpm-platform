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

import java.io.IOException;
import org.camunda.spin.SpinFileNotFoundException;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.SpinScriptException;
import org.camunda.spin.spi.SpinDataFormatException;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.camunda.spin.xml.tree.SpinXmlTreeElementException;


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

  public SpinFileNotFoundException fileNotFoundException(String filename, Throwable cause) {
    return new SpinFileNotFoundException(exceptionMessage("004", "Unable to find file with path '{}'", filename), cause);
  }

  public SpinFileNotFoundException fileNotFoundException(String filename) {
    return fileNotFoundException(filename, null);
  }

  public SpinRuntimeException unableToReadInputStream(Exception e) {
    return new SpinRuntimeException(exceptionMessage("006", "Unable to read input stream"), e);
  }

  public SpinDataFormatException wrongDataFormatException(String requestedDataFormat, String givenDataFormat) {
    return new SpinDataFormatException(exceptionMessage("007", "Wrong data format: requested '{}', given '{}'", requestedDataFormat, givenDataFormat));
  }
  
  public SpinDataFormatException unknownDataFormatException(String givenDataFormat) {
    return new SpinDataFormatException(exceptionMessage("008", "Unknown data format: given '{}'", givenDataFormat));
  }
  
  public SpinDataFormatException unrecognizableDataFormatException() {
    return new SpinDataFormatException(exceptionMessage("009", "No matching data format detected"));
  }

  public SpinXmlTreeElementException elementIsNotChildOfThisElement(SpinXmlTreeElement existingChildElement, SpinXmlTreeElement parentDomElement) {
    return new SpinXmlTreeElementException(exceptionMessage("010", "The element with namespace '{}' and name '{}' " +
        "is not a child element of the element with namespace '{}' and name '{}'",
      existingChildElement.namespace(), existingChildElement.name(),
      parentDomElement.namespace(), parentDomElement.name()
    ));
  }

  public IllegalArgumentException unsupportedParameterType(String parameterName, Object param, Class<?> expectedType) {
    return new IllegalArgumentException(exceptionMessage("011", "Unsupported parameter '{}' of type '{}'. Expected type '{}'.", parameterName, param.getClass(), expectedType.getName()));
  }

  public SpinScriptException noScriptEnvFoundForLanguage(String scriptLanguage, String path) {
    return new SpinScriptException(exceptionMessage("012", "No script script env found for script language '{}' at path '{}'", scriptLanguage, path));
  }

  public IOException unableToRewindInputStream() {
    return new IOException(exceptionMessage("013", "Unable to rewind input stream: rewind buffering limit exceeded"));
  }
}
