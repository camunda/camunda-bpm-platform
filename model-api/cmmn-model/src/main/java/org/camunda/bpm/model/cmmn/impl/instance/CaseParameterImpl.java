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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_BINDING_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_PARAMETER;

import org.camunda.bpm.model.cmmn.instance.BindingRefinementExpression;
import org.camunda.bpm.model.cmmn.instance.CaseFileItem;
import org.camunda.bpm.model.cmmn.instance.CaseParameter;
import org.camunda.bpm.model.cmmn.instance.Parameter;
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
public class CaseParameterImpl extends ParameterImpl implements CaseParameter {

  protected static AttributeReference<CaseFileItem> bindingRefAttribute;
  protected static ChildElement<BindingRefinementExpression> bindingRefinementChild;

  public CaseParameterImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public CaseFileItem getBinding() {
    return bindingRefAttribute.getReferenceTargetElement(this);
  }

  public void setBinding(CaseFileItem bindingRef) {
    bindingRefAttribute.setReferenceTargetElement(this, bindingRef);
  }

  public BindingRefinementExpression getBindingRefinement() {
    return bindingRefinementChild.getChild(this);
  }

  public void setBindingRefinement(BindingRefinementExpression bindingRefinement) {
    bindingRefinementChild.setChild(this, bindingRefinement);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseParameter.class, CMMN_ELEMENT_CASE_PARAMETER)
        .namespaceUri(CMMN11_NS)
        .extendsType(Parameter.class)
        .instanceProvider(new ModelTypeInstanceProvider<CaseParameter>() {
          public CaseParameter newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseParameterImpl(instanceContext);
          }
        });

    bindingRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_BINDING_REF)
         .idAttributeReference(CaseFileItem.class)
         .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    bindingRefinementChild = sequenceBuilder.element(BindingRefinementExpression.class)
        .build();

    typeBuilder.build();
  }


}
