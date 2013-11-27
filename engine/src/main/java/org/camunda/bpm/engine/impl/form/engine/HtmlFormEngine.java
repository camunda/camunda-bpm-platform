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


import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.type.EnumFormType;

/**
 * <p>A simple {@link FormEngine} implementaiton which renders
 * forms as HTML such that they can be used as embedded forms
 * inside camunda Tasklist.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HtmlFormEngine implements FormEngine {

  public String getName() {
    return "html";
  }

  public Object renderStartForm(StartFormData startForm) {
    return renderFormData(startForm);
  }

  public Object renderTaskForm(TaskFormData taskForm) {
    return renderFormData(taskForm);
  }

  protected String renderFormData(FormData formData) {

    if(formData == null
        || (formData.getFormFields() == null || formData.getFormFields().isEmpty())
        && (formData.getFormProperties() == null || formData.getFormProperties().isEmpty())) {
      return null;

    } else {
      HtmlDocumentBuilder documentBuilder = new HtmlDocumentBuilder(new HtmlElementWriter("form").attribute("class", "form-horizontal"));

      // render fields
      for (FormField formField : formData.getFormFields()) {
        renderFormField(formField, documentBuilder);
      }

      // render deprecated form properties
      for (FormProperty formProperty : formData.getFormProperties()) {
        renderFormField(new FormPropertyAdapter(formProperty), documentBuilder);
      }

      // end document element
      documentBuilder.endElement();

      return documentBuilder.getHtmlString();

    }
  }

  protected void renderFormField(FormField formField, HtmlDocumentBuilder documentBuilder) {
    // start group
    documentBuilder.startElement(new HtmlElementWriter("div").attribute("class", "control-group"));

    // write label
    documentBuilder.startElement(new HtmlElementWriter("label").attribute("class", "control-label").textContent(formField.getLabel()))
      .endElement();

    // start controls
    documentBuilder.startElement(new HtmlElementWriter("div").attribute("class", "controls"));

    // render form control
    if(EnumFormType.TYPE_NAME.equals(formField.getTypeName())) {
      // <select ...>
      renderSelectBox(formField, documentBuilder);

    } else {
      // <input ...>
      renderInputField(formField, documentBuilder);

    }

    // end controls
    documentBuilder.endElement();

    // end group
    documentBuilder.endElement();
  }

  protected void renderInputField(FormField formField, HtmlDocumentBuilder documentBuilder) {
    HtmlElementWriter inputField = new HtmlElementWriter("input", true);
    addCommonFormFieldAttributes(formField, inputField);

    // add default value
    Object defaultValue = formField.getDefaultValue();
    if(defaultValue != null) {
      inputField.attribute("value", defaultValue.toString());
    }

    // <input ... />
    documentBuilder.startElement(inputField).endElement();
  }

  protected void renderSelectBox(FormField formField, HtmlDocumentBuilder documentBuilder) {
    HtmlElementWriter selectBox = new HtmlElementWriter("select", false);
    addCommonFormFieldAttributes(formField, selectBox);
    // Limitation: enum is currently always of type "string"
    selectBox.attribute("type", "string");

    // <select ...>
    documentBuilder.startElement(selectBox);

    // <option ...>
    renderSelectOptions(formField, documentBuilder);

    // </select>
    documentBuilder.endElement();
  }

  protected void renderSelectOptions(FormField formField, HtmlDocumentBuilder documentBuilder) {
    EnumFormType enumFormType = (EnumFormType) formField.getType();
    Map<String, String> values = enumFormType.getValues();

    for (Entry<String, String> value : values.entrySet()) {
      // <option>
      HtmlElementWriter option = new HtmlElementWriter("option", false)
        .attribute("value", value.getKey())
        .textContent(value.getValue());

      Object defaultValue = formField.getDefaultValue();
      if(defaultValue != null && defaultValue.equals(value.getKey())) {
        option.attribute("selected", null);
      }

      documentBuilder.startElement(option).endElement();
    }
  }

  protected void addCommonFormFieldAttributes(FormField formField, HtmlElementWriter formControl) {
    formControl
      .attribute("form-field", null)
      .attribute("type", formField.getTypeName())
      .attribute("name", formField.getId());

    // add validation constraints
    for (FormFieldValidationConstraint constraint : formField.getValidationConstraints()) {
      formControl.attribute(constraint.getName(), (String) constraint.getConfiguration());
    }
  }

}
