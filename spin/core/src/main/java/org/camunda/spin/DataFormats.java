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

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.xml.dom.XmlDomDataFormat;
import org.camunda.spin.spi.DataFormat;

/**
 * Provides access to all builtin data formats.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public class DataFormats {

  protected static Set<DataFormat<? extends Spin<?>>> AVAILABLE_FORMATS;

  public static JsonJacksonTreeDataFormat DEFAULT_JSON_DATA_FORMAT;
  public static XmlDomDataFormat DEFAULT_XML_DATA_FORMAT;

  public static Set<DataFormat<? extends Spin<?>>> getAvailableDataFormats() {
    ensureDataformatsInitialized();
    return AVAILABLE_FORMATS;
  }

  /**
   * Detect all available dataformats on the classpath using a {@link ServiceLoader}.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected static void ensureDataformatsInitialized() {
    if(AVAILABLE_FORMATS == null) {
      synchronized(DataFormats.class) {
        if(AVAILABLE_FORMATS == null) {
          AVAILABLE_FORMATS = new HashSet<DataFormat<? extends Spin<?>>>();
          // find available dataformats on the classpath
          ServiceLoader<DataFormat> dataFormatLoader = ServiceLoader.load(DataFormat.class, Spin.class.getClassLoader());
          for (DataFormat dataFormat : dataFormatLoader) {

            // add to list of available data formats
            AVAILABLE_FORMATS.add(dataFormat);

            // detect default data formats
            if(dataFormat instanceof JsonJacksonTreeDataFormat) {
              DEFAULT_JSON_DATA_FORMAT = (JsonJacksonTreeDataFormat) dataFormat;

            } else if(dataFormat instanceof XmlDomDataFormat) {
              DEFAULT_XML_DATA_FORMAT = (XmlDomDataFormat) dataFormat;

            }
          }
        }
      }
    }
  }

  public static XmlDomDataFormat xmlDom() {
    ensureDataformatsInitialized();
    return DEFAULT_XML_DATA_FORMAT.newInstance();
  }

  public static JsonJacksonTreeDataFormat jsonTree() {
    ensureDataformatsInitialized();
    return DEFAULT_JSON_DATA_FORMAT.newInstance();
  }

}
