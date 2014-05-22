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

package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.instance.Documentation;
import org.camunda.bpm.model.bpmn.instance.Extension;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN extension element
 *
 * @author Sebastian Menski
 */
public class ExtensionImpl extends BpmnModelElementInstanceImpl implements Extension {

  protected static Attribute<String> definitionAttribute;
  protected static Attribute<Boolean> mustUnderstandAttribute;
  protected static ChildElementCollection<Documentation> documentationCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Extension.class, BPMN_ELEMENT_EXTENSION)
      .namespaceUri(BPMN20_NS)
      .instanceProvider(new ModelTypeInstanceProvider<Extension>() {
        public Extension newInstance(ModelTypeInstanceContext instanceContext) {
          return new ExtensionImpl(instanceContext);
        }
      });

    // TODO: qname reference extension definition
    definitionAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_DEFINITION)
      .build();

    mustUnderstandAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_MUST_UNDERSTAND)
      .defaultValue(false)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    documentationCollection = sequenceBuilder.elementCollection(Documentation.class)
      .build();

    typeBuilder.build();
  }

  public ExtensionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getDefinition() {
    return definitionAttribute.getValue(this);
  }

  public void setDefinition(String Definition) {
    definitionAttribute.setValue(this, Definition);
  }

  public boolean mustUnderstand() {
    return mustUnderstandAttribute.getValue(this);
  }

  public void setMustUnderstand(boolean mustUnderstand) {
    mustUnderstandAttribute.setValue(this, mustUnderstand);
  }

  public Collection<Documentation> getDocumentations() {
    return documentationCollection.get(this);
  }
}
