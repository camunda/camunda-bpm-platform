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
package org.camunda.spin.json.tree;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

/**
 * @author Stefan Hentschel
 */
public class JsonTreeRemovePropertyTest {

  protected SpinJsonNode jsonNode;
  protected String order;
  protected String active;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
    order = "order";
    active = "active";
  }

  @Test
  public void removePropertyByName() {
    assertThat(jsonNode.hasProp(order)).isTrue();

    jsonNode.deleteProp(order);
    assertThat(jsonNode.hasProp(order)).isFalse();

  }

  @Test
  public void removePropertyByList() {
    List<String> names = new ArrayList<String>();
    names.add(order);
    names.add(active);

    assertThat(jsonNode.hasProp(names.get(0))).isTrue();
    assertThat(jsonNode.hasProp(names.get(1))).isTrue();

    jsonNode.deleteProp(names);

    assertThat(jsonNode.hasProp(names.get(0))).isFalse();
    assertThat(jsonNode.hasProp(names.get(1))).isFalse();
  }

  @Test
  public void failWhileRemovePropertyByName() {
    try {
      jsonNode.deleteProp("waldo");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void failWhileRemovePropertyByList() {
    List<String> names = new ArrayList<String>();
    names.add(active);
    names.add("waldo");
    try {
      jsonNode.deleteProp(names);
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }
}
