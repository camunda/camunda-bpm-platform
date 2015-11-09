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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PLANNING_TABLE;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.ApplicabilityRule;
import org.camunda.bpm.model.cmmn.instance.PlanningTable;
import org.camunda.bpm.model.cmmn.instance.TableItem;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class PlanningTableImpl extends TableItemImpl implements PlanningTable {

  protected static ChildElementCollection<TableItem> tableItemCollection;
  protected static ChildElementCollection<ApplicabilityRule> applicabilityRuleCollection;

  public PlanningTableImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<TableItem> getTableItems() {
    return tableItemCollection.get(this);
  }

  public Collection<ApplicabilityRule> getApplicabilityRules() {
    return applicabilityRuleCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PlanningTable.class, CMMN_ELEMENT_PLANNING_TABLE)
        .namespaceUri(CMMN11_NS)
        .extendsType(TableItem.class)
        .instanceProvider(new ModelTypeInstanceProvider<PlanningTable>() {
          public PlanningTable newInstance(ModelTypeInstanceContext instanceContext) {
            return new PlanningTableImpl(instanceContext);
          }
        });


    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    tableItemCollection = sequenceBuilder.elementCollection(TableItem.class)
        .build();

    applicabilityRuleCollection = sequenceBuilder.elementCollection(ApplicabilityRule.class)
        .build();

    typeBuilder.build();
  }

}
