package org.camunda.bpm.spring.boot.starter.configuration.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

public class NeedsHistoryAutoConfigurationConditionTest {

  @Test
  public void isHistoryAutoSupportedTest() {
    NeedsHistoryAutoConfigurationCondition condition = new NeedsHistoryAutoConfigurationCondition();
    assertFalse(condition.isHistoryAutoSupported());
    condition.historyAutoFieldName = "DB_SCHEMA_UPDATE_FALSE";
    assertFalse(condition.isHistoryAutoSupported());
  }

  @Test
  public void needsNoAdditionalConfigurationTest1() {
    NeedsHistoryAutoConfigurationCondition condition = spy(new NeedsHistoryAutoConfigurationCondition());
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    assertFalse(condition.needsAdditionalConfiguration(context));
  }

  @Test
  public void needsNoAdditionalConfigurationTest2() {
    NeedsHistoryAutoConfigurationCondition condition = spy(new NeedsHistoryAutoConfigurationCondition());
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getProperty("camunda.bpm.history-level")).thenReturn(NeedsHistoryAutoConfigurationCondition.HISTORY_AUTO);
    when(condition.isHistoryAutoSupported()).thenReturn(true);
    assertFalse(condition.needsAdditionalConfiguration(context));
  }

  @Test
  public void needsAdditionalConfigurationTest() {
    NeedsHistoryAutoConfigurationCondition condition = spy(new NeedsHistoryAutoConfigurationCondition());
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getProperty("camunda.bpm.history-level")).thenReturn(NeedsHistoryAutoConfigurationCondition.HISTORY_AUTO);
    when(condition.isHistoryAutoSupported()).thenReturn(false);
    assertTrue(condition.needsAdditionalConfiguration(context));
  }

  @Test
  public void getMatchOutcomeMatchTest() {
    NeedsHistoryAutoConfigurationCondition condition = spy(new NeedsHistoryAutoConfigurationCondition());
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getProperty("camunda.bpm.history-level")).thenReturn(NeedsHistoryAutoConfigurationCondition.HISTORY_AUTO);
    when(condition.needsAdditionalConfiguration(context)).thenReturn(true);
    assertTrue(condition.getMatchOutcome(context, null).isMatch());
  }

  @Test
  public void getMatchOutcomeNoMatchTest() {
    NeedsHistoryAutoConfigurationCondition condition = spy(new NeedsHistoryAutoConfigurationCondition());
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getProperty("camunda.bpm.history-level")).thenReturn(NeedsHistoryAutoConfigurationCondition.HISTORY_AUTO);
    when(condition.needsAdditionalConfiguration(context)).thenReturn(false);
    assertFalse(condition.getMatchOutcome(context, null).isMatch());
  }

}
