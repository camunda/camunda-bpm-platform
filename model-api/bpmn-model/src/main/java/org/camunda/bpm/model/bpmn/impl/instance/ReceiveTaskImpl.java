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
import org.camunda.bpm.model.bpmn.builder.ReceiveTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.Operation;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN receiveTask element
 *
 * @author Sebastian Menski
 */
public class ReceiveTaskImpl extends TaskImpl implements ReceiveTask {

  protected static Attribute<String> implementationAttribute;
  protected static Attribute<Boolean> instantiateAttribute;
  protected static AttributeReference<Message> messageRefAttribute;
  protected static AttributeReference<Operation> operationRefAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ReceiveTask.class, BPMN_ELEMENT_RECEIVE_TASK)
      .namespaceUri(BPMN20_NS)
      .extendsType(Task.class)
      .instanceProvider(new ModelTypeInstanceProvider<ReceiveTask>() {
        public ReceiveTask newInstance(ModelTypeInstanceContext instanceContext) {
          return new ReceiveTaskImpl(instanceContext);
        }
      });

    implementationAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_IMPLEMENTATION)
      .defaultValue("##WebService")
      .build();

    instantiateAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_INSTANTIATE)
      .defaultValue(false)
      .build();

    messageRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_MESSAGE_REF)
      .qNameAttributeReference(Message.class)
      .build();

    operationRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_OPERATION_REF)
      .qNameAttributeReference(Operation.class)
      .build();

    typeBuilder.build();
  }

  public ReceiveTaskImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public ReceiveTaskBuilder builder() {
    return new ReceiveTaskBuilder((BpmnModelInstance) modelInstance, this);
  }

  public String getImplementation() {
    return implementationAttribute.getValue(this);
  }

  public void setImplementation(String implementation) {
    implementationAttribute.setValue(this, implementation);
  }

  public boolean instantiate() {
    return instantiateAttribute.getValue(this);
  }

  public void setInstantiate(boolean instantiate) {
    instantiateAttribute.setValue(this, instantiate);
  }

  public Message getMessage() {
    return messageRefAttribute.getReferenceTargetElement(this);
  }

  public void setMessage(Message message) {
    messageRefAttribute.setReferenceTargetElement(this, message);
  }

  public Operation getOperation() {
    return operationRefAttribute.getReferenceTargetElement(this);
  }

  public void setOperation(Operation operation) {
    operationRefAttribute.setReferenceTargetElement(this, operation);
  }
}
