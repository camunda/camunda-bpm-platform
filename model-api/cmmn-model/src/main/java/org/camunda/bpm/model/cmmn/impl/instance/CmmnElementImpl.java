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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DESCRIPTION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_ID;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT;

import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnElementImpl extends CmmnModelElementInstanceImpl implements CmmnElement {

  protected static Attribute<String> idAttribute;
  protected static Attribute<String> descriptionAttribute;

  public CmmnElementImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
    if (getId() == null) {
      setId(ModelUtil.getUniqueIdentifier(getElementType()));
    }
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

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CmmnElement.class, CMMN_ELEMENT)
        .abstractType()
        .namespaceUri(CMMN10_NS);

    idAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_ID)
        .idAttribute()
        .build();

    descriptionAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_DESCRIPTION)
        .build();

    typeBuilder.build();
  }

}
