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

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_IMPLEMENTATION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_USER_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_ASSIGNEE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CANDIDATE_GROUPS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_CANDIDATE_USERS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_DUE_DATE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_FOLLOW_UP_DATE;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_FORM_HANDLER_CLASS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_FORM_KEY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ATTRIBUTE_PRIORITY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.Rendering;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.StringUtil;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * The BPMN userTask element
 *
 * @author Sebastian Menski
 */
public class UserTaskImpl extends TaskImpl implements UserTask {

  protected static Attribute<String> implementationAttribute;
  protected static ChildElementCollection<Rendering> renderingCollection;

  /** camunda extensions */

  protected static Attribute<String> camundaAssigneeAttribute;
  protected static Attribute<String> camundaCandidateGroupsAttribute;
  protected static Attribute<String> camundaCandidateUsersAttribute;
  protected static Attribute<String> camundaDueDateAttribute;
  protected static Attribute<String> camundaFollowUpDateAttribute;
  protected static Attribute<String> camundaFormHandlerClassAttribute;
  protected static Attribute<String> camundaFormKeyAttribute;
  protected static Attribute<String> camundaPriorityAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(UserTask.class, BPMN_ELEMENT_USER_TASK)
      .namespaceUri(BPMN20_NS)
      .extendsType(Task.class)
      .instanceProvider(new ModelTypeInstanceProvider<UserTask>() {
        public UserTask newInstance(ModelTypeInstanceContext instanceContext) {
          return new UserTaskImpl(instanceContext);
        }
      });

    implementationAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_IMPLEMENTATION)
      .defaultValue("##unspecified")
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    renderingCollection = sequenceBuilder.elementCollection(Rendering.class)
      .build();

    /** camunda extensions */

    camundaAssigneeAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_ASSIGNEE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaCandidateGroupsAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CANDIDATE_GROUPS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaCandidateUsersAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_CANDIDATE_USERS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaDueDateAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_DUE_DATE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaFollowUpDateAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_FOLLOW_UP_DATE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaFormHandlerClassAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_FORM_HANDLER_CLASS)
      .namespace(CAMUNDA_NS)
      .build();

    camundaFormKeyAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_FORM_KEY)
      .namespace(CAMUNDA_NS)
      .build();

    camundaPriorityAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_PRIORITY)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public UserTaskImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public UserTaskBuilder builder() {
    return new UserTaskBuilder((BpmnModelInstance) modelInstance, this);
  }

  public String getImplementation() {
    return implementationAttribute.getValue(this);
  }

  public void setImplementation(String implementation) {
    implementationAttribute.setValue(this, implementation);
  }

  public Collection<Rendering> getRenderings() {
    return renderingCollection.get(this);
  }

  /** camunda extensions */

  public String getCamundaAssignee() {
    return camundaAssigneeAttribute.getValue(this);
  }

  public void setCamundaAssignee(String camundaAssignee) {
    camundaAssigneeAttribute.setValue(this, camundaAssignee);
  }

  public String getCamundaCandidateGroups() {
    return camundaCandidateGroupsAttribute.getValue(this);
  }

  public void setCamundaCandidateGroups(String camundaCandidateGroups) {
    camundaCandidateGroupsAttribute.setValue(this, camundaCandidateGroups);
  }

  public List<String> getCamundaCandidateGroupsList() {
    String candidateGroups = camundaCandidateGroupsAttribute.getValue(this);
    return StringUtil.splitCommaSeparatedList(candidateGroups);
  }

  public void setCamundaCandidateGroupsList(List<String> camundaCandidateGroupsList) {
    String candidateGroups = StringUtil.joinCommaSeparatedList(camundaCandidateGroupsList);
    camundaCandidateGroupsAttribute.setValue(this, candidateGroups);
  }

  public String getCamundaCandidateUsers() {
    return camundaCandidateUsersAttribute.getValue(this);
  }

  public void setCamundaCandidateUsers(String camundaCandidateUsers) {
    camundaCandidateUsersAttribute.setValue(this, camundaCandidateUsers);
  }

  public List<String> getCamundaCandidateUsersList() {
    String candidateUsers = camundaCandidateUsersAttribute.getValue(this);
    return StringUtil.splitCommaSeparatedList(candidateUsers);
  }

  public void setCamundaCandidateUsersList(List<String> camundaCandidateUsersList) {
    String candidateUsers = StringUtil.joinCommaSeparatedList(camundaCandidateUsersList);
    camundaCandidateUsersAttribute.setValue(this, candidateUsers);
  }

  public String getCamundaDueDate() {
    return camundaDueDateAttribute.getValue(this);
  }

  public void setCamundaDueDate(String camundaDueDate) {
    camundaDueDateAttribute.setValue(this, camundaDueDate);
  }

  public String getCamundaFollowUpDate() {
    return camundaFollowUpDateAttribute.getValue(this);
  }

  public void setCamundaFollowUpDate(String camundaFollowUpDate) {
    camundaFollowUpDateAttribute.setValue(this, camundaFollowUpDate);
  }

  public String getCamundaFormHandlerClass() {
    return camundaFormHandlerClassAttribute.getValue(this);
  }

  public void setCamundaFormHandlerClass(String camundaFormHandlerClass) {
    camundaFormHandlerClassAttribute.setValue(this, camundaFormHandlerClass);
  }

  public String getCamundaFormKey() {
    return camundaFormKeyAttribute.getValue(this);
  }

  public void setCamundaFormKey(String camundaFormKey) {
    camundaFormKeyAttribute.setValue(this, camundaFormKey);
  }

  public String getCamundaPriority() {
    return camundaPriorityAttribute.getValue(this);
  }

  public void setCamundaPriority(String camundaPriority) {
    camundaPriorityAttribute.setValue(this, camundaPriority);
  }

}
