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

package org.camunda.bpm.model.xml.impl.type.reference;

import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.ModelReferenceException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.reference.ElementReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;

/**
 * @author Sebastian Menski
 */
public class ElementReferenceImpl<Target extends ModelElementInstance, Source extends ModelElementInstance>  extends ElementReferenceCollectionImpl<Target,Source> implements ElementReference<Target, Source> {


  public ElementReferenceImpl(final ChildElement<Source> referenceSourceCollection) {
    super(referenceSourceCollection);
  }

  private ChildElement<Source> getReferenceSourceChild() {
    return (ChildElement<Source>) getReferenceSourceCollection();
  }

  public Source getReferenceSource(final ModelElementInstance referenceSourceParent) {
    return getReferenceSourceChild().getChild(referenceSourceParent);
  }

  private void setReferenceSource(final ModelElementInstance referenceSourceParent, final Source referenceSource) {
    getReferenceSourceChild().setChild(referenceSourceParent, referenceSource);
  }

  @SuppressWarnings("unchecked")
  public Target getReferenceTargetElement(final ModelElementInstanceImpl referenceSourceParentElement) {
    Source referenceSource = getReferenceSource(referenceSourceParentElement);
    if (referenceSource != null) {
      String identifier = getReferenceIdentifier(referenceSource);
      ModelElementInstance referenceTargetElement = referenceSourceParentElement.getModelInstance().getModelElementById(identifier);
      if (referenceTargetElement != null) {
        return (Target) referenceTargetElement;
      }
      else {
        throw new ModelException("Unable to find a model element instance for id " + identifier);
      }
    }
    else {
      return null;
    }
  }

  public void setReferenceTargetElement(final ModelElementInstanceImpl referenceSourceParentElement, final Target referenceTargetElement) {
    ModelInstanceImpl modelInstance = referenceSourceParentElement.getModelInstance();
    String identifier = referenceTargetAttribute.getValue(referenceTargetElement);
    ModelElementInstance existingElement = modelInstance.getModelElementById(identifier);

    if (existingElement == null || !existingElement.equals(referenceTargetElement)) {
      throw new ModelReferenceException("Cannot create reference to model element " + referenceTargetElement
        +": element is not part of model. Please connect element to the model first.");
    }
    else {
      Source referenceSourceElement = modelInstance.newInstance(getReferenceSourceElementType());
      setReferenceSource(referenceSourceParentElement, referenceSourceElement);
      setReferenceIdentifier(referenceSourceElement, identifier);
    }
  }

  public void clearReferenceTargetElement(final ModelElementInstanceImpl referenceSourceParentElement) {
    Source referenceSource = getReferenceSource(referenceSourceParentElement);
    Element parentDomElement = referenceSourceParentElement.getDomElement();
    Element childDomElement = ((ModelElementInstanceImpl) referenceSource).getDomElement();
    DomUtil.removeChild(parentDomElement, childDomElement);
  }
}
