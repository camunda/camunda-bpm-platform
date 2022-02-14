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
package org.camunda.bpm.client.variable.impl;

import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class TypedValueField {

  protected Object value;
  protected String type;
  protected Map<String, Object> valueInfo;

  public Object getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getValueInfo() {
    return valueInfo;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setValueInfo(Map<String,Object> valueInfo) {
    this.valueInfo = valueInfo;
  }

  @Override
  public String toString() {
    return "TypedValueField [type=" + type + ", value=" + value + ", valueInfo=" + valueInfo + "]";
  }

}
