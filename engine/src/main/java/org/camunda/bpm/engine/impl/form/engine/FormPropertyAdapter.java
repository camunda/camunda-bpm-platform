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
package org.camunda.bpm.engine.impl.form.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.impl.form.FormFieldValidationConstraintImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class FormPropertyAdapter implements FormField {

  protected FormProperty formProperty;
  protected List<FormFieldValidationConstraint> validationConstraints;

  public FormPropertyAdapter(FormProperty formProperty) {
    super();
    this.formProperty = formProperty;

    validationConstraints = new ArrayList<FormFieldValidationConstraint>();
    if(formProperty.isRequired()) {
      validationConstraints.add(new FormFieldValidationConstraintImpl("required", null));
    }
    if(!formProperty.isWritable()) {
      validationConstraints.add(new FormFieldValidationConstraintImpl("readonly", null));
    }
  }

  public String getId() {
    return formProperty.getId();
  }

  public String getLabel() {
    return formProperty.getName();
  }
  public FormType getType() {
    return formProperty.getType();
  }

  public String getTypeName() {
    return formProperty.getType().getName();
  }

  public Object getDefaultValue() {
    return formProperty.getValue();
  }

  public List<FormFieldValidationConstraint> getValidationConstraints() {
    return validationConstraints;
  }

  public Map<String, String> getProperties() {
    return Collections.emptyMap();
  }

  @Override
  public boolean isBusinessKey() {
    return false;
  }

  public TypedValue getDefaultValueTyped() {
    return getValue();
  }

  public TypedValue getValue() {
    return Variables.stringValue(formProperty.getValue());
  }

}
