/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.model.bpmn.instance;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;

/**
 * The BPMN userTask element
 *
 * @author Sebastian Menski
 */
public interface UserTask extends Task {

  UserTaskBuilder builder();

  String getImplementation();

  void setImplementation(String implementation);

  Collection<Rendering> getRenderings();

  /** camunda extensions */

  String getCamundaAssignee();

  void setCamundaAssignee(String camundaAssignee);

  String getCamundaCandidateGroups();

  void setCamundaCandidateGroups(String camundaCandidateGroups);

  List<String> getCamundaCandidateGroupsList();

  void setCamundaCandidateGroupsList(List<String> camundaCandidateGroupsList);

  String getCamundaCandidateUsers();

  void setCamundaCandidateUsers(String camundaCandidateUsers);

  List<String> getCamundaCandidateUsersList();

  void setCamundaCandidateUsersList(List<String> camundaCandidateUsersList);

  String getCamundaDueDate();

  void setCamundaDueDate(String camundaDueDate);

  String getCamundaFollowUpDate();

  void setCamundaFollowUpDate(String camundaFollowUpDate);

  String getCamundaFormHandlerClass();

  void setCamundaFormHandlerClass(String camundaFormHandlerClass);

  String getCamundaFormKey();

  void setCamundaFormKey(String camundaFormKey);

  String getCamundaFormRef();

  void setCamundaFormRef(String camundaFormRef);

  String getCamundaFormRefBinding();

  void setCamundaFormRefBinding(String camundaFormRefBinding);

  String getCamundaFormRefVersion();

  void setCamundaFormRefVersion(String camundaFormRefVersion);

  String getCamundaPriority();

  void setCamundaPriority(String camundaPriority);
}
