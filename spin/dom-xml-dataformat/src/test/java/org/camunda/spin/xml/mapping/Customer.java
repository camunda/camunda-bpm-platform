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
package org.camunda.spin.xml.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stefan Hentschel.
 */
@XmlRootElement(namespace = "http://camunda.org/example")
public class Customer {

  private String id;
  private String name;
  private int contractStartDate;

  // customer id attribute
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  // <name>
  @XmlElement(namespace = "http://camunda.org/example")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  // <contractStartDate>
  @XmlElement(namespace = "http://camunda.org/example")
  public int getContractStartDate() {
    return contractStartDate;
  }

  public void setContractStartDate(int contractStartDate) {
    this.contractStartDate = contractStartDate;
  }
}
