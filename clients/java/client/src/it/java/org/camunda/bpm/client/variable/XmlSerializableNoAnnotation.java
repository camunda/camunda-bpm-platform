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
package org.camunda.bpm.client.variable;

public class XmlSerializableNoAnnotation {

  private String stringProperty;

  private int intProperty;

  private boolean booleanProperty;

  public XmlSerializableNoAnnotation() {
  }

  public XmlSerializableNoAnnotation(String stringProperty, int intProperty, boolean booleanProperty) {
    this.stringProperty = stringProperty;
    this.intProperty = intProperty;
    this.booleanProperty = booleanProperty;
  }

  public String getStringProperty() {
    return stringProperty;
  }

  public void setStringProperty(String stringProperty) {
    this.stringProperty = stringProperty;
  }

  public int getIntProperty() {
    return intProperty;
  }

  public void setIntProperty(int intProperty) {
    this.intProperty = intProperty;
  }

  public boolean getBooleanProperty() {
    return booleanProperty;
  }

  public void setBooleanProperty(boolean booleanProperty) {
    this.booleanProperty = booleanProperty;
  }

  public String toExpectedXmlString() {
    StringBuilder xmlBuilder = new StringBuilder();

    xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmlSerializableNoAnnotation><booleanProperty>");
    xmlBuilder.append(booleanProperty);
    xmlBuilder.append("</booleanProperty><intProperty>");
    xmlBuilder.append(intProperty);
    xmlBuilder.append("</intProperty><stringProperty>");
    xmlBuilder.append(stringProperty);
    xmlBuilder.append("</stringProperty></xmlSerializableNoAnnotation>");

    return xmlBuilder.toString();
  }

  public String toString() {
    return toExpectedXmlString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (booleanProperty ? 1231 : 1237);
    result = prime * result + intProperty;
    result = prime * result + ((stringProperty == null) ? 0 : stringProperty.hashCode());
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
    XmlSerializableNoAnnotation other = (XmlSerializableNoAnnotation) obj;
    if (booleanProperty != other.booleanProperty)
      return false;
    if (intProperty != other.intProperty)
      return false;
    if (stringProperty == null) {
      if (other.stringProperty != null)
        return false;
    } else if (!stringProperty.equals(other.stringProperty))
      return false;
    return true;
  }

}
