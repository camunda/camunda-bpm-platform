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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.logging.SpinCoreLogger;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatInstance;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.SpinDataFormatException;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  private final static SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;
  
  private final static int READ_SIZE = 16;

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */

  public <T extends Spin<?>> T createSpin(T parameter) {
    ensureParameterNotNull(parameter);
    
    return parameter;
  }
  
  public <T extends Spin<?>> T createSpin(String parameter) {
    ensureParameterNotNull(parameter);
    
    InputStream input = stringAsInputStream(parameter);
    return createSpin(input);
  }
  
  public <T extends Spin<?>> T createSpin(InputStream parameter) {
    ensureParameterNotNull(parameter);
    
    PushbackInputStream backUpStream = new PushbackInputStream(parameter, READ_SIZE);
    byte[] firstBytes = IoUtil.readFirstBytes(backUpStream, READ_SIZE);
    
    DataFormatInstance<T> matchingDataFormat = null;
    for (DataFormat<?> format : DataFormats.AVAILABLE_FORMATS) {
      DataFormatInstance<?> instance = format.newInstance();
      if (instance.getReader().canRead(firstBytes)) {
        matchingDataFormat = (DataFormatInstance<T>) instance;
      }
    }
    
    if (matchingDataFormat == null) {
      throw LOG.unrecognizableDataFormatException();
    }
    
    try {
      backUpStream.unread(firstBytes);
    } catch (IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
    
    return createSpin(backUpStream, matchingDataFormat);
  }

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */
  public <T extends Spin<?>> T createSpin(T parameter, DataFormatInstance<T> format) {

    ensureParameterNotNull(parameter);
    
    return parameter;
  }

  public <T extends Spin<?>> T createSpin(String parameter, DataFormatInstance<T> format) {
    ensureParameterNotNull(parameter);
    
    InputStream input = stringAsInputStream(parameter);
    return createSpin(input, format);
  }

  public <T extends Spin<?>> T createSpin(InputStream parameter, DataFormatInstance<T> format) {
    ensureParameterNotNull(parameter);
    
    DataFormatReader reader = format.getReader();
    Object dataFormatInput = reader.readInput(parameter);
    return format.getDataFormat().createWrapperInstance(dataFormatInput);
  }
  
  protected void ensureParameterNotNull(Object parameter) {
    if(parameter == null) {
      throw LOG.unsupportedNullInputParameter();
    }
  }

}
