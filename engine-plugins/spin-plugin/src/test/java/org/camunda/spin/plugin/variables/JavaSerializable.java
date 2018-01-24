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

package org.camunda.spin.plugin.variables;

import java.io.Serializable;

public class JavaSerializable implements Serializable {

  private String stringProperty;

  private int intProperty;

  private boolean booleanProperty;

  public JavaSerializable() {

  }

  public JavaSerializable(String stringProperty, int intProperty, boolean booleanProperty) {
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
    JavaSerializable other = (JavaSerializable) obj;
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
