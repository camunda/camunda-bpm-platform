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

import org.camunda.spin.json.SpinJson;
import org.camunda.spin.json.SpinJsonDataFormat;
import org.camunda.spin.xml.SpinXml;
import org.camunda.spin.xml.SpinXmlDataFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Provides access to all builtin dataformats.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public class DataFormats {

  /** the collection of builtin data formats */
  private static Collection<DataFormat<?>> BUILTIN_DATA_FORMATS;

  static {
    BUILTIN_DATA_FORMATS = Arrays.asList(new DataFormat<?>[] {
      SpinXmlDataFormat.INSTANCE,
      SpinXmlDataFormat.INSTANCE
    });
  }

  /** map containing all data formats: the list of builtin data formats
   * unified with additional data formats discovered at runtime.
   */
  private static Map<String, DataFormat<?>> DATA_FORMATS;

  public static Collection<DataFormat<?>> list() {
    return Collections.emptyList();
  }

  public static DataFormat<?> dataFormat(String formatName) {
    return DATA_FORMATS.get(formatName);
  }

  public static DataFormat<SpinXml> xml() {
    return SpinXmlDataFormat.INSTANCE;
  }

  public static DataFormat<SpinJson> json() {
    return SpinJsonDataFormat.INSTANCE;
  }

}
