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
package org.camunda.bpm.integrationtest.functional.spin.dataformat;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Thorben Lindhauer
 *
 */
public class JsonSerializable {

  public static final long ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

  private Date dateProperty;

  public JsonSerializable() {

  }

  public JsonSerializable(Date dateProperty) {
    this.dateProperty = dateProperty;
  }

  public Date getDateProperty() {
    return dateProperty;
  }

  public void setDateProperty(Date dateProperty) {
    this.dateProperty = dateProperty;
  }

  /**
   * Serializes the value according to the given date format
   */
  public String toExpectedJsonString(DateFormat dateFormat) {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("{\"dateProperty\":\"");
    jsonBuilder.append(dateFormat.format(dateProperty));
    jsonBuilder.append("\"}");

    return jsonBuilder.toString();
  }

  /**
   * Serializes the value as milliseconds
   */
  public String toExpectedJsonString() {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("{\"dateProperty\":");
    jsonBuilder.append(Long.toString(dateProperty.getTime()));
    jsonBuilder.append("}");

    return jsonBuilder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dateProperty == null) ? 0 : dateProperty.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JsonSerializable other = (JsonSerializable) obj;
    if (dateProperty == null) {
      if (other.dateProperty != null)
        return false;
    } else if (!dateProperty.equals(other.dateProperty))
      return false;
    return true;
  }
}
