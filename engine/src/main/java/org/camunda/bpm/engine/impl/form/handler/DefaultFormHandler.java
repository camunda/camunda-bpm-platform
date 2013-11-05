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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.form.FormDataImpl;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.impl.form.type.FormTypes;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class DefaultFormHandler implements FormHandler {

  protected Expression formKey;
  protected String deploymentId;

  protected List<FormPropertyHandler> formPropertyHandlers = new ArrayList<FormPropertyHandler>();

  protected List<FormFieldHandler> formFieldHandlers = new ArrayList<FormFieldHandler>();

  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    this.deploymentId = deployment.getId();

    ExpressionManager expressionManager = Context
        .getProcessEngineConfiguration()
        .getExpressionManager();

    String formKeyAttribute = activityElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formKey");

    if (formKeyAttribute != null) {
      this.formKey = expressionManager.createExpression(formKeyAttribute);
    }

    Element extensionElement = activityElement.element("extensionElements");
    if (extensionElement != null) {

      // provide support for deprecated form properties
      parseFormProperties(bpmnParse, expressionManager, extensionElement);

      // provide support for new form field metadata
      parseFormData(bpmnParse, expressionManager, extensionElement);
    }
  }

  protected void parseFormData(BpmnParse bpmnParse, ExpressionManager expressionManager, Element extensionElement) {
    Element formData = extensionElement.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formData");
    if(formData != null) {
      parseFormFields(formData, bpmnParse, expressionManager);
    }
  }

  protected void parseFormFields(Element formData, BpmnParse bpmnParse, ExpressionManager expressionManager) {
    // parse fields:
    List<Element> formFields = formData.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formField");
    for (Element formField : formFields) {
      parseFormField(formField, bpmnParse, expressionManager);
    }
  }

  protected void parseFormField(Element formField, BpmnParse bpmnParse, ExpressionManager expressionManager) {

    FormFieldHandler formFieldHandler = new FormFieldHandler();

    // parse Id
    String id = formField.attribute("id");
    if(id == null || id.isEmpty()) {
      bpmnParse.addError("attribute id must be set for FormFieldGroup and must have a non-empty value", formField);
    } else {
      formFieldHandler.setId(id);
    }

    // parse name
    String name = formField.attribute("label");
    if (name != null) {
      Expression nameExpression = expressionManager.createExpression(name);
      formFieldHandler.setLabel(nameExpression);
    }

    // parse properties
    parseProperties(formField, formFieldHandler, bpmnParse, expressionManager);

    // parse validation
    parseValidation(formField, formFieldHandler, bpmnParse, expressionManager);

    // parse type
    FormTypes formTypes = getFormTypes();
    AbstractFormFieldType formType = formTypes.parseFormPropertyType(formField, bpmnParse);
    formFieldHandler.setType(formType);

    // parse default value
    String defaultValue = formField.attribute("defaultValue");
    if(defaultValue != null) {
      Expression defaultValueExpression = expressionManager.createExpression(defaultValue);
      formFieldHandler.setDefaultValue(defaultValueExpression);
    }

    formFieldHandlers.add(formFieldHandler);

  }

  protected void parseProperties(Element formField, FormFieldHandler formFieldHandler, BpmnParse bpmnParse, ExpressionManager expressionManager) {

    Element propertiesElement = formField.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "properties");

    if(propertiesElement != null) {
      List<Element> propertyElements = propertiesElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "property");

      // use linked hash map to preserve item ordering as provided in XML
      Map<String, String> propertyMap = new LinkedHashMap<String, String>();
      for (Element property : propertyElements) {
        String id = property.attribute("id");
        String value = property.attribute("value");
        propertyMap.put(id, value);
      }

      formFieldHandler.setProperties(propertyMap);
    }

  }

  protected void parseValidation(Element formField, FormFieldHandler formFieldHandler, BpmnParse bpmnParse, ExpressionManager expressionManager) {

    Element validationElement = formField.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "validation");

    if(validationElement != null) {
      List<Element> constraintElements = validationElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "constraint");

      for (Element property : constraintElements) {
         FormFieldValidator validator = Context.getProcessEngineConfiguration()
           .getFormValidators()
           .createValidator(property, bpmnParse, expressionManager);

         String validatorName = property.attribute("name");
         String validatorConfig = property.attribute("config");

         if(validator != null) {
           FormFieldValidationConstraintHandler handler = new FormFieldValidationConstraintHandler();
           handler.setName(validatorName);
           handler.setConfig(validatorConfig);
           handler.setValidator(validator);
           formFieldHandler.getValidationHandlers().add(handler);
         }
      }
    }
  }


  protected FormTypes getFormTypes() {
    FormTypes formTypes = Context
        .getProcessEngineConfiguration()
        .getFormTypes();
    return formTypes;
  }

  protected void parseFormProperties(BpmnParse bpmnParse, ExpressionManager expressionManager, Element extensionElement) {
    FormTypes formTypes = getFormTypes();

    List<Element> formPropertyElements = extensionElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formProperty");
    for (Element formPropertyElement : formPropertyElements) {
      FormPropertyHandler formPropertyHandler = new FormPropertyHandler();

      String id = formPropertyElement.attribute("id");
      if (id==null) {
        bpmnParse.addError("attribute 'id' is required", formPropertyElement);
      }
      formPropertyHandler.setId(id);

      String name = formPropertyElement.attribute("name");
      formPropertyHandler.setName(name);

      AbstractFormFieldType type = formTypes.parseFormPropertyType(formPropertyElement, bpmnParse);
      formPropertyHandler.setType(type);

      String requiredText = formPropertyElement.attribute("required", "false");
      Boolean required = bpmnParse.parseBooleanAttribute(requiredText);
      if (required!=null) {
        formPropertyHandler.setRequired(required);
      } else {
        bpmnParse.addError("attribute 'required' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
      }

      String readableText = formPropertyElement.attribute("readable", "true");
      Boolean readable = bpmnParse.parseBooleanAttribute(readableText);
      if (readable!=null) {
        formPropertyHandler.setReadable(readable);
      } else {
        bpmnParse.addError("attribute 'readable' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
      }

      String writableText = formPropertyElement.attribute("writable", "true");
      Boolean writable = bpmnParse.parseBooleanAttribute(writableText);
      if (writable!=null) {
        formPropertyHandler.setWritable(writable);
      } else {
        bpmnParse.addError("attribute 'writable' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
      }

      String variableName = formPropertyElement.attribute("variable");
      formPropertyHandler.setVariableName(variableName);

      String expressionText = formPropertyElement.attribute("expression");
      if (expressionText!=null) {
        Expression expression = expressionManager.createExpression(expressionText);
        formPropertyHandler.setVariableExpression(expression);
      }

      String defaultExpressionText = formPropertyElement.attribute("default");
      if (defaultExpressionText!=null) {
        Expression defaultExpression = expressionManager.createExpression(defaultExpressionText);
        formPropertyHandler.setDefaultExpression(defaultExpression);
      }

      formPropertyHandlers.add(formPropertyHandler);
    }
  }

  protected void initializeFormProperties(FormDataImpl formData, ExecutionEntity execution) {
    List<FormProperty> formProperties = new ArrayList<FormProperty>();
    for (FormPropertyHandler formPropertyHandler: formPropertyHandlers) {
      if (formPropertyHandler.isReadable()) {
        FormProperty formProperty = formPropertyHandler.createFormProperty(execution);
        formProperties.add(formProperty);
      }
    }
    formData.setFormProperties(formProperties);
  }

  protected void initializeFormFields(FormDataImpl taskFormData, ExecutionEntity execution) {
    // add form fields
    final List<FormField> formFields = taskFormData.getFormFields();
    for (FormFieldHandler formFieldHandler : formFieldHandlers) {
      formFields.add(formFieldHandler.createFormField(execution));
    }
  }

  public void submitFormProperties(Map<String, Object> properties, ExecutionEntity execution) {
    Map<String, Object> propertiesCopy = new HashMap<String, Object>(properties);

    // support legacy form properties
    for (FormPropertyHandler formPropertyHandler: formPropertyHandlers) {
      // submitFormProperty will remove all the keys which it takes care of
      formPropertyHandler.submitFormProperty(execution, propertiesCopy);
    }

    // support form data:
    for (FormFieldHandler formFieldHandler : formFieldHandlers) {
      formFieldHandler.handleSubmit(execution, propertiesCopy, properties);
    }

    // any variables passed in which are not handled by form-fields or form
    // properties are added to the process as variables
    for (String propertyId: propertiesCopy.keySet()) {
      execution.setVariable(propertyId, propertiesCopy.get(propertyId));
    }
  }


  // getters and setters //////////////////////////////////////////////////////

  public Expression getFormKey() {
    return formKey;
  }

  public void setFormKey(Expression formKey) {
    this.formKey = formKey;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public List<FormPropertyHandler> getFormPropertyHandlers() {
    return formPropertyHandlers;
  }

  public void setFormPropertyHandlers(List<FormPropertyHandler> formPropertyHandlers) {
    this.formPropertyHandlers = formPropertyHandlers;
  }
}
