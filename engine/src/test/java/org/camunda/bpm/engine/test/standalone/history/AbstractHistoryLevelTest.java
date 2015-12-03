package org.camunda.bpm.engine.test.standalone.history;

import org.camunda.bpm.engine.impl.history.AbstractHistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class AbstractHistoryLevelTest {


  public static class MyHistoryLevel extends AbstractHistoryLevel {

    @Override
    public int getId() {
      return 4711;
    }

    @Override
    public String getName() {
      return "myName";
    }

    @Override
    public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
      return false;
    }
  }

  @Test
  public void ensureCorrectToString() {
    Assert.assertThat(new MyHistoryLevel().toString(), CoreMatchers.is("MyHistoryLevel(name=myName, id=4711)"));
  }
}