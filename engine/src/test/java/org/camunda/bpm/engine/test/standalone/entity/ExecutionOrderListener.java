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
package org.camunda.bpm.engine.test.standalone.entity;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionOrderListener implements ExecutionListener {

  protected static List<ActivitySequenceCounterMap> activityExecutionOrder = new ArrayList<ActivitySequenceCounterMap>();

  public void notify(DelegateExecution execution) throws Exception {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    long sequenceCounter = executionEntity.getSequenceCounter();
    String activityId = executionEntity.getActivityId();

    activityExecutionOrder.add(new ActivitySequenceCounterMap(activityId, sequenceCounter));
  }

  public static void clearActivityExecutionOrder() {
    activityExecutionOrder.clear();
  }

  public static List<ActivitySequenceCounterMap> getActivityExecutionOrder() {
    return activityExecutionOrder;
  }

  protected class ActivitySequenceCounterMap {

    protected String activityId;
    protected long sequenceCounter;

    public ActivitySequenceCounterMap(String activityId, long sequenceCounter) {
      this.activityId = activityId;
      this.sequenceCounter = sequenceCounter;
    }

    public String getActivityId() {
      return activityId;
    }

    public long getSequenceCounter() {
      return sequenceCounter;
    }

  }

}
