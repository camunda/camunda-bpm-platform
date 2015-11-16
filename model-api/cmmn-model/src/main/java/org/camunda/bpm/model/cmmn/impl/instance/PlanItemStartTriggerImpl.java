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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PLAN_ITEM_START_TRIGGER;

import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemStartTrigger;
import org.camunda.bpm.model.cmmn.instance.PlanItemTransitionStandardEvent;
import org.camunda.bpm.model.cmmn.instance.StartTrigger;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Roman Smirnov
 *
 */
public class PlanItemStartTriggerImpl extends StartTriggerImpl implements PlanItemStartTrigger {

  protected static AttributeReference<PlanItem> sourceRefAttribute;
  protected static ChildElement<PlanItemTransitionStandardEvent> standardEventChild;

  public PlanItemStartTriggerImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public PlanItem getSource() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSource(PlanItem source) {
    sourceRefAttribute.setReferenceTargetElement(this, source);
  }

  public PlanItemTransition getStandardEvent() {
    PlanItemTransitionStandardEvent child = standardEventChild.getChild(this);
    return child.getValue();
  }

  public void setStandardEvent(PlanItemTransition standardEvent) {
    PlanItemTransitionStandardEvent child = standardEventChild.getChild(this);
    child.setValue(standardEvent);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PlanItemStartTrigger.class, CMMN_ELEMENT_PLAN_ITEM_START_TRIGGER)
        .extendsType(StartTrigger.class)
        .namespaceUri(CMMN11_NS)
        .instanceProvider(new ModelTypeInstanceProvider<PlanItemStartTrigger>() {
          public PlanItemStartTrigger newInstance(ModelTypeInstanceContext instanceContext) {
            return new PlanItemStartTriggerImpl(instanceContext);
          }
        });

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
        .idAttributeReference(PlanItem.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    standardEventChild = sequenceBuilder.element(PlanItemTransitionStandardEvent.class)
        .build();

    typeBuilder.build();
  }

}
