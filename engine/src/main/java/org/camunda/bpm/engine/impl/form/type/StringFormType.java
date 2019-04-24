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
package org.camunda.bpm.engine.impl.form.type;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;



/**
 * @author Tom Baeyens
 */
public class StringFormType extends SimpleFormFieldType {

  public final static String TYPE_NAME = "string";

  public String getName() {
    return TYPE_NAME;
  }

  public TypedValue convertValue(TypedValue propertyValue) {
    if(propertyValue instanceof StringValue) {
      return propertyValue;
    }
    else {
      Object value = propertyValue.getValue();
      if(value == null) {
        return Variables.stringValue(null, propertyValue.isTransient());
      }
      else {
        return Variables.stringValue(value.toString(), propertyValue.isTransient());
      }
    }
  }

  // deprecated ////////////////////////////////////////////////////////////

  public Object convertFormValueToModelValue(Object propertyValue) {
    return propertyValue.toString();
  }

  public String convertModelValueToFormValue(Object modelValue) {
    return (String) modelValue;
  }
}
