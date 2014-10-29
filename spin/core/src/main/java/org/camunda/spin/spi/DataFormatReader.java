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

import java.io.Reader;

import org.camunda.spin.Spin;

/**
 * Reads the internal representation of a {@link Spin} object from its serialized representation.
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 * @author Thorben Lindhauer
 *
 */
public interface DataFormatReader {

  /**
   * Returns true if this reader estimates to be able to consume the input data.
   * Implementations may not read more than <code>readLimit</code>
   * bytes from the supplied reader.
   *
   * @param reader reader that can be read from to detect a data format
   * @param readLimit positive number that poses a restriction on how many characters
   * an implementation may read. Reading beyond this limit may lead to incomplete
   * input when the method {@link #readInput(Reader)} is invoked.
   * @return true if this reader is able to consume the input
   */
  boolean canRead(Reader reader, int readLimit);

  /**
   * Read (or parse) a reader into this data format's input structure.
   * For example, an Xml-Based data format may return an
   * internal tree structure which can be used for traversing the xml tree.
   *
   * @param reader a {@link Reader} providing the data source
   * @return the read or parsed input
   * @throws SpinDataFormatException in case the reader cannot read the input
   */
  Object readInput(Reader reader);

}
