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
import org.camunda.spin.xml.SpinXml;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class SpinFactoryImpl extends SpinFactory {

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> DataFormat<T> detectDataFormat(Object parameter) {
    // TODO: use parameter content to automatically detect the data format
    return (DataFormat<T>) DataFormats.xml();
  }


  public <T extends Spin<?>> T createSpin(Object parameter) {
    DataFormat<T> spinDataFormat = detectDataFormat(parameter);
    return createSpin(parameter, spinDataFormat);
  }

  @SuppressWarnings("unchecked")
  public <T extends Spin<?>> T createSpin(Object parameter, DataFormat<T> format) {
    // TODO: use format to figure out which spin class to instantiate
    return (T) new SpinXml(parameter);
  }

}
