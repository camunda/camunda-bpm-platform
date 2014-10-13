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

package org.camunda.bpm.engine.impl.history.transformer;

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.CLOSE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.COMPLETE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.CREATE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.RE_ACTIVATE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.SUSPEND;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.TERMINATE;
import static org.camunda.bpm.engine.impl.history.event.HistoryEventTypes.CASE_INSTANCE_CLOSE;
import static org.camunda.bpm.engine.impl.history.event.HistoryEventTypes.CASE_INSTANCE_CREATE;
import static org.camunda.bpm.engine.impl.history.event.HistoryEventTypes.CASE_INSTANCE_UPDATE;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.transformer.AbstractCmmnTransformListener;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.producer.CmmnHistoryEventProducer;
import org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel;

/**
 * @author Sebastian Menski
 */
public class CmmnHistoryTransformListener extends AbstractCmmnTransformListener {

  // Cached listeners
  // listeners can be reused for a given process engine instance but cannot be cached in static fields since
  // different process engine instances on the same Classloader may have different HistoryEventProducer
  // configurations wired
  protected CaseExecutionListener CASE_INSTANCE_CREATE_LISTENER;
  protected CaseExecutionListener CASE_INSTANCE_UPDATE_LISTENER;
  protected CaseExecutionListener CASE_INSTANCE_CLOSE_LISTENER;

  // The history level set in the process engine configuration
  protected HistoryLevel historyLevel;

  public CmmnHistoryTransformListener(HistoryLevel historyLevel, CmmnHistoryEventProducer historyEventProducer) {
    this.historyLevel = historyLevel;
    initCaseExecutionListeners(historyEventProducer, historyLevel);
  }

  protected void initCaseExecutionListeners(CmmnHistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    CASE_INSTANCE_CREATE_LISTENER = new CaseInstanceCreateListener(historyEventProducer, historyLevel);
    CASE_INSTANCE_UPDATE_LISTENER = new CaseInstanceUpdateListener(historyEventProducer, historyLevel);
    CASE_INSTANCE_CLOSE_LISTENER = new CaseInstanceCloseListener(historyEventProducer, historyLevel);
  }

  public void transformCasePlanModel(CasePlanModel casePlanModel, CmmnActivity activity) {
    if (historyLevel.isHistoryEventProduced(CASE_INSTANCE_CREATE, null)) {
      activity.addListener(CREATE, CASE_INSTANCE_CREATE_LISTENER);
    }
    if (historyLevel.isHistoryEventProduced(CASE_INSTANCE_UPDATE, null)) {
      String[] updateEvents = {COMPLETE, TERMINATE, SUSPEND, RE_ACTIVATE};
      for (String updateEvent : updateEvents) {
        activity.addListener(updateEvent, CASE_INSTANCE_UPDATE_LISTENER);
      }
    }
    if (historyLevel.isHistoryEventProduced(CASE_INSTANCE_CLOSE, null)) {
      activity.addListener(CLOSE, CASE_INSTANCE_CLOSE_LISTENER);
    }
  }

}
