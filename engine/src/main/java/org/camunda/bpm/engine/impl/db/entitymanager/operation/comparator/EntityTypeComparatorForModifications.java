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

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.management.JobDefinition;


/**
 * Compares operations by Entity type.
 *
 * @author Daniel Meyer
 *
 */
public class EntityTypeComparatorForModifications implements Comparator<Class<?>> {

  public static final Map<Class<?>, Integer> TYPE_ORDER = new HashMap<Class<?>, Integer>();

  static {

    // 1
    TYPE_ORDER.put(IncidentEntity.class, 1);
    TYPE_ORDER.put(VariableInstanceEntity.class, 1);
    TYPE_ORDER.put(IdentityLinkEntity.class, 1);

    TYPE_ORDER.put(EventSubscriptionEntity.class, 1);

    TYPE_ORDER.put(JobEntity.class, 1);
    TYPE_ORDER.put(MessageEntity.class, 1);
    TYPE_ORDER.put(TimerEntity.class, 1);
    TYPE_ORDER.put(EverLivingJobEntity.class, 1);

    TYPE_ORDER.put(MembershipEntity.class, 1);
    TYPE_ORDER.put(TenantMembershipEntity.class, 1);

    TYPE_ORDER.put(CaseSentryPartEntity.class, 1);

    TYPE_ORDER.put(ExternalTaskEntity.class, 1);
    TYPE_ORDER.put(Batch.class, 1);

    // 2
    TYPE_ORDER.put(TenantEntity.class, 2);
    TYPE_ORDER.put(GroupEntity.class, 2);
    TYPE_ORDER.put(UserEntity.class, 2);
    TYPE_ORDER.put(ByteArrayEntity.class, 2);
    TYPE_ORDER.put(TaskEntity.class, 2);
    TYPE_ORDER.put(JobDefinition.class, 2);

    // 3
    TYPE_ORDER.put(ExecutionEntity.class, 3);
    TYPE_ORDER.put(CaseExecutionEntity.class, 3);

    // 4
    TYPE_ORDER.put(ProcessDefinitionEntity.class, 4);
    TYPE_ORDER.put(CaseDefinitionEntity.class, 4);
    TYPE_ORDER.put(DecisionDefinitionEntity.class, 4);
    TYPE_ORDER.put(DecisionRequirementsDefinitionEntity.class, 4);
    TYPE_ORDER.put(ResourceEntity.class, 4);

    // 5
    TYPE_ORDER.put(DeploymentEntity.class, 5);

  }

  public int compare(Class<?> firstEntityType, Class<?> secondEntityType) {

    if(firstEntityType == secondEntityType) {
      return 0;
    }

    Integer firstIndex = TYPE_ORDER.get(firstEntityType);
    Integer secondIndex = TYPE_ORDER.get(secondEntityType);

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
