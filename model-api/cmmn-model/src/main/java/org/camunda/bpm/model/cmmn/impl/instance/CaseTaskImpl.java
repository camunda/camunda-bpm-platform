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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_CASE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.ParameterMapping;
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
public class CaseTaskImpl extends TaskImpl implements CaseTask {

  protected static AttributeReference<Case> caseRefAttribute;
  protected static ChildElementCollection<ParameterMapping> parameterMappingCollection;

  public CaseTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Case getCase() {
    return caseRefAttribute.getReferenceTargetElement(this);
  }

  public void setCase(Case caseInstance) {
    caseRefAttribute.setReferenceTargetElement(this, caseInstance);
  }

  public Collection<ParameterMapping> getParameterMappings() {
    return parameterMappingCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseTask.class, CMMN_ELEMENT_CASE_TASK)
        .extendsType(Task.class)
        .namespaceUri(CMMN10_NS)
        .instanceProvider(new ModelTypeInstanceProvider<CaseTask>() {
          public CaseTask newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseTaskImpl(instanceContext);
          }
        });

    caseRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_CASE_REF)
        .qNameAttributeReference(Case.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterMappingCollection = sequenceBuilder.elementCollection(ParameterMapping.class)
        .build();

    typeBuilder.build();
  }

}
