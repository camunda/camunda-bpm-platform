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

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public class ProcessApplicationEventPublisher implements ApplicationContextAware {

  private final ApplicationEventPublisher publisher;
  private ApplicationContext parentContext;

  public ProcessApplicationEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @EventListener
  public void handleApplicationReadyEvent(final ApplicationReadyEvent applicationReadyEvent) {
    publisher.publishEvent(new ProcessApplicationStartedEvent(applicationReadyEvent));
  }

  @EventListener
  public void handleContextStoppedEvent(final ContextClosedEvent contextStoppedEvent) {
    if (parentContext == contextStoppedEvent.getApplicationContext()) {
      publisher.publishEvent(new ProcessApplicationStoppedEvent(contextStoppedEvent));
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.parentContext = applicationContext;
  }
}
