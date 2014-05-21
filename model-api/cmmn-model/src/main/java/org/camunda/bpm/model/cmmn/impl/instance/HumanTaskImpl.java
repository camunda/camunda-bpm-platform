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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_PERFORMER_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_HUMAN_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanningTable;
import org.camunda.bpm.model.cmmn.instance.Role;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskImpl extends TaskImpl implements HumanTask {

  protected static AttributeReference<Role> performerRefAttribute;
  protected static ChildElementCollection<PlanningTable> planningTableChild;

  public HumanTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Role getPerformer() {
    return performerRefAttribute.getReferenceTargetElement(this);
  }

  public void setPerformer(Role performer) {
    performerRefAttribute.setReferenceTargetElement(this, performer);
  }

  public Collection<PlanningTable> getPlanningTables() {
    return planningTableChild.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(HumanTask.class, CMMN_ELEMENT_HUMAN_TASK)
        .namespaceUri(CMMN10_NS)
        .extendsType(Task.class)
        .instanceProvider(new ModelTypeInstanceProvider<HumanTask>() {
          public HumanTask newInstance(ModelTypeInstanceContext instanceContext) {
            return new HumanTaskImpl(instanceContext);
          }
        });

    performerRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_PERFORMER_REF)
        .idAttributeReference(Role.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    planningTableChild = sequenceBuilder.elementCollection(PlanningTable.class)
        .build();

    typeBuilder.build();
  }

}
