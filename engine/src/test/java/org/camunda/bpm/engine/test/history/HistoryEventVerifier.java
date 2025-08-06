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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.assertj.core.api.Condition;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.Verifier;

/**
 * @author Edoardo Patti
 */
public class HistoryEventVerifier extends Verifier {

  private final TestEventHandler eventHandler;
  private final List<Condition<HistoryEvent>> hasConditions = new ArrayList<>(50);
  private final List<Condition<HistoryEvent>> isConditions = new ArrayList<>(50);


  public HistoryEventVerifier(TestEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  public void historyEventHas(String message, Predicate<HistoryEvent> predicate) {
    Condition<HistoryEvent> hasCondition = getCondition(message, predicate);
    this.hasConditions.add(hasCondition);
  }

  public void historyEventIs(String message, Predicate<HistoryEvent> predicate) {
    Condition<HistoryEvent> isCondition = getCondition(message, predicate);
    this.isConditions.add(isCondition);
  }

  @NotNull
  private static Condition<HistoryEvent> getCondition(String message, Predicate<HistoryEvent> predicate) {

    return new Condition<>(message) {

      @Override
      public boolean matches(HistoryEvent value) {
        return predicate.test(value);
      }
    };
  }

  @Override
  protected void verify() throws Throwable {
    while (this.eventHandler.peek() != null) {
      final HistoryEvent evt = this.eventHandler.poll();
      hasConditions.forEach(condition -> assertThat(evt).has(condition));
      isConditions.forEach(condition -> assertThat(evt).is(condition));
    }
  }
}
