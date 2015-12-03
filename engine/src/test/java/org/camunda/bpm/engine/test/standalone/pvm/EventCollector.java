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

package org.camunda.bpm.engine.test.standalone.pvm;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class EventCollector implements ExecutionListener {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public List<String> events = new ArrayList<String>();

  public void notify(DelegateExecution execution) throws Exception {
    PvmExecutionImpl executionImpl = (PvmExecutionImpl) execution;
    LOG.debug("collecting event: "+execution.getEventName()+" on "+executionImpl.getEventSource());
    events.add(execution.getEventName()+" on "+executionImpl.getEventSource());
  }

  public String toString() {
    StringBuilder text = new StringBuilder();
    for (String event: events) {
      text.append(event);
      text.append("\n");
    }
    return text.toString();

  }

}
