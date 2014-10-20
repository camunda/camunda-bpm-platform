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
package org.camunda.spin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormatProvider;
import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormatProvider;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatProvider;

/**
 * Provides access to all builtin data formats.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public class DataFormats {

  private static SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  public static String JSON_DATAFORMAT_NAME = "application/json";

  public static String XML_DATAFORMAT_NAME = "application/xml";

  /** The global instance of the manager */
  static DataFormats INSTANCE = new DataFormats();

  /**
   * Provides the global instance of the DataFormats manager.
   * @return the global instance.
   */
  public static DataFormats getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the global xml data format that can be provided with
   * configuration that applies to any Spin xml operation.
   */
  public static DomXmlDataFormat xml() {
    return (DomXmlDataFormat) INSTANCE.getDataFormatByName(XML_DATAFORMAT_NAME);
  }

  /**
   * Returns the global json data format that can be provided with
   * configuration that applies to any Spin json operation.
   */
  public static JacksonJsonDataFormat json() {
    return (JacksonJsonDataFormat) INSTANCE.getDataFormatByName(JSON_DATAFORMAT_NAME);
  }

  // instance /////////////////////////////////////////////////////

  protected Map<String, DataFormat<?>> availableDataFormats;

  public Set<DataFormat<? extends Spin<?>>> getAvailableDataFormats() {
    ensureDataformatsInitialized();
    return new HashSet<DataFormat<? extends Spin<?>>>(availableDataFormats.values());
  }

  public DataFormat<? extends Spin<?>> getDataFormatByName(String name) {
    ensureDataformatsInitialized();
    return availableDataFormats.get(name);
  }

  /**
   * Detect all available dataformats on the classpath using a {@link ServiceLoader}.
   */
  protected void ensureDataformatsInitialized() {
    if(availableDataFormats == null) {
      synchronized(DataFormats.class) {
        if(availableDataFormats == null) {
          registerDataFormats();
        }
      }
    }
  }

  protected void registerDataFormats() {
    Map<String, DataFormat<?>> dataFormats = new HashMap<String, DataFormat<?>>();

    // discover available custom dataformat providers on the classpath
    registerCustomDataFormats(dataFormats);

    // register default providers
    registerDefaultDataFormats(dataFormats);

    this.availableDataFormats = dataFormats;
  }

  protected void registerCustomDataFormats(Map<String, DataFormat<?>> dataFormats) {
    // use java.util.ServiceLoader to load custom DataFormatProvider instances on the classpath
    ServiceLoader<DataFormatProvider> providerLoader = ServiceLoader.load(DataFormatProvider.class, Spin.class.getClassLoader());
    for (DataFormatProvider provider : providerLoader) {
      registerProvider(dataFormats, provider);
    }
  }

  protected void registerDefaultDataFormats(Map<String, DataFormat<?>> dataFormats) {
    if(!dataFormats.containsKey(JSON_DATAFORMAT_NAME)) {
      registerProvider(dataFormats, new JacksonJsonDataFormatProvider());
    }
    if(!dataFormats.containsKey(XML_DATAFORMAT_NAME)) {
      registerProvider(dataFormats, new DomXmlDataFormatProvider());
    }
  }

  protected void registerProvider(Map<String, DataFormat<?>> dataFormats, DataFormatProvider provider) {

    String dataFormatName = provider.getDataFormatName();

    if(dataFormats.containsKey(dataFormatName)) {
      throw LOG.multipleProvidersForDataformat(dataFormatName);
    }
    else {
      DataFormat<?> dataFormatInstance = provider.createInstance();
      dataFormats.put(dataFormatName, dataFormatInstance);
    }
  }

}
