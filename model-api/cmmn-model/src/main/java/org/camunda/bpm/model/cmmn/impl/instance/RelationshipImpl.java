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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_DIRECTION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_TYPE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_RELATIONSHIP;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.RelationshipDirection;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Relationship;
import org.camunda.bpm.model.cmmn.instance.Source;
import org.camunda.bpm.model.cmmn.instance.Target;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class RelationshipImpl extends CmmnElementImpl implements Relationship {

  protected static Attribute<String> typeAttribute;
  protected static Attribute<RelationshipDirection> directionAttribute;
  protected static ChildElementCollection<Source> sourceCollection;
  protected static ChildElementCollection<Target> targetCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Relationship.class, CMMN_ELEMENT_RELATIONSHIP)
      .namespaceUri(CMMN11_NS)
      .extendsType(CmmnElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<Relationship>() {
        public Relationship newInstance(ModelTypeInstanceContext instanceContext) {
          return new RelationshipImpl(instanceContext);
        }
      });

    typeAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_TYPE)
      .required()
      .build();

    directionAttribute = typeBuilder.enumAttribute(CMMN_ATTRIBUTE_DIRECTION, RelationshipDirection.class)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    sourceCollection = sequenceBuilder.elementCollection(Source.class)
      .minOccurs(1)
      .build();

    targetCollection = sequenceBuilder.elementCollection(Target.class)
      .minOccurs(1)
      .build();

    typeBuilder.build();
  }

  public RelationshipImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getType() {
    return typeAttribute.getValue(this);
  }

  public void setType(String type) {
    typeAttribute.setValue(this, type);
  }

  public RelationshipDirection getDirection() {
    return directionAttribute.getValue(this);
  }

  public void setDirection(RelationshipDirection direction) {
    directionAttribute.setValue(this, direction);
  }

  public Collection<Source> getSources() {
    return sourceCollection.get(this);
  }

  public Collection<Target> getTargets() {
    return targetCollection.get(this);
  }

}
