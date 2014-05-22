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

import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_IO_SPECIFICATION;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN IoSpecification element
 *
 * @author Sebastian Menski
 */
public class IoSpecificationImpl extends BaseElementImpl implements IoSpecification {

  protected static ChildElementCollection<DataInput> dataInputCollection;
  protected static ChildElementCollection<DataOutput> dataOutputCollection;
  protected static ChildElementCollection<InputSet> inputSetCollection;
  protected static ChildElementCollection<OutputSet> outputSetCollection;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(IoSpecification.class, BPMN_ELEMENT_IO_SPECIFICATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(BaseElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<IoSpecification>() {
        public IoSpecification newInstance(ModelTypeInstanceContext instanceContext) {
          return new IoSpecificationImpl(instanceContext);
        }
      });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    dataInputCollection = sequenceBuilder.elementCollection(DataInput.class)
      .build();

    dataOutputCollection = sequenceBuilder.elementCollection(DataOutput.class)
      .build();

    inputSetCollection = sequenceBuilder.elementCollection(InputSet.class)
      .required()
      .build();

    outputSetCollection = sequenceBuilder.elementCollection(OutputSet.class)
      .required()
      .build();

    typeBuilder.build();
  }

  public IoSpecificationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<DataInput> getDataInputs() {
    return dataInputCollection.get(this);
  }

  public Collection<DataOutput> getDataOutputs() {
    return dataOutputCollection.get(this);
  }

  public Collection<InputSet> getInputSets() {
    return inputSetCollection.get(this);
  }

  public Collection<OutputSet> getOutputSets() {
    return outputSetCollection.get(this);
  }
}
