/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.variables;


public class SimpleBean {

  private String stringProperty;

  private int intProperty;

  private boolean booleanProperty;

  public SimpleBean() {

  }

  public SimpleBean(String stringProperty, int intProperty, boolean booleanProperty) {
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

  public String toExpectedJsonString() {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("{\"stringProperty\":\"");
    jsonBuilder.append(stringProperty);
    jsonBuilder.append("\",\"intProperty\":");
    jsonBuilder.append(intProperty);
    jsonBuilder.append(",\"booleanProperty\":");
    jsonBuilder.append(booleanProperty);
    jsonBuilder.append("}");

    return jsonBuilder.toString();
  }

  public String toString() {
    return toExpectedJsonString();
  }

}
