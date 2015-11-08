/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.dmn.businessruletask;

import java.io.Serializable;

public class TestPojo implements Serializable {

  protected String foo;
  protected Double bar;

  public TestPojo(String foo, Double bar) {
    this.foo = foo;
    this.bar = bar;
  }

  public String getFoo() {
    return foo;
  }

  public Double getBar() {
    return bar;
  }

  public String toString() {
    return "TestPojo{" +
      "foo='" + foo + '\'' +
      ", bar=" + bar +
      '}';
  }

}
