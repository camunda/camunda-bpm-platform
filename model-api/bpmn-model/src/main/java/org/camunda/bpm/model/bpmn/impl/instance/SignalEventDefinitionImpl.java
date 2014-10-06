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

import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_SIGNAL_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ASYNC;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN signalEventDefinition element
 *
 * @author Sebastian Menski
 */
public class SignalEventDefinitionImpl extends EventDefinitionImpl implements SignalEventDefinition {

  protected static AttributeReference<Signal> signalRefAttribute;
  protected static Attribute<Boolean> camundaAsyncAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(SignalEventDefinition.class, BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION)
      .namespaceUri(BPMN20_NS)
      .extendsType(EventDefinition.class)
      .instanceProvider(new ModelTypeInstanceProvider<SignalEventDefinition>() {
        public SignalEventDefinition newInstance(ModelTypeInstanceContext instanceContext) {
          return new SignalEventDefinitionImpl(instanceContext);
        }
      });

    signalRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_SIGNAL_REF)
      .qNameAttributeReference(Signal.class)
      .build();

    /** Camunda Attributes */
    camundaAsyncAttribute = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_ASYNC)
      .namespace(CAMUNDA_NS)
      .defaultValue(false)
      .build();

    typeBuilder.build();
  }

  public SignalEventDefinitionImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public Signal getSignal() {
    return signalRefAttribute.getReferenceTargetElement(this);
  }

  public void setSignal(Signal signal) {
    signalRefAttribute.setReferenceTargetElement(this, signal);
  }

  public boolean isCamundaAsync() {
    return camundaAsyncAttribute.getValue(this);
  }

  public void setCamundaAsync(boolean camundaAsync) {
    camundaAsyncAttribute.setValue(this, camundaAsync);
  }
}
