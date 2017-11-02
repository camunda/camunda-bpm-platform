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
package org.camunda.bpm.engine.test.api.form;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.type.EnumFormType;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * <p>Testcase verifying support for form matadata provided using
 * custom extension elements in BPMN Xml</p>
 *
 * @author Daniel Meyer
 *
 */
public class FormDataTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testGetFormFieldBasicProperties() {

    runtimeService.startProcessInstanceByKey("FormDataTest.testGetFormFieldBasicProperties");

    Task task = taskService.createTaskQuery().singleResult();
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    // validate properties:
    List<FormField> formFields = taskFormData.getFormFields();

    // validate field 1
    FormField formField1 = formFields.get(0);
    assertNotNull(formField1);
    assertEquals(formField1.getId(), "formField1");
    assertEquals(formField1.getLabel(), "Form Field 1");
    assertEquals("string", formField1.getTypeName());
    assertNotNull(formField1.getType());

    // validate field 2
    FormField formField2 = formFields.get(1);
    assertNotNull(formField2);
    assertEquals(formField2.getId(), "formField2");
    assertEquals(formField2.getLabel(), "Form Field 2");
    assertEquals("boolean", formField2.getTypeName());
    assertNotNull(formField1.getType());

  }

  @Deployment
  public void testGetFormFieldBuiltInTypes() {

    runtimeService.startProcessInstanceByKey("FormDataTest.testGetFormFieldBuiltInTypes");

    Task task = taskService.createTaskQuery().singleResult();

    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    // validate properties:
    List<FormField> formFields = taskFormData.getFormFields();

    // validate string field
    FormField stringField = formFields.get(0);
    assertNotNull(stringField);
    assertEquals("string", stringField.getTypeName());
    assertNotNull(stringField.getType());
    assertEquals("someString", stringField.getDefaultValue());

    // validate long field
    FormField longField = formFields.get(1);
    assertNotNull(longField);
    assertEquals("long", longField.getTypeName());
    assertNotNull(longField.getType());
    assertEquals(Long.valueOf(1l), longField.getDefaultValue());

    // validate boolean field
    FormField booleanField = formFields.get(2);
    assertNotNull(booleanField);
    assertEquals("boolean", booleanField.getTypeName());
    assertNotNull(booleanField.getType());
    assertEquals(Boolean.valueOf(true), booleanField.getDefaultValue());

    // validate date field
    FormField dateField = formFields.get(3);
    assertNotNull(dateField);
    assertEquals("date", dateField.getTypeName());
    assertNotNull(dateField.getType());
    Date dateValue = (Date) dateField.getDefaultValue();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateValue);
    assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH));
    assertEquals(2013, calendar.get(Calendar.YEAR));

    // validate enum field
    FormField enumField = formFields.get(4);
    assertNotNull(enumField);
    assertEquals("enum", enumField.getTypeName());
    assertNotNull(enumField.getType());
    EnumFormType enumFormType = (EnumFormType) enumField.getType();
    Map<String, String> values = enumFormType.getValues();
    assertEquals("A", values.get("a"));
    assertEquals("B", values.get("b"));
    assertEquals("C", values.get("c"));

  }

  @Deployment
  public void testGetFormFieldProperties() {

    runtimeService.startProcessInstanceByKey("FormDataTest.testGetFormFieldProperties");

    Task task = taskService.createTaskQuery().singleResult();

    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    List<FormField> formFields = taskFormData.getFormFields();

    FormField stringField = formFields.get(0);
    Map<String, String> properties = stringField.getProperties();
    assertEquals("property1", properties.get("p1"));
    assertEquals("property2", properties.get("p2"));

  }

  @Deployment
  public void testGetFormFieldValidationConstraints() {

    runtimeService.startProcessInstanceByKey("FormDataTest.testGetFormFieldValidationConstraints");

    Task task = taskService.createTaskQuery().singleResult();

    TaskFormData taskFormData = formService.getTaskFormData(task.getId());

    List<FormField> formFields = taskFormData.getFormFields();

    FormField field1 = formFields.get(0);
    List<FormFieldValidationConstraint> validationConstraints = field1.getValidationConstraints();
    FormFieldValidationConstraint constraint1 = validationConstraints.get(0);
    assertEquals("maxlength", constraint1.getName());
    assertEquals("10", constraint1.getConfiguration());
    FormFieldValidationConstraint constraint2 = validationConstraints.get(1);
    assertEquals("minlength", constraint2.getName());
    assertEquals("5", constraint2.getConfiguration());

  }

  @Deployment
  public void testFormFieldSubmit() {

    // valid submit
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FormDataTest.testFormFieldSubmit");
    Task task = taskService.createTaskQuery().singleResult();
    Map<String, Object> formValues = new HashMap<String, Object>();
    formValues.put("stringField", "12345");
    formValues.put("longField", 9L);
    formValues.put("customField", "validValue");
    formService.submitTaskForm(task.getId(), formValues);

    assertEquals(formValues, runtimeService.getVariables(processInstance.getId()));
    runtimeService.deleteProcessInstance(processInstance.getId(), "test complete");

    runtimeService.startProcessInstanceByKey("FormDataTest.testFormFieldSubmit");
    task = taskService.createTaskQuery().singleResult();
    // invalid submit 1

    formValues = new HashMap<String, Object>();
    formValues.put("stringField", "1234");
    formValues.put("longField", 9L);
    formValues.put("customField", "validValue");
    try {
      formService.submitTaskForm(task.getId(), formValues);
      fail();
    } catch (FormFieldValidatorException e) {
      assertEquals(e.getName(), "minlength");
    }

    // invalid submit 2
    formValues = new HashMap<String, Object>();

    formValues.put("customFieldWithValidationDetails", "C");
    try {
      formService.submitTaskForm(task.getId(), formValues);
      fail();
    } catch (FormFieldValidatorException e) {
      assertEquals(e.getName(), "validator");
      assertEquals(e.getId(), "customFieldWithValidationDetails");

      assertTrue(e.getCause() instanceof FormFieldValidationException);

      FormFieldValidationException exception = (FormFieldValidationException) e.getCause();
      assertEquals(exception.getDetail(), "EXPIRED");
    }

  }

  @Deployment
  public void testSubmitFormDataWithEmptyDate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FormDataTest.testSubmitFormDataWithEmptyDate");
    Task task = taskService.createTaskQuery().singleResult();
    Map<String, Object> formValues = new HashMap<String, Object>();
    formValues.put("stringField", "12345");
    formValues.put("dateField", "");

    // when
    formService.submitTaskForm(task.getId(), formValues);

    // then
    formValues.put("dateField", null);
    assertEquals(formValues, runtimeService.getVariables(processInstance.getId()));
  }

  @Deployment
  public void testMissingFormVariables()
  {
    // given process definition with defined form varaibles
    // when start process instance with no variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("date-form-property-test");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // then taskFormData contains form variables with null as values
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    assertNotNull(taskFormData);
    assertEquals(5, taskFormData.getFormFields().size());
    for (FormField field : taskFormData.getFormFields()) {
      assertNotNull(field);
      assertNull(field.getValue().getValue());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/form/FormDataTest.testDoubleQuotesAreEscapedInGeneratedTaskForms.bpmn20.xml")
  public void testDoubleQuotesAreEscapedInGeneratedTaskForms() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "This is a \"Test\" message!");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    Task taskWithForm = taskService.createTaskQuery().singleResult();

    // when
    Object renderedStartForm = formService.getRenderedTaskForm(taskWithForm.getId());
    assertTrue(renderedStartForm instanceof String);

    // then
    String renderedForm = (String) renderedStartForm;
    String expectedFormValueWithEscapedQuotes = "This is a &quot;Test&quot; message!";
    assertTrue(renderedForm.contains(expectedFormValueWithEscapedQuotes));

  }

}
