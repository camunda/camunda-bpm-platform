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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.event.SignalEventHandler;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingEventSubscriptionInstance;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class EventSubscriptionInstanceHandler implements MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> {

  public static final Set<String> SUPPORTED_EVENT_TYPES = new HashSet<String>();
  static {
    SUPPORTED_EVENT_TYPES.add(MessageEventHandler.EVENT_HANDLER_TYPE);
    SUPPORTED_EVENT_TYPES.add(SignalEventHandler.EVENT_HANDLER_TYPE);
  }

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingActivityInstance owningInstance, List<EventSubscriptionEntity> elements) {

    Map<String, EventSubscriptionDeclaration> targetDeclarations = getDeclarationsByTriggeringActivity(owningInstance.getTargetScope());

    for (EventSubscriptionEntity eventSubscription : elements) {
      if (!getSupportedEventTypes().contains(eventSubscription.getEventType())) {
        // ignore unsupported event subscriptions
        continue;
      }

      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(eventSubscription.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if (targetActivity != null && owningInstance.migratesTo(targetActivity.getEventScope())) {
        // the event subscription is migrated
        EventSubscriptionDeclaration targetDeclaration = targetDeclarations.remove(targetActivity.getId());

        owningInstance.addMigratingDependentInstance(
            new MigratingEventSubscriptionInstance(eventSubscription, targetActivity, migrationInstruction.isUpdateEventTrigger(), targetDeclaration));

      }
      else {
        // the event subscription will be removed
        owningInstance.addRemovingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscription));

      }

      parseContext.consume(eventSubscription);
    }

    if (owningInstance.migrates()) {
      addEmergingEventSubscriptions(owningInstance, targetDeclarations.values());
    }
  }

  protected Set<String> getSupportedEventTypes() {
    return SUPPORTED_EVENT_TYPES;
  }

  protected Map<String, EventSubscriptionDeclaration> getDeclarationsByTriggeringActivity(ScopeImpl eventScope) {
    Map<String, EventSubscriptionDeclaration> declarations = EventSubscriptionDeclaration.getDeclarationsForScope(eventScope);

    return new HashMap<String, EventSubscriptionDeclaration>(declarations);
  }

  protected void addEmergingEventSubscriptions(MigratingActivityInstance owningInstance, Collection<EventSubscriptionDeclaration> emergingDeclarations) {
    for (EventSubscriptionDeclaration eventSubscriptionDeclaration : emergingDeclarations) {
      // the event subscription will be created
      owningInstance.addEmergingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscriptionDeclaration));
    }
  }

}
