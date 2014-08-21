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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author Stefan Hentschel.
 */
@XmlRootElement(name = "customers", namespace = "http://camunda.org/example")
public class Order {
  private String order;
  private Date dueUntil;
  private String date;
  private LinkedList<Customer> customer;
  private OrderDetails orderDetails;

  // <order-details>
  @XmlElement(name = "order-details", namespace = "http://camunda.org/example")
  public OrderDetails getOrderDetails() {
    return orderDetails;
  }

  public void setOrderDetails(OrderDetails orderDetails) {
    this.orderDetails = orderDetails;
  }


  // Due until attribute
  @XmlAttribute(name = "dueUntil")
  @XmlJavaTypeAdapter(DateAdapter.class)
  public Date getDueUntil() {
    return dueUntil;
  }

  public void setDueUntil(Date dueUntil) {
    this.dueUntil = dueUntil;
  }


  // order attribute
  @XmlAttribute(name = "order")
  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }


  // <customer tags>
  @XmlElement(namespace = "http://camunda.org/example")
  public LinkedList<Customer> getCustomer() {
    return customer;
  }

  public void setCustomer(LinkedList<Customer> customer) {
    this.customer = customer;
  }
}
