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
import org.camunda.bpm.model.bpmn.builder.IntermediateThrowEventBuilder;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_TASK_PRIORITY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * The BPMN intermediateThrowEvent element
 *
 * @author Sebastian Menski
 */
public class IntermediateThrowEventImpl extends ThrowEventImpl implements IntermediateThrowEvent {

  /** camunda extensions */
  protected static Attribute<String> camundaTaskPriorityAttribute;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(IntermediateThrowEvent.class, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)
      .namespaceUri(BpmnModelConstants.BPMN20_NS)
      .extendsType(ThrowEvent.class)
      .instanceProvider(new ModelTypeInstanceProvider<IntermediateThrowEvent>() {
        public IntermediateThrowEvent newInstance(ModelTypeInstanceContext instanceContext) {
          return new IntermediateThrowEventImpl(instanceContext);
        }
      });

    camundaTaskPriorityAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TASK_PRIORITY)
      .namespace(CAMUNDA_NS)
      .build();
    
    typeBuilder.build();
  }

  public IntermediateThrowEventImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public IntermediateThrowEventBuilder builder() {
    return new IntermediateThrowEventBuilder((BpmnModelInstance) modelInstance, this);
  }
  
  /** camunda extensions */
  
  @Override
  public String getCamundaTaskPriority() {
    return camundaTaskPriorityAttribute.getValue(this);    
  }

  @Override
  public void setCamundaTaskPriority(String taskPriority) {
    camundaTaskPriorityAttribute.setValue(this, taskPriority);
  }
}
