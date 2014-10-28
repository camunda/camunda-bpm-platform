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
package org.camunda.spin.spi;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;

/**
 * @author Thorben Lindhauer
 *
 */
public class OverrideBuiltinJsonDataFormatProvider implements DataFormatProvider {

  public static final JacksonJsonDataFormat DATA_FORMAT = new JacksonJsonDataFormat();

  @Override
  public String getDataFormatName() {
    return DataFormats.JSON_DATAFORMAT_NAME;
  }

  @Override
  public DataFormat<?> createInstance() {
    return DATA_FORMAT;
  }

}
