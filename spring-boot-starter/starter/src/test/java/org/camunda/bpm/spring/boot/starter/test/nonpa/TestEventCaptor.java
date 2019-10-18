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
package org.camunda.bpm.spring.boot.starter.test.nonpa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Stack;

@Component
public class TestEventCaptor {

  public Stack<HistoryEvent> historyEvents = new Stack<>();
  public Stack<TaskEvent> taskEvents = new Stack<>();
  public Stack<TaskEvent> immutableTaskEvents = new Stack<>();
  public Stack<ExecutionEvent> executionEvents = new Stack<>();
  public Stack<ExecutionEvent> immutableExecutionEvents = new Stack<>();

  // Transactional Listener Events
  public Stack<HistoryEvent> transactionHistoryEvents = new Stack<>();
  public Stack<TaskEvent> transactionTaskEvents = new Stack<>();
  public Stack<TaskEvent> transactionImmutableTaskEvents = new Stack<>();
  public Stack<ExecutionEvent> transactionExecutionEvents = new Stack<>();
  public Stack<ExecutionEvent> transactionImmutableExecutionEvents = new Stack<>();

  @EventListener
  public void onEvent(HistoryEvent event) {
    historyEvents.push(event);
  }

  @EventListener
  public void onEvent(DelegateExecution event) {
    executionEvents.push(new ExecutionEvent(event));
  }

  @EventListener
  public void onEvent(DelegateTask event) {
    taskEvents.push(new TaskEvent(event));
  }

  @EventListener
  public void onEvent(ExecutionEvent event) {
    immutableExecutionEvents.push(event);
  }

  @EventListener
  public void onEvent(TaskEvent event) {
    immutableTaskEvents.push(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onTransactionalEvent(HistoryEvent event) {
    transactionHistoryEvents.push(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onTransactionalEvent(DelegateExecution event) {
    transactionExecutionEvents.push(new ExecutionEvent(event));
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onTransactionalEvent(DelegateTask event) {
    transactionTaskEvents.push(new TaskEvent(event));
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onTransactionalEvent(ExecutionEvent event) {
    transactionImmutableExecutionEvents.push(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onTransactionalEvent(TaskEvent event) {
    transactionImmutableTaskEvents.push(event);
  }

  public void clear() {
    historyEvents.clear();
    taskEvents.clear();
    immutableTaskEvents.clear();
    executionEvents.clear();
    immutableExecutionEvents.clear();

    transactionHistoryEvents.clear();
    transactionTaskEvents.clear();
    transactionImmutableTaskEvents.clear();
    transactionExecutionEvents.clear();
    transactionImmutableExecutionEvents.clear();
  }

}
