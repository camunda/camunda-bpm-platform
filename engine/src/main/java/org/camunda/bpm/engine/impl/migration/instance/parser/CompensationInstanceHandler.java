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
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.migration.instance.MigratingCompensationEventSubscriptionInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingEventScopeInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingScopeInstance;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompensationInstanceHandler implements MigratingInstanceParseHandler<EventSubscriptionEntity> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, EventSubscriptionEntity element) {

    MigratingProcessElementInstance migratingInstance;
    if (element.getConfiguration() != null) {
      migratingInstance = createMigratingEventScopeInstance(parseContext, element);
    }
    else {
      migratingInstance = createMigratingEventSubscriptionInstance(parseContext, element);
    }


    ExecutionEntity owningExecution = element.getExecution();
    MigratingScopeInstance parentInstance = null;
    if (owningExecution.isEventScope()) {
      parentInstance = parseContext.getMigratingCompensationInstanceByExecutionId(owningExecution.getId());
    }
    else {
      parentInstance = parseContext.getMigratingActivityInstanceById(owningExecution.getParentActivityInstanceId());
    }
    migratingInstance.setParent(parentInstance);
  }

  protected MigratingProcessElementInstance createMigratingEventSubscriptionInstance(MigratingInstanceParseContext parseContext,
      EventSubscriptionEntity element) {
    ActivityImpl compensationHandler = parseContext.getSourceProcessDefinition().findActivity(element.getActivityId());

    MigrationInstruction migrationInstruction = getMigrationInstruction(parseContext, compensationHandler);

    ActivityImpl targetScope = null;
    if (migrationInstruction != null) {
      ActivityImpl targetEventScope = (ActivityImpl) parseContext.getTargetActivity(migrationInstruction).getEventScope();
      targetScope = targetEventScope.findCompensationHandler();
    }

    MigratingCompensationEventSubscriptionInstance migratingCompensationInstance =
        parseContext.getMigratingProcessInstance().addCompensationSubscriptionInstance(
            migrationInstruction,
            element,
            compensationHandler,
            targetScope);

    parseContext.consume(element);

    return migratingCompensationInstance;
  }

  protected MigratingProcessElementInstance createMigratingEventScopeInstance(MigratingInstanceParseContext parseContext,
      EventSubscriptionEntity element) {

    ActivityImpl compensatingActivity = parseContext.getSourceProcessDefinition().findActivity(element.getActivityId());

    MigrationInstruction migrationInstruction = getMigrationInstruction(parseContext, compensatingActivity);

    ActivityImpl eventSubscriptionTargetScope = null;

    if (migrationInstruction != null) {
      if (compensatingActivity.isCompensationHandler()) {
        ActivityImpl targetEventScope = (ActivityImpl) parseContext.getTargetActivity(migrationInstruction).getEventScope();
        eventSubscriptionTargetScope = targetEventScope.findCompensationHandler();
      }
      else {
        eventSubscriptionTargetScope = parseContext.getTargetActivity(migrationInstruction);
      }
    }

    ExecutionEntity eventScopeExecution = CompensationUtil.getCompensatingExecution(element);
    MigrationInstruction eventScopeInstruction = parseContext.findSingleMigrationInstruction(eventScopeExecution.getActivityId());
    ActivityImpl targetScope = parseContext.getTargetActivity(eventScopeInstruction);

    MigratingEventScopeInstance migratingCompensationInstance =
        parseContext.getMigratingProcessInstance().addEventScopeInstance(
          eventScopeInstruction,
          eventScopeExecution,
          eventScopeExecution.getActivity(),
          targetScope,
          migrationInstruction,
          element,
          compensatingActivity,
          eventSubscriptionTargetScope);

    parseContext.consume(element);
    parseContext.submit(migratingCompensationInstance);

    parseDependentEntities(parseContext, migratingCompensationInstance);

    return migratingCompensationInstance;
  }

  protected MigrationInstruction getMigrationInstruction(MigratingInstanceParseContext parseContext, ActivityImpl activity) {
    if (activity.isCompensationHandler()) {
      Properties compensationHandlerProperties = activity.getProperties();
      ActivityImpl eventTrigger = compensationHandlerProperties.get(BpmnProperties.COMPENSATION_BOUNDARY_EVENT);
      if (eventTrigger == null) {
        eventTrigger = compensationHandlerProperties.get(BpmnProperties.INITIAL_ACTIVITY);
      }

      return parseContext.findSingleMigrationInstruction(eventTrigger.getActivityId());
    }
    else {
      return parseContext.findSingleMigrationInstruction(activity.getActivityId());
    }
  }

  protected void parseDependentEntities(MigratingInstanceParseContext parseContext, MigratingEventScopeInstance migratingInstance) {

    ExecutionEntity representativeExecution = migratingInstance.resolveRepresentativeExecution();

    List<VariableInstanceEntity> variables = new ArrayList<VariableInstanceEntity>(
        representativeExecution.getVariablesInternal());
    parseContext.handleDependentVariables(migratingInstance, variables);
  }

}
