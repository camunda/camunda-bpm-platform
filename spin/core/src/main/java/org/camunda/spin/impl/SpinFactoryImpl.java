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

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.io.IOException;
import java.io.Reader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.util.RewindableReader;
import org.camunda.spin.impl.util.SpinIoUtil;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.SpinDataFormatException;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  private static final SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;

  private static final int READ_SIZE = 256;

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(Object parameter) {
    ensureNotNull("parameter", parameter);

    if (parameter instanceof String) {
      return createSpinFromString((String) parameter);

    } else if (parameter instanceof Reader) {
      return createSpinFromReader((Reader) parameter);

    } else if (parameter instanceof Spin) {
      return createSpinFromSpin((T) parameter);

    } else {
      throw LOG.unsupportedInputParameter(parameter.getClass());
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(Object parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);
    ensureNotNull("format", format);

    if (parameter instanceof String) {
      return createSpinFromString((String) parameter, format);

    } else if (parameter instanceof Reader) {
      return createSpinFromReader((Reader) parameter, format);

    } else if (parameter instanceof Spin) {
      return createSpinFromSpin((T) parameter, format);

    } else {
      return createSpinFromObject(parameter, format);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(Object parameter, String dataFormatName) {
    ensureNotNull("dataFormatName", dataFormatName);

    DataFormat<T> dataFormat = (DataFormat<T>) DataFormats.getDataFormat(dataFormatName);

    return createSpin(parameter, dataFormat);
  }

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */
  public <T extends Spin<?>> T createSpinFromSpin(T parameter) {
    ensureNotNull("parameter", parameter);

    return parameter;
  }

  public <T extends Spin<?>> T createSpinFromString(String parameter) {
    ensureNotNull("parameter", parameter);

    Reader input = SpinIoUtil.stringAsReader(parameter);
    return createSpin(input);
  }

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpinFromReader(Reader parameter) {
    ensureNotNull("parameter", parameter);

    RewindableReader rewindableReader = new RewindableReader(parameter, READ_SIZE);

    DataFormat<T> matchingDataFormat = null;
    for (DataFormat<?> format : DataFormats.getAvailableDataFormats()) {
      if (format.getReader().canRead(rewindableReader, rewindableReader.getRewindBufferSize())) {
        matchingDataFormat = (DataFormat<T>) format;
      }

      try {
        rewindableReader.rewind();
      } catch (IOException e) {
        throw LOG.unableToReadFromReader(e);
      }

    }

    if (matchingDataFormat == null) {
      throw LOG.unrecognizableDataFormatException();
    }

    return createSpin(rewindableReader, matchingDataFormat);
  }

  /**
   *
   * @throws SpinDataFormatException in case the parameter cannot be read using this data format
   * @throws IllegalArgumentException in case the parameter is null or dd:
   */
  public <T extends Spin<?>> T createSpinFromSpin(T parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    return parameter;
  }

  public <T extends Spin<?>> T createSpinFromString(String parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    Reader input = SpinIoUtil.stringAsReader(parameter);
    return createSpin(input, format);
  }

  public <T extends Spin<?>> T createSpinFromReader(Reader parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    DataFormatReader reader = format.getReader();
    Object dataFormatInput = reader.readInput(parameter);
    return format.createWrapperInstance(dataFormatInput);
  }

  public <T extends Spin<?>> T createSpinFromObject(Object parameter, DataFormat<T> format) {
    ensureNotNull("parameter", parameter);

    DataFormatMapper mapper = format.getMapper();
    Object dataFormatInput = mapper.mapJavaToInternal(parameter);

    return format.createWrapperInstance(dataFormatInput);
  }
}
