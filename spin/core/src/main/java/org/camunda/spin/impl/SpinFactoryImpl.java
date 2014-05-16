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
package org.camunda.spin.impl;

import org.camunda.spin.DataFormat;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.impl.util.Service;
import org.camunda.spin.xml.SpinXml;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Meyer
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  /**
   * This method returns a {@link Spin} objects and makes a best effort to automatically detect the
   * underlying data format.
   *
   */
  public Spin<?> createSpin(Object parameter) {

    Collection<DataFormat<?>> availableDataFormats = getAvailableDataformats();

    Collection<DataFormat<?>> builtInDataFormats = DataFormats.list();
    Collection<DataFormat> additionalDataFormats = Service.getAll(DataFormat.class);



    return new SpinXml();
  }

  public Spin<?> createSpin(Object parameter, DataFormat<?> format) {
    // TODO: implement
    return new SpinXml();
  }

  /**
   * @return the list of supported data formats
   */
  protected Collection<DataFormat<?>> getAvailableDataformats() {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  public Collection<DataFormat<?>> getSupportedDataFormats() {
    // TODO: implement
    return Collections.emptyList();
  }

}
