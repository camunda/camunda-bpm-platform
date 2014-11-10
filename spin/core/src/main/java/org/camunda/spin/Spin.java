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

import java.io.Writer;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

/**
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public abstract class Spin<T extends Spin<?>> {

  /**
   * Creates a spin wrapper for a data input of a given data format.
   *
   * @param input the input to wrap
   * @param format the data format of the input
   * @return the spin wrapper for the input
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static <T extends Spin<?>> T S(Object input, DataFormat<T> format) {
    return SpinFactory.INSTANCE.createSpin(input, format);
  }

  /**
   * Creates a spin wrapper for a data input of a given data format.
   *
   * @param input the input to wrap
   * @param dataFormatName the data format name of the input
   * @return the spin wrapper for the input
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
 public static <T extends Spin<?>> T S(Object input, String dataFormatName) {
   return SpinFactory.INSTANCE.createSpin(input, dataFormatName);
 }

  /**
   * Creates a spin wrapper for a data input. The data format of the
   * input is auto detected.
   *
   * @param input the input to wrap
   * @return the spin wrapper for the input
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static <T extends Spin<?>> T S(Object input) {
    return SpinFactory.INSTANCE.createSpin(input);
  }

  /**
   * Creates a spin wrapper for a data input. The data format of the
   * input is assumed to be XML.
   *
   * @param input the input to wrap
   * @return the spin wrapper for the input
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static SpinXmlElement XML(Object input) {
    return SpinFactory.INSTANCE.createSpin(input, DataFormats.xml());
  }

  /**
   * Creates a spin wrapper for a data input. The data format of the
   * input is assumed to be JSON.
   *
   * @param input the input to wrap
   * @return the spin wrapper for the input
   *
   * @throws IllegalArgumentException in case an argument of illegal type is provided (such as 'null')
   */
  public static SpinJsonNode JSON(Object input) {
    return SpinFactory.INSTANCE.createSpin(input, DataFormats.json());
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
   * Writes the wrapped object to a existing writer.
   *
   * @param writer the writer to write to
   */
  public abstract void writeToWriter(Writer writer);

  /**
   * Maps the wrapped object to an instance of a java class.
   *
   * @param type the java class to map to
   * @return the mapped object
   */
  public abstract <C> C mapTo(Class<C> type);

  /**
   * Maps the wrapped object to a java object.
   * The object is determined based on the configuration string
   * which is data format specific.
   *
   * @param type the class name to map to
   * @return the mapped object
   */
  public abstract <C> C mapTo(String type);
}
