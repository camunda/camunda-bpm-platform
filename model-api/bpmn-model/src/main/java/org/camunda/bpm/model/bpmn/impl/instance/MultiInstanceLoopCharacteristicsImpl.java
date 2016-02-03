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

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BEHAVIOR;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_IS_SEQUENTIAL;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_MULTI_INSTANCE_LOOP_CHARACTERISTICS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_NONE_BEHAVIOR_EVENT_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ONE_BEHAVIOR_EVENT_REF;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ASYNC_AFTER;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ASYNC_BEFORE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXCLUSIVE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_COLLECTION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ELEMENT_VARIABLE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.MultiInstanceFlowCondition;
import org.camunda.bpm.model.bpmn.builder.MultiInstanceLoopCharacteristicsBuilder;
import org.camunda.bpm.model.bpmn.instance.CompletionCondition;
import org.camunda.bpm.model.bpmn.instance.ComplexBehaviorDefinition;
import org.camunda.bpm.model.bpmn.instance.DataInput;
import org.camunda.bpm.model.bpmn.instance.DataOutput;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.InputDataItem;
import org.camunda.bpm.model.bpmn.instance.LoopCardinality;
import org.camunda.bpm.model.bpmn.instance.LoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.OutputDataItem;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;
import org.camunda.bpm.model.xml.type.reference.ElementReference;

/**
 * The BPMN 2.0 multiInstanceLoopCharacteristics element
 *
 * @author Filip Hrisafov
 */
public class MultiInstanceLoopCharacteristicsImpl extends LoopCharacteristicsImpl implements MultiInstanceLoopCharacteristics {

  protected static Attribute<Boolean> isSequentialAttribute;
  protected static Attribute<MultiInstanceFlowCondition> behaviorAttribute;
  protected static AttributeReference<EventDefinition> oneBehaviorEventRefAttribute;
  protected static AttributeReference<EventDefinition> noneBehaviorEventRefAttribute;
  protected static ChildElement<LoopCardinality> loopCardinalityChild;
  protected static ElementReference<DataInput, LoopDataInputRef> loopDataInputRefChild;
  protected static ElementReference<DataOutput, LoopDataOutputRef> loopDataOutputRefChild;
  protected static ChildElement<InputDataItem> inputDataItemChild;
  protected static ChildElement<OutputDataItem> outputDataItemChild;
  protected static ChildElementCollection<ComplexBehaviorDefinition> complexBehaviorDefinitionCollection;
  protected static ChildElement<CompletionCondition> completionConditionChild;
  protected static Attribute<Boolean> camundaAsyncAfter;
  protected static Attribute<Boolean> camundaAsyncBefore;
  protected static Attribute<Boolean> camundaExclusive;
  protected static Attribute<String> camundaCollection;
  protected static Attribute<String> camundaElementVariable;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder
      .defineType(MultiInstanceLoopCharacteristics.class, BPMN_ELEMENT_MULTI_INSTANCE_LOOP_CHARACTERISTICS)
      .namespaceUri(BPMN20_NS)
      .extendsType(LoopCharacteristics.class)
      .instanceProvider(new ModelTypeInstanceProvider<MultiInstanceLoopCharacteristics>() {

        public MultiInstanceLoopCharacteristics newInstance(ModelTypeInstanceContext instanceContext) {
          return new MultiInstanceLoopCharacteristicsImpl(instanceContext);
        }
      });

    isSequentialAttribute = typeBuilder.booleanAttribute(BPMN_ELEMENT_IS_SEQUENTIAL)
      .defaultValue(false)
      .build();

    behaviorAttribute = typeBuilder.enumAttribute(BPMN_ELEMENT_BEHAVIOR, MultiInstanceFlowCondition.class)
      .defaultValue(MultiInstanceFlowCondition.All)
      .build();

    oneBehaviorEventRefAttribute = typeBuilder.stringAttribute(BPMN_ELEMENT_ONE_BEHAVIOR_EVENT_REF)
      .qNameAttributeReference(EventDefinition.class)
      .build();

    noneBehaviorEventRefAttribute = typeBuilder.stringAttribute(BPMN_ELEMENT_NONE_BEHAVIOR_EVENT_REF)
      .qNameAttributeReference(EventDefinition.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    loopCardinalityChild = sequenceBuilder.element(LoopCardinality.class)
      .build();

    loopDataInputRefChild = sequenceBuilder.element(LoopDataInputRef.class)
      .qNameElementReference(DataInput.class)
      .build();

    loopDataOutputRefChild = sequenceBuilder.element(LoopDataOutputRef.class)
      .qNameElementReference(DataOutput.class)
      .build();

    outputDataItemChild = sequenceBuilder.element(OutputDataItem.class)
      .build();

    inputDataItemChild = sequenceBuilder.element(InputDataItem.class)
      .build();

    complexBehaviorDefinitionCollection = sequenceBuilder.elementCollection(ComplexBehaviorDefinition.class)
      .build();

    completionConditionChild = sequenceBuilder.element(CompletionCondition.class)
      .build();

    camundaAsyncAfter = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_ASYNC_AFTER)
        .namespace(CAMUNDA_NS)
        .defaultValue(false)
        .build();

