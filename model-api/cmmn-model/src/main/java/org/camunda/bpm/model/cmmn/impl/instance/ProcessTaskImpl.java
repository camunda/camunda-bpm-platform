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

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_PROCESS_BINDING;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_PROCESS_VERSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_PROCESS_TENANT_ID;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_PROCESS_REF;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_PROCESS_TASK;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.ParameterMapping;
import org.camunda.bpm.model.cmmn.instance.ProcessRefExpression;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskImpl extends TaskImpl implements ProcessTask {

  protected static Attribute<String> processRefAttribute;
  protected static ChildElementCollection<ParameterMapping> parameterMappingCollection;
  protected static ChildElement<ProcessRefExpression> processRefExpressionChild;

  protected static Attribute<String> camundaProcessBindingAttribute;
  protected static Attribute<String> camundaProcessVersionAttribute;
  protected static Attribute<String> camundaProcessTenantIdAttribute;

  public ProcessTaskImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getProcess() {
    return processRefAttribute.getValue(this);
  }

  public void setProcess(String process) {
    processRefAttribute.setValue(this, process);
  }

  public ProcessRefExpression getProcessExpression() {
    return processRefExpressionChild.getChild(this);
  }

  public void setProcessExpression(ProcessRefExpression processExpression) {
    processRefExpressionChild.setChild(this, processExpression);
  }

  public Collection<ParameterMapping> getParameterMappings() {
    return parameterMappingCollection.get(this);
  }

  public String getCamundaProcessBinding() {
    return camundaProcessBindingAttribute.getValue(this);
  }

  public void setCamundaProcessBinding(String camundaProcessBinding) {
    camundaProcessBindingAttribute.setValue(this, camundaProcessBinding);
  }

  public String getCamundaProcessVersion() {
    return camundaProcessVersionAttribute.getValue(this);
  }

  public void setCamundaProcessVersion(String camundaProcessVersion) {
    camundaProcessVersionAttribute.setValue(this, camundaProcessVersion);
  }

  public String getCamundaProcessTenantId() {
    return camundaProcessTenantIdAttribute.getValue(this);
  }

  public void setCamundaProcessTenantId(String camundaProcessTenantId) {
    camundaProcessTenantIdAttribute.setValue(this, camundaProcessTenantId);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ProcessTask.class, CMMN_ELEMENT_PROCESS_TASK)
        .namespaceUri(CMMN11_NS)
        .extendsType(Task.class)
        .instanceProvider(new ModelTypeInstanceProvider<ProcessTask>() {
          public ProcessTask newInstance(ModelTypeInstanceContext instanceContext) {
            return new ProcessTaskImpl(instanceContext);
          }
        });

    processRefAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_PROCESS_REF)
        .build();

    /** camunda extensions */

    camundaProcessBindingAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_PROCESS_BINDING)
      .namespace(CAMUNDA_NS)
      .build();

    camundaProcessVersionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_PROCESS_VERSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaProcessTenantIdAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_PROCESS_TENANT_ID)
        .namespace(CAMUNDA_NS)
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    parameterMappingCollection = sequenceBuilder.elementCollection(ParameterMapping.class)
        .build();

    processRefExpressionChild = sequenceBuilder.element(ProcessRefExpression.class)
        .minOccurs(0)
        .maxOccurs(1)
        .build();

    typeBuilder.build();
  }


}
