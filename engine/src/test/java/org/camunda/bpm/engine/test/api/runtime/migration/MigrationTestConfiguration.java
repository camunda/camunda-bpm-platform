/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.BpmnEventFactory;
import org.camunda.bpm.engine.test.util.BpmnEventTrigger;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Map;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public abstract class MigrationTestConfiguration implements BpmnEventFactory {

  public abstract String getEventName();

  public void assertEventSubscriptionMigration(MigrationTestRule testHelper, String activityIdBefore, String activityIdAfter) {
    testHelper.assertEventSubscriptionMigrated(activityIdBefore, activityIdAfter, getEventName());
  }

  public MigrationPlanBuilder createMigrationPlanBuilder(ProcessEngineRule rule, String srcProcDefId, String trgProcDefId) {
    return rule.getRuntimeService().createMigrationPlan(srcProcDefId, trgProcDefId);
  }

  public MigrationPlanBuilder createMigrationPlanBuilder(ProcessEngineRule rule, String srcProcDefId, String trgProcDefId, Map<String, String> activities) {
    MigrationPlanBuilder migrationPlanBuilder = createMigrationPlanBuilder(rule, srcProcDefId, trgProcDefId);

    for (String key : activities.keySet()) {
      migrationPlanBuilder.mapActivities(key, activities.get(key));
    }
    return migrationPlanBuilder;
  }
}
