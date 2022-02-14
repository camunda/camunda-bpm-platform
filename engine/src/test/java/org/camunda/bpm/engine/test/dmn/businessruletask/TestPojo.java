/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TestPojo testPojo = (TestPojo) o;
    return Objects.equals(foo, testPojo.foo) &&
        Objects.equals(bar, testPojo.bar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(foo, bar);
  }

}
