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
package org.camunda.bpm.model.xml.testmodel.instance;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReference;

import java.util.Collection;

import static org.camunda.bpm.model.xml.testmodel.TestModelConstants.*;

/**
 * @author Daniel Meyer
 *
 */
public class Bird extends FlyingAnimal {

  private static ChildElementCollection<Egg> eggColl;
  private static ElementReference<Bird, SpouseRef> spouseRefsColl;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Bird.class, ELEMENT_NAME_BIRD)
      .namespaceUri(MODEL_NAMESPACE)
      .extendsType(FlyingAnimal.class)
      .instanceProvider(new ModelTypeInstanceProvider<Bird>() {
        public Bird newInstance(ModelTypeInstanceContext instanceContext) {
          return new Bird(instanceContext);
        }
      });

    SequenceBuilder sequence = typeBuilder.sequence();

    eggColl = sequence.elementCollection(Egg.class, ELEMENT_NAME_EGG)
      .minOccurs(0)
      .maxOccurs(6)
      .build();

    spouseRefsColl = sequence.element(SpouseRef.class, ELEMENT_NAME_SPOUSE_REF)
      .qNameElementReference(Bird.class)
      .build();

    typeBuilder.build();

  }

  public Bird(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<Egg> getEggs() {
    return eggColl.get(this);
  }

  public Bird getSpouse() {
    return spouseRefsColl.getReferenceTargetElement(this);
  }

  public void setSpouse(Bird bird) {
    spouseRefsColl.setReferenceTargetElement(this, bird);
  }

  public void removeSpouse() {
    spouseRefsColl.getReferenceTargetElements(this).clear();
  }

  public SpouseRef getSpouseRef() {
    return spouseRefsColl.getReferenceSource(this);
  }

}
