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

import java.io.Writer;

/**
 * @author Daniel Meyer
 *
 */
public interface DataFormatWriter {

  /**
   * Writes the internal representation, as provided by <code>input</code>
   * to the supplied <code>writer</code> according to the data format
   * that the implementation belongs to.
   * For example, an XML data format writer writes XML.
   *
   * @param writer The writer to write the output to
   * @param input The object to write. Can be safely cast to the internal
   * format of the data format.
   */
  public void writeToWriter(Writer writer, Object input);

}
