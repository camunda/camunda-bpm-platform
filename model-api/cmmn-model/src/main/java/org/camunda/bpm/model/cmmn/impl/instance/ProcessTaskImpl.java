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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_PROCESS_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PROCESS_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.ParameterMapping;
import org.camunda.bpm.model.cmmn.instance.Process;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskImpl extends TaskImpl implements ProcessTask {

  protected static AttributeReference<Process> processRefAttribute;
  protected static ChildElementCollection<ParameterMapping> parameterMappingCollection;

  public ProcessTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Process getProcess() {
    return processRefAttribute.getReferenceTargetElement(this);
  }

  public void setProcess(Process process) {
    processRefAttribute.setReferenceTargetElement(this, process);
  }

  public Collection<ParameterMapping> getParameterMappings() {
    return parameterMappingCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ProcessTask.class, CMMN_ELEMENT_PROCESS_TASK)
        .namespaceUri(CMMN10_NS)
        .extendsType(Task.class)
        .instanceProvider(new ModelTypeInstanceProvider<ProcessTask>() {
          public ProcessTask newInstance(ModelTypeInstanceContext instanceContext) {
            return new ProcessTaskImpl(instanceContext);
          }
        });

    processRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_PROCESS_REF)
        .qNameAttributeReference(Process.class)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterMappingCollection = sequenceBuilder.elementCollection(ParameterMapping.class)
        .build();

    typeBuilder.build();
  }
}
