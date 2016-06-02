/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_ERROR_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ERROR_CODE_VARIABLE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ERROR_MESSAGE_VARIABLE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN errorEventDefinition element
 *
 * @author Sebastian Menski
 */
public class ErrorEventDefinitionImpl extends EventDefinitionImpl implements ErrorEventDefinition {

  protected static AttributeReference<Error> errorRefAttribute;

  protected static Attribute<String> camundaErrorCodeVariableAttribute;

  protected static Attribute<String> camundaErrorMessageVariableAttribute;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ErrorEventDefinition.class, BPMN_ELEMENT_ERROR_EVENT_DEFINITION)
      .namespaceUri(BPMN20_NS)
      .extendsType(EventDefinition.class)
      .instanceProvider(new ModelTypeInstanceProvider<ErrorEventDefinition>() {
        public ErrorEventDefinition newInstance(ModelTypeInstanceContext instanceContext) {
          return new ErrorEventDefinitionImpl(instanceContext);
        }
      });

    errorRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_ERROR_REF)
      .qNameAttributeReference(Error.class)
      .build();
    
    camundaErrorCodeVariableAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ERROR_CODE_VARIABLE)
        .namespace(CAMUNDA_NS)
        .build();

    camundaErrorMessageVariableAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ERROR_MESSAGE_VARIABLE)
      .namespace(CAMUNDA_NS)
      .build();
    
    typeBuilder.build();
  }

  public ErrorEventDefinitionImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public Error getError() {
    return errorRefAttribute.getReferenceTargetElement(this);
  }

  public void setError(Error error) {
    errorRefAttribute.setReferenceTargetElement(this, error);
  }

  @Override
  public void setCamundaErrorCodeVariable(String camundaErrorCodeVariable) {
    camundaErrorCodeVariableAttribute.setValue(this, camundaErrorCodeVariable);
  }

  @Override
  public String getCamundaErrorCodeVariable() {
    return camundaErrorCodeVariableAttribute.getValue(this);
  }

  @Override
  public void setCamundaErrorMessageVariable(String camundaErrorMessageVariable) {
    camundaErrorMessageVariableAttribute.setValue(this, camundaErrorMessageVariable);
  }

  @Override
  public String getCamundaErrorMessageVariable() {
    return camundaErrorMessageVariableAttribute.getValue(this);
  }
}
