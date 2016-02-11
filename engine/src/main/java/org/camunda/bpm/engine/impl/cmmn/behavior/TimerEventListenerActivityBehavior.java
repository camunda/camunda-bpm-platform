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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.handler.TimerEventListenerItemHandler;

import java.util.logging.Logger;

/**
 * @author subhro
 */
public class TimerEventListenerActivityBehavior extends EventListenerActivityBehavior {
    protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;
    private static final Logger logger=Logger.getLogger(TimerEventListenerActivityBehavior.class.getSimpleName());

    @Override
    public void created(CmmnActivityExecution execution) {
        // TODO: implement this:
        logger.info("Created timer event listener");
        // (2) in case of TimerEventListener we have to check
        // whether the timer must be triggered, when a transition
        // on another plan item or case file item happens!
        // Handle trigger expression property
        // handle planItemStartTrigger property
        // handle caseFileItemStartTrigger property



    }

    protected String getTypeName() {
        return "timer event listener";
    }
}
