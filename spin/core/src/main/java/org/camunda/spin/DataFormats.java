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

import org.camunda.spin.xml.SpinXml;
import org.camunda.spin.xml.SpinXmlDataFormat;

/**
 * Provides access to all builtin data formats.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public class DataFormats {

  public static DataFormat<SpinXml> xml() {
    return SpinXmlDataFormat.INSTANCE;
  }

}
