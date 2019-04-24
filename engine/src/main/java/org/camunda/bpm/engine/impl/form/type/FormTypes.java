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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.form.handler.DefaultFormHandler;
import org.camunda.bpm.engine.impl.util.xml.Element;


/**
 * @author Tom Baeyens
 */
public class FormTypes {

  protected Map<String, AbstractFormFieldType> formTypes = new HashMap<String, AbstractFormFieldType>();

  public void addFormType(AbstractFormFieldType formType) {
    formTypes.put(formType.getName(), formType);
  }

  public AbstractFormFieldType parseFormPropertyType(Element formFieldElement, BpmnParse bpmnParse) {
    AbstractFormFieldType formType = null;

    String typeText = formFieldElement.attribute("type");
    String datePatternText = formFieldElement.attribute("datePattern");

    if (typeText == null && DefaultFormHandler.FORM_FIELD_ELEMENT.equals(formFieldElement.getTagName())) {
      bpmnParse.addError("form field must have a 'type' attribute", formFieldElement);
    }

    if ("date".equals(typeText) && datePatternText!=null) {
      formType = new DateFormType(datePatternText);

    } else if ("enum".equals(typeText)) {
      // ACT-1023: Using linked hashmap to preserve the order in which the entries are defined
      Map<String, String> values = new LinkedHashMap<String, String>();
      for (Element valueElement: formFieldElement.elementsNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,"value")) {
        String valueId = valueElement.attribute("id");
        String valueName = valueElement.attribute("name");
        values.put(valueId, valueName);
      }
      formType = new EnumFormType(values);

    } else if (typeText!=null) {
      formType = formTypes.get(typeText);
      if (formType==null) {
        bpmnParse.addError("unknown type '"+typeText+"'", formFieldElement);
      }
    }
    return formType;
  }

  public AbstractFormFieldType getFormType(String name) {
    return formTypes.get(name);
  }
}
