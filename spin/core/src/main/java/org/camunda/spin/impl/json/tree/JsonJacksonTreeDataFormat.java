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
package org.camunda.spin.impl.json.tree;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.Configurable;
import org.camunda.spin.spi.DataFormat;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeDataFormat implements DataFormat<SpinJsonNode> {

  public static final JsonJacksonTreeDataFormat INSTANCE = new JsonJacksonTreeDataFormat();
  
  protected JsonJacksonTreeConfiguration configuration;
  
  public JsonJacksonTreeDataFormat() {
    this.configuration = new JsonJacksonTreeConfiguration();
  }
  
  public Class<? extends SpinJsonNode> getWrapperType() {
    return SpinJsonJacksonTreeNode.class;
  }

  public SpinJsonNode createWrapperInstance(Object parameter) {
    return new SpinJsonJacksonTreeNode((JsonNode) parameter);
  }

  public String getName() {
    return "application/json; implementation=tree";
  }
  
  // configuration


  public JsonJacksonTreeDataFormatInstance newInstance() {
    return new JsonJacksonTreeDataFormatInstance(configuration.getConfiguration(), this);
  }

  public Configurable<?> getConfiguration() {
    return configuration;
  }

}
