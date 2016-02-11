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
package org.camunda.bpm.engine.impl.cmmn.handler;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.EventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TimerEventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.TimerEventListener;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author subhro
 */
public class TimerEventListenerItemHandler extends ItemHandler{

    private static Logger logger=Logger.getLogger(TimerEventListenerItemHandler.class.getSimpleName());

    @Override
    protected List<String> getStandardEvents(CmmnElement element) {
        return EVENT_LISTENER_OR_MILESTONE_EVENTS;
    }

    @Override
    protected CmmnActivityBehavior getActivityBehavior() {
        logger.info("Getting CMMN activity behavior for TimeEventListnerItemHandler");
        return new TimerEventListenerActivityBehavior();
    }

    @Override
    protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        super.initializeActivity(element, activity, context);
    }

    @Override
    protected void initializeEntryCriterias(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        // entry criteria is not applicable on event listeners
    }
    @Override
    protected void initializeExitCriterias(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        // exit criteria is not applicable on milestones
    }
    @Override
    protected void initializeRepetitionRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        // repetition rule is not applicable on event listeners
    }
    @Override
    protected void initializeRequiredRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        // required rule is not applicable on event listeners
    }
    @Override
    protected void initializeManualActivationRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
        // manual activation rule is not applicable on event listeners
    }
}
