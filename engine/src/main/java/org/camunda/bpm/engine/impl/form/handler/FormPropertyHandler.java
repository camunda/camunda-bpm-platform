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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.impl.el.StartProcessVariableScope;
import org.camunda.bpm.engine.impl.form.FormPropertyImpl;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.VariableMap;


/**
 * @author Tom Baeyens
 */
public class FormPropertyHandler {

  protected String id;
  protected String name;
  protected AbstractFormFieldType type;
  protected boolean isReadable;
  protected boolean isWritable;
  protected boolean isRequired;
  protected String variableName;
  protected Expression variableExpression;
  protected Expression defaultExpression;

  public FormProperty createFormProperty(ExecutionEntity execution) {
    FormPropertyImpl formProperty = new FormPropertyImpl(this);
    Object modelValue = null;

    if (execution!=null) {
      if (variableName != null || variableExpression == null) {
        final String varName = variableName != null ? variableName : id;
        if (execution.hasVariable(varName)) {
          modelValue = execution.getVariable(varName);
        } else if (defaultExpression != null) {
          modelValue = defaultExpression.getValue(execution);
        }
      } else {
        modelValue = variableExpression.getValue(execution);
      }
    } else {
      // Execution is null, the form-property is used in a start-form. Default value
      // should be available (ACT-1028) even though no execution is available.
      if (defaultExpression != null) {
        modelValue = defaultExpression.getValue(StartProcessVariableScope.getSharedInstance());
      }
    }

    if (modelValue instanceof String) {
      formProperty.setValue((String) modelValue);
    } else if (type != null) {
      String formValue = type.convertModelValueToFormValue(modelValue);
      formProperty.setValue(formValue);
    } else if (modelValue != null) {
      formProperty.setValue(modelValue.toString());
    }

    return formProperty;
  }

  public void submitFormProperty(VariableScope variableScope, VariableMap variables) {
    if (!isWritable && variables.containsKey(id)) {
      throw new ProcessEngineException("form property '"+id+"' is not writable");
    }

    if (isRequired && !variables.containsKey(id) && defaultExpression == null) {
      throw new ProcessEngineException("form property '"+id+"' is required");
    }

    Object modelValue = null;
    if (variables.containsKey(id)) {
      final Object propertyValue = variables.remove(id);
      if (type != null) {
        modelValue = type.convertFormValueToModelValue(propertyValue);
      } else {
        modelValue = propertyValue;
      }
    } else if (defaultExpression != null) {
      final Object expressionValue = defaultExpression.getValue(variableScope);
      if (type != null && expressionValue != null) {
        modelValue = type.convertFormValueToModelValue(expressionValue.toString());
      } else if (expressionValue != null) {
        modelValue = expressionValue.toString();
      } else if (isRequired) {
        throw new ProcessEngineException("form property '"+id+"' is required");
      }
    }

    if (modelValue != null) {
      if (variableName != null) {
        variableScope.setVariable(variableName, modelValue);
      } else if (variableExpression != null) {
        variableExpression.setValue(modelValue, variableScope);
      } else {
        variableScope.setVariable(id, modelValue);
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FormType getType() {
    return type;
  }

  public void setType(AbstractFormFieldType type) {
    this.type = type;
  }

  public boolean isReadable() {
    return isReadable;
  }

  public void setReadable(boolean isReadable) {
    this.isReadable = isReadable;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public Expression getVariableExpression() {
    return variableExpression;
  }

  public void setVariableExpression(Expression variableExpression) {
    this.variableExpression = variableExpression;
  }

  public Expression getDefaultExpression() {
    return defaultExpression;
  }

  public void setDefaultExpression(Expression defaultExpression) {
    this.defaultExpression = defaultExpression;
  }

  public boolean isWritable() {
    return isWritable;
  }

  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
}
