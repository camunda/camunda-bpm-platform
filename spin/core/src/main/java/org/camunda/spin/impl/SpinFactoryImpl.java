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

import java.util.Collection;

import org.camunda.spin.DataFormat;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.impl.util.Service;

/**
 * @author Daniel Meyer
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  /**
   * This method returns a {@link Spin} objects and makes a best effort to automatically detect the
   * underlying dataformat.
   *
   */
  public Spin<?> createSpin(Object parameter) {

    Collection<DataFormat<?>> availableDataformats = getAvailableDataformats();

    Collection<DataFormat<?>> builtInDataformats = DataFormats.list();
    Collection<DataFormat> additionalDataformats = Service.getAll(DataFormat.class);



    return null;
  }

  /**
   * @return the list of supported dataformats
   */
  protected Collection<DataFormat<?>> getAvailableDataformats() {
    // TODO Auto-generated method stub
    return null;
  }

}
