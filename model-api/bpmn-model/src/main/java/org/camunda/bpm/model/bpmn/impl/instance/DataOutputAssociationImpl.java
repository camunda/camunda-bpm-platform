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

import org.camunda.bpm.model.bpmn.instance.DataAssociation;
import org.camunda.bpm.model.bpmn.instance.DataOutputAssociation;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_DATA_OUTPUT_ASSOCIATION;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN dataOutputAssociation element
 *
 * @author Sebastian Menski
 */
public class DataOutputAssociationImpl extends DataAssociationImpl implements DataOutputAssociation {
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DataOutputAssociation.class, BPMN_ELEMENT_DATA_OUTPUT_ASSOCIATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(DataAssociation.class)
      .instanceProvider(new ModelTypeInstanceProvider<DataOutputAssociation>() {
        public DataOutputAssociation newInstance(ModelTypeInstanceContext instanceContext) {
          return new DataOutputAssociationImpl(instanceContext);
        }
      });
  
    typeBuilder.build();
  }

  public DataOutputAssociationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }
}
