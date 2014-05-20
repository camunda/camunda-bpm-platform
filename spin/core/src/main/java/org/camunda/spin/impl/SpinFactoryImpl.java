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
package org.camunda.spin.impl;

import static org.camunda.spin.impl.util.IoUtil.stringAsInputStream;

import java.io.InputStream;

import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.logging.SpinCoreLogger;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.SpinDataFormatException;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  private final static SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> DataFormat<T> detectDataFormat(Object parameter) {
    // TODO: use parameter content to automatically detect the data format
    return (DataFormat<T>) DataFormats.xmlDom();
  }


  public <T extends Spin<?>> T createSpin(Object parameter) {
    DataFormat<T> spinDataFormat = detectDataFormat(parameter);
    return createSpin(parameter, spinDataFormat);
  }

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   */
  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(Object parameter, DataFormat<T> format) {

    InputStream input;
    if(parameter == null) {
      throw LOG.unsupportedNullInputParameter();

    } else if (format.getWrapperType().isAssignableFrom(parameter.getClass())) {
      return (T) parameter;

    } else if(parameter instanceof Spin) {
      Spin<?> spinParameter = (Spin<?>) parameter;
      throw LOG.wrongDataFormatException(format.getName(), spinParameter.getDataFormatName());

    } else if (parameter instanceof InputStream) {
      input = (InputStream) parameter;

    } else if (parameter instanceof String) {
      String stringInput = (String) parameter;
      input = stringAsInputStream(stringInput);

    } else {
      throw LOG.unsupportedInputParameter(parameter.getClass());

    }

    DataFormatReader reader = format.getReader();
    Object dataFormatInput = reader.readInput(input);
    return format.createWrapperInstance(dataFormatInput);
  }

}
