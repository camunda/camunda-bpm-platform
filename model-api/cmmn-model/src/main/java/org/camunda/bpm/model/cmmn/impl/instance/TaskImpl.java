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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_IS_BLOCKING;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.InputCaseParameter;
import org.camunda.bpm.model.cmmn.instance.InputsCaseParameter;
import org.camunda.bpm.model.cmmn.instance.OutputCaseParameter;
import org.camunda.bpm.model.cmmn.instance.OutputsCaseParameter;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class TaskImpl extends PlanItemDefinitionImpl implements Task {

  protected static Attribute<Boolean> isBlockingAttribute;

  // cmmn 1.0
  @Deprecated
  protected static ChildElementCollection<InputsCaseParameter> inputsCollection;
  @Deprecated
  protected static ChildElementCollection<OutputsCaseParameter> outputsCollection;

  // cmmn 1.1
  protected static ChildElementCollection<InputCaseParameter> inputParameterCollection;
  protected static ChildElementCollection<OutputCaseParameter> outputParameterCollection;

  public TaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public boolean isBlocking() {
    return isBlockingAttribute.getValue(this);
  }

  public void setIsBlocking(boolean isBlocking) {
    isBlockingAttribute.setValue(this, isBlocking);
  }

  public Collection<InputsCaseParameter> getInputs() {
    return inputsCollection.get(this);
  }

  public Collection<OutputsCaseParameter> getOutputs() {
    return outputsCollection.get(this);
  }

  public Collection<InputCaseParameter> getInputParameters() {
    return inputParameterCollection.get(this);
  }

  public Collection<OutputCaseParameter> getOutputParameters() {
    return outputParameterCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Task.class, CMMN_ELEMENT_TASK)
        .namespaceUri(CMMN11_NS)
        .extendsType(PlanItemDefinition.class)
        .instanceProvider(new ModelTypeInstanceProvider<Task>() {
          public Task newInstance(ModelTypeInstanceContext instanceContext) {
            return new TaskImpl(instanceContext);
          }
        });

    isBlockingAttribute = typeBuilder.booleanAttribute(CMMN_ATTRIBUTE_IS_BLOCKING)
        .defaultValue(true)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    inputsCollection = sequenceBuilder.elementCollection(InputsCaseParameter.class)
        .build();

    outputsCollection = sequenceBuilder.elementCollection(OutputsCaseParameter.class)
        .build();

    inputParameterCollection = sequenceBuilder.elementCollection(InputCaseParameter.class)
        .build();

    outputParameterCollection = sequenceBuilder.elementCollection(OutputCaseParameter.class)
        .build();

    typeBuilder.build();
  }

}
