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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author Stefan Hentschel.
 */
@XmlRootElement(name = "order-details", namespace = "http://camunda.org/test-example")
public class OrderDetails {

  private long id;
  private Date date;
  private String product;

  @XmlElement(namespace = "http://camunda.org/test-example")
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @XmlElement(namespace = "http://camunda.org/test-example")
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @XmlElement(namespace = "http://camunda.org/test-example")
  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }
}
