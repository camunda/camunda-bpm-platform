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
package org.camunda.bpm.spring.boot.starter.event;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

public class JobExecutorStartingEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutorStartingEventListener.class);

  @Autowired
  protected JobExecutor jobExecutor;

  @EventListener
  public void handleProcessApplicationStartedEvent(ProcessApplicationStartedEvent processApplicationStartedEvent) {
    activate();
  }

  protected void activate() {
    if (!jobExecutor.isActive()) {
      jobExecutor.start();
    } else {
      LOGGER.info("job executor is already active");
    }
  }

}
