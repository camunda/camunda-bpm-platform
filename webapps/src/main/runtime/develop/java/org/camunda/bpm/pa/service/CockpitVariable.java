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
package org.camunda.bpm.pa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CockpitVariable implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String value;
  protected List<Date> dates = new ArrayList<Date>();

  public CockpitVariable() {
  }

  public CockpitVariable(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Date> getDates() {
    return dates;
  }

  public void setDates(List<Date> dates) {
    this.dates = dates;
  }

}
