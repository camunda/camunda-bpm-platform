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
package org.camunda.bpm.engine.impl.form.validator;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * <p>Registry for built-in {@link FormFieldValidator} implementations.</p>
 *
 * <p>Factory for {@link FormFieldValidator} instances.</p>
 *
 * @author Daniel Meyer
 *
 */
public class FormValidators {

  /** the registry of configured validators. Populated through {@link ProcessEngineConfiguration}. */
  protected Map<String, Class<? extends FormFieldValidator>> validators = new HashMap<String, Class<? extends FormFieldValidator>>();

  /**
   * factory method for creating validator instances
   *
   */
  public FormFieldValidator createValidator(Element constraint, BpmnParse bpmnParse, ExpressionManager expressionManager) {

    String name = constraint.attribute("name");
    String config = constraint.attribute("config");

    if("validator".equals(name)) {

      // custom validators

      if(config == null || config.isEmpty()) {
        bpmnParse.addError("validator configuration needs to provide either a fully " +
        		"qualified classname or an expression resolving to a custom FormFieldValidator implementation.",
        		constraint);

      } else {
        if(StringUtil.isExpression(config)) {
          // expression
          Expression validatorExpression = expressionManager.createExpression(config);
          return new DelegateFormFieldValidator(validatorExpression);
        } else {
          // classname
          return new DelegateFormFieldValidator(config);
        }
      }

    } else {

      // built-in validators

      Class<? extends FormFieldValidator> validator = validators.get(name);
      if(validator != null) {
        FormFieldValidator validatorInstance = createValidatorInstance(validator);
        return validatorInstance;

      } else {
        bpmnParse.addError("Cannot find validator implementation for name '"+name+"'.", constraint);

      }

    }

    return null;


  }

  protected FormFieldValidator createValidatorInstance(Class<? extends FormFieldValidator> validator) {
    try {
      return validator.newInstance();

    } catch (InstantiationException e) {
      throw new ProcessEngineException("Could not instantiate validator", e);

    } catch (IllegalAccessException e) {
      throw new ProcessEngineException("Could not instantiate validator", e);

    }
  }

  public void addValidator(String name, Class<? extends FormFieldValidator> validatorType) {
    validators.put(name, validatorType);
  }

  public Map<String, Class<? extends FormFieldValidator>> getValidators() {
    return validators;
  }

}
