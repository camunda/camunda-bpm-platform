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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.camunda.spin.json.SpinJson;
import org.camunda.spin.json.SpinJsonDataFormat;
import org.camunda.spin.xml.SpinXml;
import org.camunda.spin.xml.SpinXmlDataFormat;

/**
 * Provides access to all builtin dataformats.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public class DataFormats {

  /** the collection of builtin dataformats */
  private static Collection<DataFormat<?>> BUILTIN_DATAFORMATS;

  /** map containing all dataformats: the list of builtin dataformats
   * unified with additional dataformats discovered at runtime.
   */
  private static Map<String, DataFormat<?>> DATAFORMAT_MAP;

  static {
    BUILTIN_DATAFORMATS = Arrays.asList(new DataFormat<?>[] {
        SpinXmlDataFormat.INSTANCE,
        SpinXmlDataFormat.INSTANCE
    });
  }

  public static DataFormat<?> list() {

  }

  public static DataFormat<?> dataformat(String formatName) {
    return DATAFORMAT_MAP.get(key)
  }

  public static DataFormat<SpinXml> xml() {
    return SpinXmlDataFormat.INSTANCE;
  }

  public static DataFormat<SpinJson> json() {
    return SpinJsonDataFormat.INSTANCE;
  }

}
