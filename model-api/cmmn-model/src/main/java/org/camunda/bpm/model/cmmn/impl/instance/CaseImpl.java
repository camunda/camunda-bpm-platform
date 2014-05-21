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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class CaseImpl extends CmmnElementImpl implements Case {

  protected static Attribute<String> nameAttribute;

  protected static ChildElement<CaseFileModel> caseFileModelChild;
  protected static ChildElement<CasePlanModel> casePlanModelChild;
  protected static ChildElementCollection<CaseRoles> caseRolesCollection;
  protected static ChildElementCollection<InputCaseParameter> inputCollection;
  protected static ChildElementCollection<OutputCaseParameter> outputCollection;

  public CaseImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public Collection<CaseRoles> getCaseRoles() {
    return caseRolesCollection.get(this);
  }

  public Collection<InputCaseParameter> getInputs() {
    return inputCollection.get(this);
  }

  public Collection<OutputCaseParameter> getOutputs() {
    return outputCollection.get(this);
  }

  public CasePlanModel getCasePlanModel() {
    return casePlanModelChild.getChild(this);
  }

  public void setCasePlanModel(CasePlanModel casePlanModel) {
    casePlanModelChild.setChild(this, casePlanModel);
  }

  public CaseFileModel getCaseFileModel() {
    return caseFileModelChild.getChild(this);
  }

  public void setCaseFileModel(CaseFileModel caseFileModel) {
    caseFileModelChild.setChild(this, (CaseFileModel) caseFileModel);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Case.class, CMMN_ELEMENT_CASE)
        .extendsType(CmmnElement.class)
        .namespaceUri(CMMN10_NS)
        .instanceProvider(new ModelTypeInstanceProvider<Case>() {
          public Case newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseImpl(instanceContext);
          }
        });

    nameAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_NAME)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    caseFileModelChild = sequenceBuilder.element(CaseFileModel.class)
        .build();

    casePlanModelChild = sequenceBuilder.element(CasePlanModel.class)
        .build();

    caseRolesCollection = sequenceBuilder.elementCollection(CaseRoles.class)
        .build();

    inputCollection = sequenceBuilder.elementCollection(InputCaseParameter.class)
        .build();

    outputCollection = sequenceBuilder.elementCollection(OutputCaseParameter.class)
        .build();

    typeBuilder.build();
  }

}
