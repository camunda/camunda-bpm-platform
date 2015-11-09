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
package org.camunda.bpm.model.cmmn.instance;

import java.util.Collection;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public interface HumanTask extends Task {

  Role getPerformer();

  void setPerformer(Role performerRef);

  @Deprecated
  Collection<PlanningTable> getPlanningTables();

  PlanningTable getPlanningTable();

  void setPlanningTable(PlanningTable planningTable);

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

  String getCamundaFormKey();

  void setCamundaFormKey(String camundaFormKey);

  String getCamundaPriority();

  void setCamundaPriority(String camundaPriority);

}
