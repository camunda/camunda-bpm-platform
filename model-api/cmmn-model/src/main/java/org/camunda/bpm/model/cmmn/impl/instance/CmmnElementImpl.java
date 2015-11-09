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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DESCRIPTION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_ID;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Documentation;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnElementImpl extends CmmnModelElementInstanceImpl implements CmmnElement {

  protected static Attribute<String> idAttribute;
  protected static ChildElement<ExtensionElements> extensionElementsChild;

  // cmmn 1.0
  @Deprecated
  protected static Attribute<String> descriptionAttribute;

  // cmmn 1.1
  protected static ChildElementCollection<Documentation> documentationCollection;

  public CmmnElementImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttribute.getValue(this);
  }

  public void setId(String id) {
    idAttribute.setValue(this, id);
  }

  public String getDescription() {
    return descriptionAttribute.getValue(this);
  }

  public void setDescription(String description) {
    descriptionAttribute.setValue(this, description);
  }

  public Collection<Documentation> getDocumentations() {
    return documentationCollection.get(this);
  }

  public ExtensionElements getExtensionElements() {
    return extensionElementsChild.getChild(this);
  }

  public void setExtensionElements(ExtensionElements extensionElements) {
    extensionElementsChild.setChild(this, extensionElements);
  }

  protected boolean isCmmn11() {
    return CmmnModelConstants.CMMN11_NS.equals(getDomElement().getNamespaceURI());
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CmmnElement.class, CMMN_ELEMENT)
        .abstractType()
        .namespaceUri(CMMN11_NS);

    idAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_ID)
        .idAttribute()
        .build();

    descriptionAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_DESCRIPTION)
        .namespace(CMMN10_NS)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    documentationCollection = sequenceBuilder.elementCollection(Documentation.class)
      .build();

    extensionElementsChild = sequenceBuilder.element(ExtensionElements.class)
      .build();

    typeBuilder.build();
  }

}
