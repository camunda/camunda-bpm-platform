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

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.EventBasedGatewayType;
import org.camunda.bpm.model.bpmn.builder.EventBasedGatewayBuilder;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN eventBasedGateway element
 *
 * @author Sebastian Menski
 */
public class EventBasedGatewayImpl extends GatewayImpl implements EventBasedGateway {

  protected static Attribute<Boolean> instantiateAttribute;
  protected static Attribute<EventBasedGatewayType> eventGatewayTypeAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(EventBasedGateway.class, BPMN_ELEMENT_EVENT_BASED_GATEWAY)
      .namespaceUri(BPMN20_NS)
      .extendsType(Gateway.class)
      .instanceProvider(new ModelTypeInstanceProvider<EventBasedGateway>() {
        public EventBasedGateway newInstance(ModelTypeInstanceContext instanceContext) {
          return new EventBasedGatewayImpl(instanceContext);
        }
      });

    instantiateAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_INSTANTIATE)
      .defaultValue(false)
      .build();

    eventGatewayTypeAttribute = typeBuilder.enumAttribute(BPMN_ATTRIBUTE_EVENT_GATEWAY_TYPE, EventBasedGatewayType.class)
      .defaultValue(EventBasedGatewayType.Exclusive)
      .build();

    typeBuilder.build();
  }

  public EventBasedGatewayImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public EventBasedGatewayBuilder builder() {
    return new EventBasedGatewayBuilder((BpmnModelInstance) modelInstance, this);
  }

  public boolean isInstantiate() {
    return instantiateAttribute.getValue(this);
  }

  public void setInstantiate(boolean isInstantiate) {
    instantiateAttribute.setValue(this, isInstantiate);
  }

  public EventBasedGatewayType getEventGatewayType() {
    return eventGatewayTypeAttribute.getValue(this);
  }

  public void setEventGatewayType(EventBasedGatewayType eventGatewayType) {
    eventGatewayTypeAttribute.setValue(this, eventGatewayType);
  }

  @Override
  public boolean isCamundaAsyncAfter() {
    throw new UnsupportedOperationException("'asyncAfter' is not supported for 'Event Based Gateway'");
  }

  @Override
  public void setCamundaAsyncAfter(boolean isCamundaAsyncAfter) {
    throw new UnsupportedOperationException("'asyncAfter' is not supported for 'Event Based Gateway'");
  }

}
