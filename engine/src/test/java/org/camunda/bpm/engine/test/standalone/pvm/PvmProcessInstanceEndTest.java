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

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmProcessInstanceEndTest extends PvmTestCase {

  public void testSimpleProcessInstanceEnd() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("wait")
      .endActivity()
      .createActivity("wait")
        .behavior(new WaitState())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    System.err.println(eventCollector);
    
    processInstance.deleteCascade("test");

    System.err.println();
    System.err.println(eventCollector);
  }
}
