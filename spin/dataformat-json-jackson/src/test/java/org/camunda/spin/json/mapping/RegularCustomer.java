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


public class RegularCustomer implements Customer {

  private String name;
  private int contractStartDate;

  public RegularCustomer() {
  }

  public RegularCustomer(String name, int contractStartDate) {
    this.name = name;
    this.contractStartDate = contractStartDate;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getContractStartDate() {
    return contractStartDate;
  }
  public void setContractStartDate(int contractStartDate) {
    this.contractStartDate = contractStartDate;
  }

}
