package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.assertj.core.api.Condition;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.Verifier;

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
