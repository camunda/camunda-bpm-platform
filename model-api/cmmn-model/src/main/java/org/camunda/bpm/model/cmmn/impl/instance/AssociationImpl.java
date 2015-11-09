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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_ASSOCIATION_DIRECTION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_SOURCE_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_TARGET_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_ASSOCIATION;

import org.camunda.bpm.model.cmmn.AssociationDirection;
import org.camunda.bpm.model.cmmn.instance.Artifact;
import org.camunda.bpm.model.cmmn.instance.Association;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Roman Smirnov
 *
 */
public class AssociationImpl extends ArtifactImpl implements Association {

  protected static AttributeReference<CmmnElement> sourceRefAttribute;
  protected static AttributeReference<CmmnElement> targetRefAttribute;
  protected static Attribute<AssociationDirection> associationDirectionAttribute;

  public AssociationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public CmmnElement getSource() {
    return sourceRefAttribute.getReferenceTargetElement(this);
  }

  public void setSource(CmmnElement source) {
    sourceRefAttribute.setReferenceTargetElement(this, source);
  }

  public CmmnElement getTarget() {
    return targetRefAttribute.getReferenceTargetElement(this);
  }

  public void setTarget(CmmnElement target) {
    targetRefAttribute.setReferenceTargetElement(this, target);
  }

  public AssociationDirection getAssociationDirection() {
    return associationDirectionAttribute.getValue(this);
  }

  public void setAssociationDirection(AssociationDirection associationDirection) {
    associationDirectionAttribute.setValue(this, associationDirection);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Association.class, CMMN_ELEMENT_ASSOCIATION)
      .namespaceUri(CMMN11_NS)
      .extendsType(Artifact.class)
      .instanceProvider(new ModelTypeInstanceProvider<Association>() {
        public Association newInstance(ModelTypeInstanceContext instanceContext) {
          return new AssociationImpl(instanceContext);
        }
      });

    sourceRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_SOURCE_REF)
      .idAttributeReference(CmmnElement.class)
      .build();

    targetRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_TARGET_REF)
      .idAttributeReference(CmmnElement.class)
      .build();

    associationDirectionAttribute = typeBuilder.enumAttribute(CMMN_ATTRIBUTE_ASSOCIATION_DIRECTION, AssociationDirection.class)
      .build();

    typeBuilder.build();
  }

}
