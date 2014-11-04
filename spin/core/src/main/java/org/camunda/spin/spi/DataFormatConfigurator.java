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

/**
 * <p>Can be used to configure data formats of a certain type.
 * An implementation will be supplied with all discovered data formats
 * of the specified class. {@link DataFormat#getName()} may be used to further
 * restrict configuration to data formats with a specific name.</p>
 *
 * @author Thorben Lindhauer
 */
public interface DataFormatConfigurator<T extends DataFormat<?>> {

  /**
   * @return the dataformat class this configurator can configure (including subclasses)
   */
  Class<T> getDataFormatClass();

  /**
   * Applies configuration to the desired format.
   * This method is invoked with all dataformats of the required type.
   */
  void configure(T dataFormat);
}
