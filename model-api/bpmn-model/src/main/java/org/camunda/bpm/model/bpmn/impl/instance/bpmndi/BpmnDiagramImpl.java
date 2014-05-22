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

package org.camunda.bpm.model.bpmn.impl.instance.bpmndi;

import org.camunda.bpm.model.bpmn.impl.instance.di.DiagramImpl;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabelStyle;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.di.Diagram;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMNDI_ELEMENT_BPMN_DIAGRAM;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMNDI_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMNDI BPMNDiagram element
 *
 * @author Sebastian Menski
 */
public class BpmnDiagramImpl extends DiagramImpl implements BpmnDiagram {

  protected static ChildElement<BpmnPlane> bpmnPlaneChild;
  protected static ChildElementCollection<BpmnLabelStyle> bpmnLabelStyleCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(BpmnDiagram.class, BPMNDI_ELEMENT_BPMN_DIAGRAM)
      .namespaceUri(BPMNDI_NS)
      .extendsType(Diagram.class)
      .instanceProvider(new ModelTypeInstanceProvider<BpmnDiagram>(){
        public BpmnDiagram newInstance(ModelTypeInstanceContext instanceContext) {
          return new BpmnDiagramImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    bpmnPlaneChild = sequenceBuilder.element(BpmnPlane.class)
      .required()
      .build();

    bpmnLabelStyleCollection = sequenceBuilder.elementCollection(BpmnLabelStyle.class)
      .build();

    typeBuilder.build();
  }

  public BpmnDiagramImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public BpmnPlane getBpmnPlane() {
    return bpmnPlaneChild.getChild(this);
  }

  public void setBpmnPlane(BpmnPlane bpmnPlane) {
    bpmnPlaneChild.setChild(this, bpmnPlane);
  }

  public Collection<BpmnLabelStyle> getBpmnLabelStyles() {
    return bpmnLabelStyleCollection.get(this);
  }
}
