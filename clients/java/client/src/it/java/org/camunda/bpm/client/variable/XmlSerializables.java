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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlSerializables {

  private List<XmlSerializable> serializables;

  public XmlSerializables() {
    this.serializables = new LinkedList<>();
  }

  public XmlSerializables(List<XmlSerializable> list) {
    this.serializables = new ArrayList<>(list);
  }

  public String toExpectedXmlString() {
    StringBuilder xmlBuilder = new StringBuilder();

    xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmlSerializables>");
    serializables.forEach((s) -> {
      xmlBuilder.append("<serializable>");
      xmlBuilder.append(String.format("<booleanProperty>%s</booleanProperty>", s.getBooleanProperty()));
      xmlBuilder.append(String.format("<intProperty>%s</intProperty>", s.getIntProperty()));
      xmlBuilder.append(String.format("<stringProperty>%s</stringProperty>", s.getStringProperty()));
      xmlBuilder.append("</serializable>");
    });
    xmlBuilder.append("</xmlSerializables>");

    return xmlBuilder.toString();
  }

  public int size() {
    return serializables.size();
  }

  public XmlSerializable get(int i) {
    return serializables.get(i);
  }

  @XmlElement(name="serializable")
  public List<XmlSerializable> getSerializable() {
    return serializables;
  }

  public void setSerializable(List<XmlSerializable> serializables) {
    this.serializables = serializables;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((serializables == null) ? 0 : serializables.hashCode());
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
    XmlSerializables other = (XmlSerializables) obj;
    if (serializables == null) {
      if (other.serializables != null)
        return false;
    } else if (!serializables.equals(other.serializables))
      return false;
    return true;
  }

}
