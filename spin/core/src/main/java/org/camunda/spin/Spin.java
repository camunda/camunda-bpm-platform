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

import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.SpinDataFormatException;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;

import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public abstract class Spin<T extends Spin<?>> {

  protected final static SpinFactory SPIN_FACTORY = SpinFactory.getInstance();

  /**
   *
   * @param input
   * @param format
   * @return
   *
   * @throws SpinDataFormatException in case the input cannot be read using this data format
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static <T extends Spin<?>> T S(Object input, DataFormat<T> format) {
    return SPIN_FACTORY.createSpin(input, format);
  }

  /**
   *
   * @param input
   * @param format
   * @return
   *
   * @throws SpinDataFormatException in case the input cannot be read using the detected data format
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  @SuppressWarnings("unchecked")
  public static <T extends Spin<?>> T S(Object parameter) {
    return (T) SPIN_FACTORY.createSpin(parameter);
  }

  /**
   *
   * @param input
   * @return
   *
   * @throws SpinDataFormatException in case the input cannot be read as XML
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static SpinXmlTreeElement XML(Object parameter) {
    return SPIN_FACTORY.createSpin(parameter, DataFormats.xmlDom());
  }

  /**
   * Provides the name of the dataformat used by this spin.
   *
   * @return the name of the dataformat used by this Spin.
   */
  public abstract String getDataFormatName();

  /**
   * Return the wrapped object. The return type of this method
   * depends on the concrete data format.
   *
   * @return the object wrapped by this wrapper.
   */
  public abstract Object unwrap();

  /**
   * Returns the wrapped object as string representation.
   *
   * @return the string representation
   */
  public abstract String toString();

  /**
   * Returns the wrapped object as output stream.
   *
   * @return the output stream
   */
  public abstract OutputStream toStream();

  /**
   * Writes the wrapped object to a existing stream.
   *
   * @param outputStream the stream to write to
   * @return the stream after the object was written
   */
  public abstract <S extends OutputStream> S writeToStream(S outputStream);

  /**
   * Writes the wrapped object to a existing writer.
   *
   * @param writer the writer to write to
   * @return the Writer after the object was written
   */
  public abstract <W extends Writer> W writeToWriter(W writer);

}
