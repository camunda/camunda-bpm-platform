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
package org.camunda.spin.xml;

import org.camunda.spin.Spin;
import org.camunda.spin.SpinCollection;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttribute;

/**
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 *
 */
public abstract class SpinXmlElement extends Spin<SpinXmlElement> {

  public SpinCollection<SpinXmlDomAttribute> attrs() {
    return null;
  };
}
