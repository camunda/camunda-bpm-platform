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
package org.camunda.spin.json.mapping;

import java.util.List;

public class OrderDetails {

  private String article;
  private double price;
  private int roundedPrice;
  private List<String> currencies;
  private boolean paid;

  public String getArticle() {
    return article;
  }
  public void setArticle(String article) {
    this.article = article;
  }
  public double getPrice() {
    return price;
  }
  public void setPrice(double price) {
    this.price = price;
  }
  public int getRoundedPrice() {
    return roundedPrice;
  }
  public void setRoundedPrice(int roundedPrice) {
    this.roundedPrice = roundedPrice;
  }
  public List<String> getCurrencies() {
    return currencies;
  }
  public void setCurrencies(List<String> currencies) {
    this.currencies = currencies;
  }
  public boolean isPaid() {
    return paid;
  }
  public void setPaid(boolean paid) {
    this.paid = paid;
  }
}
