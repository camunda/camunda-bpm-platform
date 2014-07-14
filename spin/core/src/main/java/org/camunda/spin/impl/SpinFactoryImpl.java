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

import java.io.IOException;
import java.io.InputStream;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.impl.util.RewindableInputStream;
import org.camunda.spin.logging.SpinCoreLogger;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.SpinDataFormatException;

import static org.camunda.spin.impl.util.IoUtil.stringAsInputStream;
import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  private final static SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;
  
  private final static int READ_SIZE = 256;

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */

  public <T extends Spin<?>> T createSpin(T parameter) {
    ensureNotNull("parameter", parameter);
    
    return parameter;
  }
  
  public <T extends Spin<?>> T createSpin(String parameter) {
    ensureNotNull("parameter", parameter);

    InputStream input = stringAsInputStream(parameter);
    return createSpin(input);
  }

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(InputStream parameter) {
    ensureNotNull("parameter", parameter);

    RewindableInputStream rewindableStream = new RewindableInputStream(parameter, READ_SIZE);
    
    DataFormat<T> matchingDataFormat = null;
    for (DataFormat<?> format : DataFormats.AVAILABLE_FORMATS) {
      if (format.getReader().canRead(rewindableStream)) {
        matchingDataFormat = (DataFormat<T>) format;
      }
      
      try {
        rewindableStream.rewind();
      } catch (IOException e) {
        throw LOG.unableToReadInputStream(e);
      }
      
    }
    
    if (matchingDataFormat == null) {
      throw LOG.unrecognizableDataFormatException();
    }
    
    return createSpin(rewindableStream, matchingDataFormat);
  }

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */
  public <T extends Spin<?>> T createSpin(T parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    return parameter;
  }

  public <T extends Spin<?>> T createSpin(String parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    InputStream input = stringAsInputStream(parameter);
    return createSpin(input, format);
  }

  public <T extends Spin<?>> T createSpin(InputStream parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    DataFormatReader reader = format.getReader();
    Object dataFormatInput = reader.readInput(parameter);
    return format.createWrapperInstance(dataFormatInput);
  }

}
