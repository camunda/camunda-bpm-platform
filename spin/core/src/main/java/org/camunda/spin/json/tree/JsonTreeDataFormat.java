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
package org.camunda.spin.json.tree;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Thorben Lindhauer
 */
public class JsonTreeDataFormat implements DataFormat<SpinJsonNode> {

  public static final JsonTreeDataFormat INSTANCE = new JsonTreeDataFormat();

  public Class<? extends SpinJsonNode> getWrapperType() {
    return SpinJsonTreeNode.class;
  }

  public SpinJsonNode createWrapperInstance(Object parameter) {
    return new SpinJsonTreeNode((JsonNode) parameter);
  }

  public DataFormatReader getReader() {
    return new JsonTreeDataFormatReader();
  }

  public String getName() {
    return "application/json; implementation=tree";
  }

}
