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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_TARGET_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PARAMETER_MAPPING;

import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Parameter;
import org.camunda.bpm.model.cmmn.instance.ParameterMapping;
import org.camunda.bpm.model.cmmn.instance.TransformationExpression;
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
public class ParameterMappingImpl extends CmmnElementImpl implements ParameterMapping {

  protected static AttributeReference<Parameter> sourceRefAttribute;
  protected static AttributeReference<Parameter> targetRefAttribute;

  protected static ChildElement<TransformationExpression> transformationChild;

  public ParameterMappingImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Parameter getSource() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSource(Parameter parameter) {
    sourceRefAttribute.setReferenceTargetElement(this, parameter);
  }

  public Parameter getTarget() {
    return targetRefAttribute.getReferenceTargetElement(this);
  }

  public void setTarget(Parameter parameter) {
    targetRefAttribute.setReferenceTargetElement(this, parameter);
  }

  public TransformationExpression getTransformation() {
    return transformationChild.getChild(this);
  }

  public void setTransformation(TransformationExpression transformationExpression) {
    transformationChild.setChild(this, transformationExpression);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ParameterMapping.class, CMMN_ELEMENT_PARAMETER_MAPPING)
        .extendsType(CmmnElement.class)
        .namespaceUri(CMMN11_NS)
        .instanceProvider(new ModelTypeInstanceProvider<ParameterMapping>() {
          public ParameterMapping newInstance(ModelTypeInstanceContext instanceContext) {
            return new ParameterMappingImpl(instanceContext);
          }
        });

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
        .idAttributeReference(Parameter.class)
        .build();

    targetRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_TARGET_REF)
        .idAttributeReference(Parameter.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    transformationChild = sequenceBuilder.element(TransformationExpression.class)
        .build();

    typeBuilder.build();
  }

}
