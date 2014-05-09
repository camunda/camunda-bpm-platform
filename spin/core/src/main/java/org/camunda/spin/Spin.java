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

import org.camunda.spin.json.SpinJson;
import org.camunda.spin.xml.SpinXml;

/**
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 */
public abstract class Spin<T extends Spin<?>> {

  public static <T extends Spin<?>> T S(Object parameter) {
    return (T) new SpinXml();
  }

  public static <T extends Spin<?>> T S(Object parameter,  DataFormat<T> format) {
    return (T) new SpinXml();
  }

  public static <T extends SpinJson> T JSON(Object parameter) {
    return null;
  }

  public static <T extends SpinXml> T XML(Object parameter) {
    return (T) new SpinXml();
  }

  public static <T extends SpinXml> T XML(Object parameter,  DataFormat<T> format) {
    return (T) new SpinXml();
  }

  public <U extends Spin<?>> U as(DataFormat<U> format) {
    return (U) new SpinXml();
  }

  public Object unwrap() {
    return null;
  }

  public SpinCollection<T> queryList(String queryString) {
    return null;
  }

  public T query(String queryString) {
    return null;
  }

}
