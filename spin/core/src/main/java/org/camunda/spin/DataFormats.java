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

import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatConfigurator;
import org.camunda.spin.spi.DataFormatProvider;
import org.camunda.spin.xml.SpinXmlElement;

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
  @SuppressWarnings("unchecked")
  public static DataFormat<SpinXmlElement> xml() {
    return (DataFormat<SpinXmlElement>) getDataFormat(XML_DATAFORMAT_NAME);
  }

  /**
   * Returns the global json data format that can be provided with
   * configuration that applies to any Spin json operation.
   */
  @SuppressWarnings("unchecked")
  public static DataFormat<SpinJsonNode> json() {
    return (DataFormat<SpinJsonNode>) getDataFormat(JSON_DATAFORMAT_NAME);
  }

  /**
   * Returns the registered data format for the given name.
   *
   * @param dataFormatName the name of the data format
   * @return the data format or null if non is registered for this name
   */
  public static DataFormat<? extends Spin<?>> getDataFormat(String dataFormatName) {
    return INSTANCE.getDataFormatByName(dataFormatName);
  }

  /**
   * @return a set of all registered data formats
   */
  public static Set<DataFormat<? extends Spin<?>>> getAvailableDataFormats() {
    return INSTANCE.getAllAvailableDataFormats();
  }

  // instance /////////////////////////////////////////////////////

  protected Map<String, DataFormat<?>> availableDataFormats;

  public Set<DataFormat<? extends Spin<?>>> getAllAvailableDataFormats() {
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
          registerDataFormats(null);
        }
      }
    }
  }

  public void registerDataFormats(ClassLoader classloader) {
    Map<String, DataFormat<?>> dataFormats = new HashMap<String, DataFormat<?>>();

    if(classloader == null) {
      classloader = DataFormats.class.getClassLoader();
    }

    // discover available custom dataformat providers on the classpath
    registerCustomDataFormats(dataFormats, classloader);

    // discover and apply data format configurators on the classpath
    applyConfigurators(dataFormats, classloader);

    LOG.logDataFormats(dataFormats.values());

    this.availableDataFormats = dataFormats;
  }

  protected void registerCustomDataFormats(Map<String, DataFormat<?>> dataFormats, ClassLoader classloader) {
    // use java.util.ServiceLoader to load custom DataFormatProvider instances on the classpath
    ServiceLoader<DataFormatProvider> providerLoader = ServiceLoader.load(DataFormatProvider.class, classloader);

    for (DataFormatProvider provider : providerLoader) {
      LOG.logDataFormatProvider(provider);
      registerProvider(dataFormats, provider);
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

  @SuppressWarnings("rawtypes")
  protected void applyConfigurators(Map<String, DataFormat<?>> dataFormats, ClassLoader classloader) {
    ServiceLoader<DataFormatConfigurator> configuratorLoader = ServiceLoader.load(DataFormatConfigurator.class, classloader);

    for (DataFormatConfigurator configurator : configuratorLoader) {
      LOG.logDataFormatConfigurator(configurator);
      applyConfigurator(dataFormats, configurator);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void applyConfigurator(Map<String, DataFormat<?>> dataFormats, DataFormatConfigurator configurator) {
    for (DataFormat<?> dataFormat : dataFormats.values()) {
      if (configurator.getDataFormatClass().isAssignableFrom(dataFormat.getClass())) {
        configurator.configure(dataFormat);
      }
    }
  }

  public static void loadDataFormats() {
    loadDataFormats(null);
  }

  public static void loadDataFormats(ClassLoader classloader) {
    INSTANCE.registerDataFormats(classloader);
  }

}
