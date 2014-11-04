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

import org.camunda.spin.json.SpinJsonNode;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExampleCustomDataFormat implements DataFormat<SpinJsonNode> {

  protected String name;
  protected String property = "defaultValue";

  public ExampleCustomDataFormat(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Class<? extends SpinJsonNode> getWrapperType() {
    return null;
  }

  @Override
  public SpinJsonNode createWrapperInstance(Object parameter) {
    return null;
  }

  @Override
  public DataFormatReader getReader() {
    return null;
  }

  @Override
  public DataFormatWriter getWriter() {
    return null;
  }

  @Override
  public DataFormatMapper getMapper() {
    return null;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

}
