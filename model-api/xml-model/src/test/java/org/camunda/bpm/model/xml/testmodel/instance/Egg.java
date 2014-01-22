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

package org.camunda.bpm.model.xml.testmodel.instance;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import static org.camunda.bpm.model.xml.testmodel.TestModelConstants.*;

/**
 * @author Sebastian Menski
 */
public class Egg extends ModelElementInstanceImpl {

  private static Attribute<String> idAttr;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Egg.class, ELEMENT_NAME_EGG)
      .namespaceUri(MODEL_NAMESPACE)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<Egg>() {
        public Egg newInstance(ModelTypeInstanceContext instanceContext) {
          return new Egg(instanceContext);
        }
      });

    idAttr = typeBuilder.stringAttribute(ATTRIBUTE_NAME_ID)
      .idAttribute()
      .build();

    typeBuilder.build();
  }

  public Egg(final ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttr.getValue(this);
  }

  public void setId(String id) {
    idAttr.setValue(this, id);
  }
}
