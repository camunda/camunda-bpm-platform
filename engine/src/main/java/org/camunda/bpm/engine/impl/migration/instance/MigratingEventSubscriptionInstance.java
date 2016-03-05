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

package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class MigratingEventSubscriptionInstance implements MigratingInstance, RemovingInstance, EmergingInstance {

  protected EventSubscriptionEntity eventSubscriptionEntity;
  protected ScopeImpl targetScope;

  protected EventSubscriptionDeclaration eventSubscriptionDeclaration;

  public MigratingEventSubscriptionInstance(EventSubscriptionEntity eventSubscriptionEntity, ScopeImpl targetScope) {
    this.eventSubscriptionEntity = eventSubscriptionEntity;
    this.targetScope = targetScope;
  }

  public MigratingEventSubscriptionInstance(EventSubscriptionEntity eventSubscriptionEntity) {
    this(eventSubscriptionEntity, null);
  }

  public MigratingEventSubscriptionInstance(EventSubscriptionDeclaration eventSubscriptionDeclaration) {
    this.eventSubscriptionDeclaration = eventSubscriptionDeclaration;
  }

  public void detachState() {
    eventSubscriptionEntity.setExecution(null);
  }

  public void attachState(ExecutionEntity newScopeExecution) {
    eventSubscriptionEntity.setExecution(newScopeExecution);
  }

  public void migrateState() {
    eventSubscriptionEntity.setActivity((ActivityImpl) targetScope);
  }

  public void migrateDependentEntities() {
    // do nothing
  }

  public void create(ExecutionEntity scopeExecution) {
    eventSubscriptionDeclaration.createSubscription(scopeExecution);
  }

  public void remove() {
    eventSubscriptionEntity.delete();
  }
}
