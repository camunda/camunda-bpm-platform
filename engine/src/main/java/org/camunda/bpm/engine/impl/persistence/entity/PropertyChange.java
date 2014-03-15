/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;

/**
 * Contains data about a property change.
 *
 * @author Daniel Meyer
 * @author Danny Gräf
 *
 */
public class PropertyChange {

  /** the empty change */
  public static final PropertyChange EMPTY_CHANGE = new PropertyChange(null, null, null);

  /** the name of the property which has been changed */
  protected String propertyName;

  /** the original value */
  protected Object orgValue;

  /** the new value */
  protected Object newValue;

  public PropertyChange(String propertyName, Object orgValue, Object newValue) {
    this.propertyName = propertyName;
    this.orgValue = orgValue;
    this.newValue = newValue;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public Object getOrgValue() {
    return orgValue;
  }

  public void setOrgValue(Object orgValue) {
    this.orgValue = orgValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public void setNewValue(Object newValue) {
    this.newValue = newValue;
  }

  public String getNewValueString() {
    return valueAsString(newValue);
  }

  public String getOrgValueString() {
    return valueAsString(orgValue);
  }

  protected String valueAsString(Object value) {
    if(value == null) {
      return null;

    } else if(value instanceof Date){
      return String.valueOf(((Date)value).getTime());

    } else {
      return value.toString();

    }
  }

}
