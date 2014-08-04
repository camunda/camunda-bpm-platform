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
package org.camunda.bpm.engine.impl.db.entitymanager.operation.comparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MembershipEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;


/**
 * Compares operations by Entity type.
 *
 * @author Daniel Meyer
 *
 */
public class EntityTypeComparatorForModifications implements Comparator<Class<?>> {

  protected static Map<Class<?>, Integer> typeOrder = new HashMap<Class<?>, Integer>();

  static {

    // 1
    typeOrder.put(IncidentEntity.class, 1);
    typeOrder.put(VariableInstanceEntity.class, 1);
    typeOrder.put(IdentityLinkEntity.class, 1);

    typeOrder.put(EventSubscriptionEntity.class, 1);
    typeOrder.put(MessageEventSubscriptionEntity.class, 1);
    typeOrder.put(CompensateEventSubscriptionEntity.class, 1);
    typeOrder.put(SignalEventSubscriptionEntity.class, 1);

    typeOrder.put(JobEntity.class, 1);
    typeOrder.put(MessageEntity.class, 1);
    typeOrder.put(TimerEntity.class, 1);

    typeOrder.put(MembershipEntity.class, 1);

    // 2
    typeOrder.put(GroupEntity.class, 2);
    typeOrder.put(UserEntity.class, 2);
    typeOrder.put(ByteArrayEntity.class, 2);
    typeOrder.put(TaskEntity.class, 2);

    // 3
    typeOrder.put(ExecutionEntity.class, 3);
    typeOrder.put(CaseExecutionEntity.class, 3);

    // 4
    typeOrder.put(ProcessDefinitionEntity.class, 4);
    typeOrder.put(CaseDefinitionEntity.class, 4);
    typeOrder.put(ResourceEntity.class, 4);

    // 5
    typeOrder.put(DeploymentEntity.class, 5);

  }

  public int compare(Class<?> firstEntityType, Class<?> secondEntityType) {

    if(firstEntityType == secondEntityType) {
      return 0;
    }

    Integer firstIndex = typeOrder.get(firstEntityType);
    Integer secondIndex = typeOrder.get(secondEntityType);

    // unknown type happens before / after everything else
    if(firstIndex == null) {
      firstIndex = Integer.MAX_VALUE;
    }
    if(secondIndex == null) {
      secondIndex = Integer.MAX_VALUE;
    }

    int result = firstIndex.compareTo(secondIndex);
    if(result == 0) {
      return firstEntityType.getName().compareTo(secondEntityType.getName());

    } else {
      return result;

    }
  }

}
