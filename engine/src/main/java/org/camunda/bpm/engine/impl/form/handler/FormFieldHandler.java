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
package org.camunda.bpm.engine.impl.form.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.impl.el.StartProcessVariableScope;
import org.camunda.bpm.engine.impl.form.FormDataImpl;
import org.camunda.bpm.engine.impl.form.FormFieldImpl;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class FormFieldHandler {

  protected String id;
  protected Expression label;
  protected AbstractFormFieldType type;
  protected Expression defaultValueExpression;
  protected Map<String, String> properties = new HashMap<String, String>();
  protected List<FormFieldValidationConstraintHandler> validationHandlers = new ArrayList<FormFieldValidationConstraintHandler>();
  protected boolean businessKey;

  public FormField createFormField(ExecutionEntity executionEntity) {
    FormFieldImpl formField = new FormFieldImpl();

    // set id
    formField.setId(id);

    // set label (evaluate expression)
    VariableScope variableScope = executionEntity != null ? executionEntity : StartProcessVariableScope.getSharedInstance();
    if (label != null) {
      Object labelValueObject = label.getValue(variableScope);
      if(labelValueObject != null) {
        formField.setLabel(labelValueObject.toString());
      }
    }

    formField.setBusinessKey(businessKey);

    // set type
    formField.setType(type);

    // set default value (evaluate expression)
    Object defaultValue = null;
    if(defaultValueExpression != null) {
      defaultValue = defaultValueExpression.getValue(variableScope);

      if(defaultValue != null) {
        formField.setDefaultValue(type.convertFormValueToModelValue(defaultValue));
      } else {
        formField.setDefaultValue(null);
      }
    }

    // value
    TypedValue value = variableScope.getVariableTyped(id);
    if(value != null) {
      formField.setValue(type.convertToFormValue(value));
    }
    else {
      // first, need to convert to model value since the default value may be a String Constant specified in the model xml.
      TypedValue typedDefaultValue = type.convertToModelValue(Variables.untypedValue(defaultValue));
      // now convert to form value
      formField.setValue(type.convertToFormValue(typedDefaultValue));
    }

    // properties
    formField.setProperties(properties);

    // validation
    List<FormFieldValidationConstraint> validationConstraints = formField.getValidationConstraints();
    for (FormFieldValidationConstraintHandler validationHandler : validationHandlers) {
      // do not add custom validators
      if(!"validator".equals(validationHandler.name)) {
        validationConstraints.add(validationHandler.createValidationConstraint(executionEntity));
      }
    }

    return formField;
  }

  // submit /////////////////////////////////////////////

  public void handleSubmit(VariableScope variableScope, VariableMap values, VariableMap allValues) {
    TypedValue submittedValue = (TypedValue) values.getValueTyped(id);
    values.remove(id);

    // perform validation
    for (FormFieldValidationConstraintHandler validationHandler : validationHandlers) {
      Object value = null;
      if(submittedValue != null) {
        value = submittedValue.getValue();
      }
      validationHandler.validate(value, allValues, this, variableScope);
    }

    // update variable(s)
    TypedValue modelValue = null;
    if (submittedValue != null) {
      if (type != null) {
        modelValue = type.convertToModelValue(submittedValue);
      }
      else {
        modelValue = submittedValue;
      }
    }
    else if (defaultValueExpression != null) {
      final TypedValue expressionValue = Variables.untypedValue(defaultValueExpression.getValue(variableScope));
      if (type != null) {
        // first, need to convert to model value since the default value may be a String Constant specified in the model xml.
        modelValue = type.convertToModelValue(Variables.untypedValue(expressionValue));
      }
      else if (expressionValue != null) {
        modelValue = Variables.stringValue(expressionValue.getValue().toString());
      }
    }

    if (modelValue != null) {
      if (id != null) {
        variableScope.setVariable(id, modelValue);
      }
    }
  }

  // getters / setters //////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Expression getLabel() {
    return label;
  }

  public void setLabel(Expression name) {
    this.label = name;
  }

  public void setType(AbstractFormFieldType formType) {
    this.type = formType;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public FormType getType() {
    return type;
  }

  public Expression getDefaultValueExpression() {
    return defaultValueExpression;
  }

  public void setDefaultValueExpression(Expression defaultValue) {
    this.defaultValueExpression = defaultValue;
  }

  public List<FormFieldValidationConstraintHandler> getValidationHandlers() {
    return validationHandlers;
  }

  public void setValidationHandlers(List<FormFieldValidationConstraintHandler> validationHandlers) {
    this.validationHandlers = validationHandlers;
  }

  public void setBusinessKey(boolean businessKey) {
    this.businessKey = businessKey;
  }

  public boolean isBusinessKey() {
    return businessKey;
  }
}
