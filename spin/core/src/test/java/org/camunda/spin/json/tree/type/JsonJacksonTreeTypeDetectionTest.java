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
package org.camunda.spin.json.tree.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.DataFormats.jsonTree;

import java.util.ArrayList;
import java.util.List;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.json.tree.type.AbstractJsonJacksonTypeDetector;
import org.camunda.spin.json.mapping.Customer;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.junit.Test;

public class JsonJacksonTreeTypeDetectionTest {

  @Test
  public void shouldDetectTypeFromObject() {
    RegularCustomer customer = new RegularCustomer();
    String canonicalTypeString = jsonTree().getCanonicalTypeName(customer);
    assertThat(canonicalTypeString).isEqualTo("org.camunda.spin.json.mapping.RegularCustomer");
  }
  
  @Test
  public void shouldDetectListType() {
    List<Customer> customers = new ArrayList<Customer>();
    customers.add(new RegularCustomer());
    
    String canonicalTypeString = jsonTree().getCanonicalTypeName(customers);
    assertThat(canonicalTypeString).isEqualTo("java.util.ArrayList<org.camunda.spin.json.mapping.RegularCustomer>");
  }
  
  @Test
  public void shouldDetectListTypeFromEmptyList() {
    List<RegularCustomer> customers = new ArrayList<RegularCustomer>();
    
    String canonicalTypeString = jsonTree().getCanonicalTypeName(customers);
    assertThat(canonicalTypeString).isEqualTo("java.util.ArrayList<java.lang.Object>");
  }
  
  @Test
  public void shouldAllowToAddCustomTypeDetector() {
    JsonJacksonTreeDataFormat dataFormat = jsonTree();
    dataFormat.addTypeDetector(new MyTypeDetector());
    
    assertThat(dataFormat.getCanonicalTypeName(new RegularCustomer())).isEqualTo("my-type");
  }
  
  @Test
  public void shouldHandleNullParameter() {
    try {
      jsonTree().getCanonicalTypeName(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // happy path
    }
  }
  
  @Test
  public void shouldHandleListOfLists() {
    List<List<RegularCustomer>> nestedCustomers = new ArrayList<List<RegularCustomer>>();
    List<RegularCustomer> customers = new ArrayList<RegularCustomer>();
    customers.add(new RegularCustomer());
    nestedCustomers.add(customers);
    
    String canonicalTypeString = jsonTree().getCanonicalTypeName(nestedCustomers);
    assertThat(canonicalTypeString).isEqualTo("java.util.ArrayList<java.util.ArrayList<org.camunda.spin.json.mapping.RegularCustomer>>");
  }
  
  @Test
  public void shouldPassTypeDetectorsToNewInstance() {
    JsonJacksonTreeDataFormat dataFormat = jsonTree();
    dataFormat.addTypeDetector(new MyTypeDetector());
    
    JsonJacksonTreeDataFormat newDataFormat = dataFormat.newInstance();
    
    assertThat(newDataFormat.getCanonicalTypeName(new RegularCustomer())).isEqualTo("my-type");
  }

  private class MyTypeDetector extends AbstractJsonJacksonTypeDetector {

    public boolean canHandle(Object object) {
      return true;
    }

    public String detectType(Object object) {
      return "my-type";
    }
    
  }
  
}
