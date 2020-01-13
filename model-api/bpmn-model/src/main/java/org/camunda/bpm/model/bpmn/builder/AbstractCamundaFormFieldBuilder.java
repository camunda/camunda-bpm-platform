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
package org.camunda.bpm.model.bpmn.builder;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;

/**
 * @author Kristin Polenz
 *
 */
public class AbstractCamundaFormFieldBuilder<P, B extends AbstractCamundaFormFieldBuilder<P, B>> 
  extends AbstractBpmnModelElementBuilder<B, CamundaFormField> {

  protected BaseElement parent;

  protected AbstractCamundaFormFieldBuilder(BpmnModelInstance modelInstance, BaseElement parent, CamundaFormField element, Class<?> selfType) {
    super(modelInstance, element, selfType);
    this.parent = parent;
  }
  

  /**
   * Sets the form field id.
   *
   * @param id the form field id
   * @return  the builder object
   */
  public B camundaId(String id) {
    element.setCamundaId(id);
    return myself;
  }

  /**
   * Sets form field label.
   *
   * @param label the form field label
   * @return  the builder object
   */
  public B camundaLabel(String label) {
    element.setCamundaLabel(label);;
    return myself;
  }

  /**
   * Sets the form field type.
   *
   * @param type the form field type
   * @return the builder object
   */
  public B camundaType(String type) {
    element.setCamundaType(type);
    return myself;
  }

  /**
   * Sets the form field default value.
   *
   * @param defaultValue the form field default value
   * @return the builder object
   */
  public B camundaDefaultValue(String defaultValue) {
    element.setCamundaDefaultValue(defaultValue);
    return myself;
  }

  /**
   * Finishes the building of a form field.
   *
   * @return the parent activity builder
   */
  @SuppressWarnings({ "unchecked" })
  public P camundaFormFieldDone() {
    return (P) parent.builder();
  }
}
