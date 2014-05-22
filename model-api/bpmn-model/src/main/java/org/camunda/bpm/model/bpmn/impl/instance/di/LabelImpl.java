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

package org.camunda.bpm.model.bpmn.impl.instance.di;

import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Label;
import org.camunda.bpm.model.bpmn.instance.di.Node;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_LABEL;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;

/**
 * The DI Label element
 *
 * @author Sebastian Menski
 */
public abstract class LabelImpl extends NodeImpl implements Label {

  protected static ChildElement<Bounds> boundsChild;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Label.class, DI_ELEMENT_LABEL)
      .namespaceUri(DI_NS)
      .extendsType(Node.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    boundsChild = sequenceBuilder.element(Bounds.class)
      .build();

    typeBuilder.build();
  }

  public LabelImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Bounds getBounds() {
    return boundsChild.getChild(this);
  }

  public void setBounds(Bounds bounds) {
    boundsChild.setChild(this, bounds);
  }
}
