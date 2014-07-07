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

package org.camunda.bpm.model.bpmn.impl.instance.camunda;

import java.util.Collection;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * The BPMN inputOutput camunda extension element
 *
 * @author Sebastian Menski
 */
public class CamundaInputOutputImpl extends BpmnModelElementInstanceImpl implements CamundaInputOutput {

  protected static ChildElementCollection<CamundaInputParameter> camundaInputParameterCollection;
  protected static ChildElementCollection<CamundaOutputParameter> camundaOutputParameterCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaInputOutput.class, CAMUNDA_ELEMENT_INPUT_OUTPUT)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaInputOutput>() {
        public CamundaInputOutput newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaInputOutputImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaInputParameterCollection = sequenceBuilder.elementCollection(CamundaInputParameter.class)
      .build();

    camundaOutputParameterCollection = sequenceBuilder.elementCollection(CamundaOutputParameter.class)
      .build();

    typeBuilder.build();
  }

  public CamundaInputOutputImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<CamundaInputParameter> getCamundaInputParameters() {
    return camundaInputParameterCollection.get(this);
  }

  public Collection<CamundaOutputParameter> getCamundaOutputParameters() {
    return camundaOutputParameterCollection.get(this);
  }
}
