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

package org.camunda.bpm.model.dmn.impl.instance;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN10_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_ID;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT;

import org.camunda.bpm.model.dmn.instance.Description;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

public abstract class DmnElementImpl extends DmnModelElementInstanceImpl implements DmnElement {

  protected static Attribute<String> idAttribute;
  protected static Attribute<String> nameAttribute;

  protected static ChildElement<Description> descriptionChild;

  public DmnElementImpl (ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
    if (getId() == null) {
      setId(ModelUtil.getUniqueIdentifier(getElementType()));
    }
  }

  public String getId() {
    return idAttribute.getValue(this);
  }

  public void setId(String id) {
    idAttribute.setValue(this, id);;
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public String getDescription() {
    Description child = descriptionChild.getChild(this);
    if (child != null) {
      return child.getTextContent();
    }
    else {
      return null;
    }
  }

  public void setDescription(String description) {
    Description child = descriptionChild.getChild(this);
    if (child == null) {
      child = modelInstance.newInstance(Description.class);
      descriptionChild.setChild(this, child);
    }
    child.setTextContent(description);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DmnElement.class, DMN_ELEMENT)
      .namespaceUri(DMN10_NS)
      .abstractType();

    idAttribute = typeBuilder.stringAttribute(DMN_ATTRIBUTE_ID)
      .idAttribute()
      .required()
      .build();

    nameAttribute = typeBuilder.stringAttribute(DMN_ATTRIBUTE_NAME)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    descriptionChild = sequenceBuilder.element(Description.class)
      .build();

    typeBuilder.build();
  }

}
