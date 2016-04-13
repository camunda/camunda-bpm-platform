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
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingEventSubscriptionInstance;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class EventSubscriptionInstanceHandler implements MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingActivityInstance owningInstance, List<EventSubscriptionEntity> elements) {
    List<String> migratedEventSubscriptionTargetActivityIds = new ArrayList<String>();

    for (EventSubscriptionEntity eventSubscription : elements) {
      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(eventSubscription.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if (targetActivity != null && owningInstance.migratesTo(targetActivity.getEventScope())) {
        // the event subscription is migrated
        migratedEventSubscriptionTargetActivityIds.add(targetActivity.getId());
        owningInstance.addMigratingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscription, targetActivity));

      }
      else {
        // the event subscription will be removed
        owningInstance.addRemovingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscription));

      }

      parseContext.consume(eventSubscription);
    }

    if (owningInstance.migrates()) {
      addEmergingEventSubscriptions(owningInstance, migratedEventSubscriptionTargetActivityIds);
    }

  }

  protected void addEmergingEventSubscriptions(MigratingActivityInstance owningInstance, List<String> migratingEventSubscriptionActivityIds) {
    for (EventSubscriptionDeclaration eventSubscriptionDeclaration : EventSubscriptionDeclaration.getDeclarationsForScope(owningInstance.getTargetScope())) {
      if (!migratingEventSubscriptionActivityIds.contains(eventSubscriptionDeclaration.getActivityId())) {
        // the event subscription will be created
        owningInstance.addEmergingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscriptionDeclaration));
      }
    }
  }
}