    camundaAsyncBefore = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_ASYNC_BEFORE)
        .namespace(CAMUNDA_NS)
        .defaultValue(false)
        .build();

    camundaExclusive = typeBuilder.booleanAttribute(CAMUNDA_ATTRIBUTE_EXCLUSIVE)
        .namespace(CAMUNDA_NS)
        .defaultValue(true)
        .build();

    camundaCollection = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_COLLECTION)
        .namespace(CAMUNDA_NS)
        .build();

    camundaElementVariable = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ELEMENT_VARIABLE)
        .namespace(CAMUNDA_NS)
        .build();

    typeBuilder.build();
  }

  public MultiInstanceLoopCharacteristicsImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public MultiInstanceLoopCharacteristicsBuilder builder() {
    return new MultiInstanceLoopCharacteristicsBuilder((BpmnModelInstance) modelInstance, this);
  }

  public LoopCardinality getLoopCardinality() {
    return loopCardinalityChild.getChild(this);
  }

  public void setLoopCardinality(LoopCardinality loopCardinality) {
    loopCardinalityChild.setChild(this, loopCardinality);
  }

  public DataInput getLoopDataInputRef() {
    return loopDataInputRefChild.getReferenceTargetElement(this);
  }

  public void setLoopDataInputRef(DataInput loopDataInputRef) {
    loopDataInputRefChild.setReferenceTargetElement(this, loopDataInputRef);
  }

  public DataOutput getLoopDataOutputRef() {
    return loopDataOutputRefChild.getReferenceTargetElement(this);
  }

  public void setLoopDataOutputRef(DataOutput loopDataOutputRef) {
    loopDataOutputRefChild.setReferenceTargetElement(this, loopDataOutputRef);
  }

  public InputDataItem getInputDataItem() {
    return inputDataItemChild.getChild(this);
  }

  public void setInputDataItem(InputDataItem inputDataItem) {
    inputDataItemChild.setChild(this, inputDataItem);
  }

  public OutputDataItem getOutputDataItem() {
    return outputDataItemChild.getChild(this);
  }

  public void setOutputDataItem(OutputDataItem outputDataItem) {
    outputDataItemChild.setChild(this, outputDataItem);
  }

  public Collection<ComplexBehaviorDefinition> getComplexBehaviorDefinitions() {
    return complexBehaviorDefinitionCollection.get(this);
  }

  public CompletionCondition getCompletionCondition() {
    return completionConditionChild.getChild(this);
  }

  public void setCompletionCondition(CompletionCondition completionCondition) {
    completionConditionChild.setChild(this, completionCondition);
  }

  public boolean isSequential() {
    return isSequentialAttribute.getValue(this);
  }

  public void setSequential(boolean sequential) {
    isSequentialAttribute.setValue(this, sequential);
  }

  public MultiInstanceFlowCondition getBehavior() {
    return behaviorAttribute.getValue(this);
  }

  public void setBehavior(MultiInstanceFlowCondition behavior) {
    behaviorAttribute.setValue(this, behavior);
  }

  public EventDefinition getOneBehaviorEventRef() {
    return oneBehaviorEventRefAttribute.getReferenceTargetElement(this);
  }

  public void setOneBehaviorEventRef(EventDefinition oneBehaviorEventRef) {
    oneBehaviorEventRefAttribute.setReferenceTargetElement(this, oneBehaviorEventRef);
  }

  public EventDefinition getNoneBehaviorEventRef() {
    return noneBehaviorEventRefAttribute.getReferenceTargetElement(this);
  }

  public void setNoneBehaviorEventRef(EventDefinition noneBehaviorEventRef) {
    noneBehaviorEventRefAttribute.setReferenceTargetElement(this, noneBehaviorEventRef);
  }

  public boolean isCamundaAsyncBefore() {
    return camundaAsyncBefore.getValue(this);
  }

  public void setCamundaAsyncBefore(boolean isCamundaAsyncBefore) {
    camundaAsyncBefore.setValue(this, isCamundaAsyncBefore);
  }

  public boolean isCamundaAsyncAfter() {
    return camundaAsyncAfter.getValue(this);
  }

  public void setCamundaAsyncAfter(boolean isCamundaAsyncAfter) {
    camundaAsyncAfter.setValue(this, isCamundaAsyncAfter);
  }

  public boolean isCamundaExclusive() {
    return camundaExclusive.getValue(this);
  }

  public void setCamundaExclusive(boolean isCamundaExclusive) {
    camundaExclusive.setValue(this, isCamundaExclusive);
  }

  public String getCamundaCollection() {
    return camundaCollection.getValue(this);
  }

  public void setCamundaCollection(String expression) {
    camundaCollection.setValue(this, expression);
  }

  public String getCamundaElementVariable() {
    return camundaElementVariable.getValue(this);
  }

  public void setCamundaElementVariable(String variableName) {
    camundaElementVariable.setValue(this, variableName);
  }
}
