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
package org.camunda.bpm.engine.test.api.runtime.util;

import java.io.Serializable;

public class SimpleSerializableBean implements Serializable {

  private static final long serialVersionUID = 1L;

  protected int intProperty;

  public SimpleSerializableBean() {

  }

  public SimpleSerializableBean(int intProperty) {
    this.intProperty = intProperty;
  }

  public int getIntProperty() {
    return intProperty;
  }

  public void setIntProperty(int intProperty) {
    this.intProperty = intProperty;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + intProperty;
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SimpleSerializableBean other = (SimpleSerializableBean) obj;
    if (intProperty != other.intProperty)
      return false;
    return true;
  }
}
