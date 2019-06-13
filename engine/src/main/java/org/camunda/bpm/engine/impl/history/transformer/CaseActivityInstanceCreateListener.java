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
package org.camunda.bpm.engine.impl.history.transformer;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.CmmnHistoryEventProducer;

/**
 * @author Sebastian Menski
 */
public class CaseActivityInstanceCreateListener extends HistoryCaseExecutionListener {

  public CaseActivityInstanceCreateListener(CmmnHistoryEventProducer historyEventProducer) {
    super(historyEventProducer);
  }

  protected HistoryEvent createHistoryEvent(DelegateCaseExecution caseExecution) {
    ensureHistoryLevelInitialized();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_CREATE, caseExecution)) {
      return eventProducer.createCaseActivityInstanceCreateEvt(caseExecution);
    }
    else {
      return null;
    }
  }

}
