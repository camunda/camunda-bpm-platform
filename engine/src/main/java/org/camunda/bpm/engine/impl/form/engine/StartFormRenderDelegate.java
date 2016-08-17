package org.camunda.bpm.engine.impl.form.engine;

import org.camunda.bpm.engine.form.FormField;

/**
 * @author Askar Akhmerov
 */
public class StartFormRenderDelegate extends AbstractRenderFormDelegate {
  protected static final String CAM_BUSINESS_KEY_ATTRIBUTE = "cam-business-key";

  protected void addBusinessKeyAttribute(HtmlElementWriter elementWriter, FormField formField) {
    if (formField.isBusinessKey()) {
      elementWriter.attribute(CAM_BUSINESS_KEY_ATTRIBUTE, null);
    }
  }

  protected void renderInputField(FormField formField, HtmlDocumentBuilder documentBuilder) {
    HtmlElementWriter inputField = new HtmlElementWriter(INPUT_ELEMENT, true);
    addCommonFormFieldAttributes(formField, inputField);

    String inputType = !isBoolean(formField) ? TEXT_INPUT_TYPE : CHECKBOX_INPUT_TYPE;

    inputField.attribute(TYPE_ATTRIBUTE, inputType);
    addBusinessKeyAttribute(inputField,formField);

    // add default value
    Object defaultValue = formField.getDefaultValue();
    if(defaultValue != null) {
      inputField.attribute(VALUE_ATTRIBUTE, defaultValue.toString());
    }

    // <input ... />
    documentBuilder.startElement(inputField).endElement();
  }
}
