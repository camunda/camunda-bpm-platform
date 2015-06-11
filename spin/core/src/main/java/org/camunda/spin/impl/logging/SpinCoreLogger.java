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
package org.camunda.spin.impl.logging;

import java.io.IOException;
import java.util.Collection;

import org.camunda.spin.SpinFileNotFoundException;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.SpinScriptException;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.camunda.spin.spi.DataFormatProvider;
import org.camunda.spin.spi.SpinDataFormatException;


/**
 * The Logger for the core api.
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinCoreLogger extends SpinLogger {

  public IllegalArgumentException unsupportedInputParameter(Class<?> parameterClass) {
    return new IllegalArgumentException(exceptionMessage("001", "Unsupported input of type '{}'", parameterClass.getName()));
  }

  public SpinFileNotFoundException fileNotFoundException(String filename, Throwable cause) {
    return new SpinFileNotFoundException(exceptionMessage("002", "Unable to find file with path '{}'", filename), cause);
  }

  public SpinFileNotFoundException fileNotFoundException(String filename) {
    return fileNotFoundException(filename, null);
  }

  public SpinRuntimeException unableToReadFromReader(Exception e) {
    return new SpinRuntimeException(exceptionMessage("003", "Unable to read from reader"), e);
  }

  public SpinDataFormatException unrecognizableDataFormatException() {
    return new SpinDataFormatException(exceptionMessage("004", "No matching data format detected"));
  }

  public SpinScriptException noScriptEnvFoundForLanguage(String scriptLanguage, String path) {
    return new SpinScriptException(exceptionMessage("006", "No script script env found for script language '{}' at path '{}'", scriptLanguage, path));
  }

  public IOException unableToRewindReader() {
    return new IOException(exceptionMessage("007", "Unable to rewind input stream: rewind buffering limit exceeded"));
  }

  public SpinDataFormatException multipleProvidersForDataformat(String dataFormatName) {
    return new SpinDataFormatException(exceptionMessage("008", "Multiple providers found for dataformat '{}'", dataFormatName));
  }

  public void logDataFormats(Collection<DataFormat<?>> formats) {
    if (isInfoEnabled()) {
      for (DataFormat<?> format : formats) {
        logDataFormat(format);
      }
    }
  }

  protected void logDataFormat(DataFormat<?> dataFormat) {
    logInfo("009", "Discovered Spin data format: {}[name = {}]", dataFormat.getClass().getName(), dataFormat.getName());
  }

  public void logDataFormatProvider(DataFormatProvider provider) {
    if (isInfoEnabled()) {
      logInfo("010", "Discovered Spin data format provider: {}[name = {}]",
          provider.getClass().getName(), provider.getDataFormatName());
    }
  }

  @SuppressWarnings("rawtypes")
  public void logDataFormatConfigurator(DataFormatConfigurator configurator) {
    if (isInfoEnabled()) {
      logInfo("011", "Discovered Spin data format configurator: {}[dataformat = {}]",
          configurator.getClass(), configurator.getDataFormatClass().getName());
    }
  }


  public SpinDataFormatException classNotFound(String classname, ClassNotFoundException cause) {
    return new SpinDataFormatException(exceptionMessage("012", "Class {} not found ", classname), cause);
  }

  public void tryLoadingClass(String classname, ClassLoader cl) {
    logDebug("013", "Try loading class '{}' using classloader '{}'.", classname, cl);
  }
}
