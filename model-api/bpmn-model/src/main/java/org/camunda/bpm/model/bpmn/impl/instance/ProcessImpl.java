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
package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.ProcessType;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.StringUtil;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN process element
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
public class ProcessImpl extends CallableElementImpl implements Process {

  protected static Attribute<ProcessType> processTypeAttribute;
  protected static Attribute<Boolean> isClosedAttribute;
  protected static Attribute<Boolean> isExecutableAttribute;
  // TODO: definitionalCollaborationRef
  protected static ChildElement<Auditing> auditingChild;
  protected static ChildElement<Monitoring> monitoringChild;
  protected static ChildElementCollection<Property> propertyCollection;
  protected static ChildElementCollection<LaneSet> laneSetCollection;
  protected static ChildElementCollection<FlowElement> flowElementCollection;
  protected static ChildElementCollection<Artifact> artifactCollection;
  protected static ChildElementCollection<ResourceRole> resourceRoleCollection;
  protected static ChildElementCollection<CorrelationSubscription> correlationSubscriptionCollection;
  protected static ElementReferenceCollection<Process, Supports> supportsCollection;

  /** camunda extensions */

  protected static Attribute<String> camundaCandidateStarterGroupsAttribute;
  protected static Attribute<String> camundaCandidateStarterUsersAttribute;
  protected static Attribute<String> camundaJobPriorityAttribute;
  protected static Attribute<String> camundaTaskPriorityAttribute;
  protected static Attribute<String> camundaHistoryTimeToLiveAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Process.class, BPMN_ELEMENT_PROCESS)
      .namespaceUri(BPMN20_NS)
      .extendsType(CallableElement.class)
      .instanceProvider(new ModelTypeInstanceProvider<Process>() {
        public Process newInstance(ModelTypeInstanceContext instanceContext) {
          return new ProcessImpl(instanceContext);
        }
      });

    processTypeAttribute = typeBuilder.enumAttribute(BPMN_ATTRIBUTE_PROCESS_TYPE, ProcessType.class)
      .defaultValue(ProcessType.None)
      .build();

    isClosedAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_IS_CLOSED)
      .defaultValue(false)
      .build();

    isExecutableAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_IS_EXECUTABLE)
      .build();

    // TODO: definitionalCollaborationRef

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    auditingChild = sequenceBuilder.element(Auditing.class)
      .build();

    monitoringChild = sequenceBuilder.element(Monitoring.class)
      .build();

    propertyCollection = sequenceBuilder.elementCollection(Property.class)
      .build();

    laneSetCollection = sequenceBuilder.elementCollection(LaneSet.class)
      .build();

    flowElementCollection = sequenceBuilder.elementCollection(FlowElement.class)
      .build();

    artifactCollection = sequenceBuilder.elementCollection(Artifact.class)
      .build();

    resourceRoleCollection = sequenceBuilder.elementCollection(ResourceRole.class)
      .build();

    correlationSubscriptionCollection = sequenceBuilder.elementCollection(CorrelationSubscription.class)
      .build();

    supportsCollection = sequenceBuilder.elementCollection(Supports.class)
      .qNameElementReferenceCollection(Process.class)
      .build();

    /** camunda extensions */

    camundaCandidateStarterGroupsAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CANDIDATE_STARTER_GROUPS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaCandidateStarterUsersAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CANDIDATE_STARTER_USERS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaJobPriorityAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_JOB_PRIORITY)
      .namespace(CAMUNDA_NS)
      .build();
    
    camundaTaskPriorityAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TASK_PRIORITY)
      .namespace(CAMUNDA_NS)
      .build();

    camundaHistoryTimeToLiveAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_HISTORY_TIME_TO_LIVE)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public ProcessImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public ProcessBuilder builder() {
    return new ProcessBuilder((BpmnModelInstance) modelInstance, this);
  }

  public ProcessType getProcessType() {
    return processTypeAttribute.getValue(this);
  }

  public void setProcessType(ProcessType processType) {
    processTypeAttribute.setValue(this, processType);
  }

  public boolean isClosed() {
    return isClosedAttribute.getValue(this);
  }

  public void setClosed(boolean closed) {
    isClosedAttribute.setValue(this, closed);
  }

  public boolean isExecutable() {
    return isExecutableAttribute.getValue(this);
  }

  public void setExecutable(boolean executable) {
    isExecutableAttribute.setValue(this, executable);
  }

  public Auditing getAuditing() {
    return auditingChild.getChild(this);
  }

  public void setAuditing(Auditing auditing) {
    auditingChild.setChild(this, auditing);
  }

  public Monitoring getMonitoring() {
    return monitoringChild.getChild(this);
  }

  public void setMonitoring(Monitoring monitoring) {
    monitoringChild.setChild(this, monitoring);
  }

  public Collection<Property> getProperties() {
    return propertyCollection.get(this);
  }

  public Collection<LaneSet> getLaneSets() {
    return laneSetCollection.get(this);
  }

  public Collection<FlowElement> getFlowElements() {
    return flowElementCollection.get(this);
  }

  public Collection<Artifact> getArtifacts() {
    return artifactCollection.get(this);
  }

  public Collection<CorrelationSubscription> getCorrelationSubscriptions() {
    return correlationSubscriptionCollection.get(this);
  }

  public Collection<ResourceRole> getResourceRoles() {
    return resourceRoleCollection.get(this);
  }

  public Collection<Process> getSupports() {
    return supportsCollection.getReferenceTargetElements(this);
  }

  /** camunda extensions */

  public String getCamundaCandidateStarterGroups() {
    return camundaCandidateStarterGroupsAttribute.getValue(this);
  }

  public void setCamundaCandidateStarterGroups(String camundaCandidateStarterGroups) {
    camundaCandidateStarterGroupsAttribute.setValue(this, camundaCandidateStarterGroups);
  }

  public List<String> getCamundaCandidateStarterGroupsList() {
    String groupsString = camundaCandidateStarterGroupsAttribute.getValue(this);
    return StringUtil.splitCommaSeparatedList(groupsString);
  }

  public void setCamundaCandidateStarterGroupsList(List<String> camundaCandidateStarterGroupsList) {
    String candidateStarterGroups = StringUtil.joinCommaSeparatedList(camundaCandidateStarterGroupsList);
    camundaCandidateStarterGroupsAttribute.setValue(this, candidateStarterGroups);
  }

  public String getCamundaCandidateStarterUsers() {
    return camundaCandidateStarterUsersAttribute.getValue(this);
  }

  public void setCamundaCandidateStarterUsers(String camundaCandidateStarterUsers) {
    camundaCandidateStarterUsersAttribute.setValue(this, camundaCandidateStarterUsers);
  }

  public List<String> getCamundaCandidateStarterUsersList() {
    String candidateStarterUsers = camundaCandidateStarterUsersAttribute.getValue(this);
    return StringUtil.splitCommaSeparatedList(candidateStarterUsers);
  }

  public void setCamundaCandidateStarterUsersList(List<String> camundaCandidateStarterUsersList) {
    String candidateStarterUsers = StringUtil.joinCommaSeparatedList(camundaCandidateStarterUsersList);
    camundaCandidateStarterUsersAttribute.setValue(this, candidateStarterUsers);
  }

  public String getCamundaJobPriority() {
    return camundaJobPriorityAttribute.getValue(this);
  }

  public void setCamundaJobPriority(String jobPriority) {
    camundaJobPriorityAttribute.setValue(this, jobPriority);
  }

  @Override
  public String getCamundaTaskPriority() {
    return camundaTaskPriorityAttribute.getValue(this);    
  }

  @Override
  public void setCamundaTaskPriority(String taskPriority) {
    camundaTaskPriorityAttribute.setValue(this, taskPriority);
  }

  @Override
  public Integer getCamundaHistoryTimeToLive() {
    String ttl = getCamundaHistoryTimeToLiveString();
    if (ttl != null) {
      return Integer.parseInt(ttl);
    }
    return null;
  }

  @Override
  public void setCamundaHistoryTimeToLive(Integer historyTimeToLive) {
    setCamundaHistoryTimeToLiveString(String.valueOf(historyTimeToLive));
  }

  @Override
  public String getCamundaHistoryTimeToLiveString() {
    return camundaHistoryTimeToLiveAttribute.getValue(this);
  }

  @Override
  public void setCamundaHistoryTimeToLiveString(String historyTimeToLive) {
    camundaHistoryTimeToLiveAttribute.setValue(this, historyTimeToLive);
  }
}
