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

import java.io.InputStream;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public interface DataFormatReader {

  /**
   * Returns true if this reader estimates to be able to consume the input data
   *
   * @param firstBytes first bytes of an input
   * @return true if this reader is able to consume the input
   */
  boolean canRead(byte[] firstBytes);

  /**
   * Read (or parse) an input stream into this data-formats input structure. An Xml-Based data format may return an
   * internal tree structure which can be used for traversing the xml tree.
   *
   * @param input an {@link InputStream} providing the data source
   * @return the read or parsed input
   * @throws SpinDataFormatException in case the reader cannot read the input
   */
  Object readInput(InputStream input);

}
