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

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.SpinDataFormatException;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;

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
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static <T extends Spin<?>> T S(Object input, DataFormat<T> format) {
    return SPIN_FACTORY.createSpin(input, format);
  }

  /**
   *
   * @param input
   * @return
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static <T extends Spin<?>> T S(Object input) {
    return SPIN_FACTORY.createSpin(input);
  }

  /**
   *
   * @param input
   * @return
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static SpinXmlTreeElement XML(Object input) {
    return SPIN_FACTORY.createSpin(input, DataFormats.xmlDom());
  }

  /**
  *
  * @param input
  * @return
  *
  * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
  */
  public static SpinJsonNode JSON(Object input) {
    return SPIN_FACTORY.createSpin(input, DataFormats.jsonTree());
  }

  /**
  *
  * @param input
  * @param configuration
  * @return
  *
  * @throws SpinDataFormatException in case the input cannot be read as JSON
  * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
  */
  public static SpinJsonNode JSON(Object input, Map<String, Object> readerConfiguration,
      Map<String, Object> writerConfiguration, Map<String, Object> mapperConfiguration) {

    DataFormat<SpinJsonNode> configuredFormat =
        DataFormats.jsonTree()
          .reader().config(readerConfiguration)
          .writer().config(writerConfiguration)
          .mapper().config(mapperConfiguration)
        .done();

    return SPIN_FACTORY.createSpin(input, configuredFormat);
  }

  /**
  *
  * @param input
  * @param format
  * @return
  *
  * @throws SpinDataFormatException in case the input cannot be read as JSON
  * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
  */
  public static SpinJsonNode JSON(Object input, DataFormat<SpinJsonNode> format) {
    return SPIN_FACTORY.createSpin(input, format);
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

  /**
   * Maps the wrapped object to an instance of a java class.
   *
   * @param type
   * @return
   */
  public abstract <C> C mapTo(Class<C> type);

  /**
   * Maps the wrapped object to a java object.
   * The object is determined based on the configuration string
   * which is data format specific.
   *
   * @param type
   * @return
   */
  public abstract <C> C mapTo(String type);
}
