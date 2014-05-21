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

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN10_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SENTRY_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PLAN_ITEM_ON_PART;

import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.instance.OnPart;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemOnPart;
import org.camunda.bpm.model.cmmn.instance.Sentry;
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
public class PlanItemOnPartImpl extends OnPartImpl implements PlanItemOnPart {

  protected static AttributeReference<PlanItem> sourceRefAttribute;
  protected static AttributeReference<Sentry> sentryRefAttribute;
  protected static ChildElement<PlanItemTransitionStandardEvent> standardEventChild;

  public PlanItemOnPartImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Sentry getSentry() {
    return sentryRefAttribute.getReferenceTargetElement(this);
  }

  public void setSentry(Sentry sentry) {
    sentryRefAttribute.setReferenceTargetElement(this, sentry);
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
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PlanItemOnPart.class, CMMN_ELEMENT_PLAN_ITEM_ON_PART)
        .extendsType(OnPart.class)
        .namespaceUri(CMMN10_NS)
        .instanceProvider(new ModelTypeInstanceProvider<PlanItemOnPart>() {
          public PlanItemOnPart newInstance(ModelTypeInstanceContext instanceContext) {
            return new PlanItemOnPartImpl(instanceContext);
          }
        });

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
        .idAttributeReference(PlanItem.class)
        .build();

    sentryRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SENTRY_REF)
        .idAttributeReference(Sentry.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    standardEventChild = sequenceBuilder.element(PlanItemTransitionStandardEvent.class)
        .build();

    typeBuilder.build();
  }

}
