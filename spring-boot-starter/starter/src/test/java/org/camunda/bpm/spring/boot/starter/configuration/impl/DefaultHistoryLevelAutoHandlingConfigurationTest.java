package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.jdbc.HistoryLevelDeterminator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultHistoryLevelAutoHandlingConfigurationTest {

  @Mock
  private SpringProcessEngineConfiguration springProcessEngineConfiguration;

  @Mock
  private HistoryLevelDeterminator historyLevelDeterminator;

  private CamundaBpmProperties camundaBpmProperties;

  private DefaultHistoryLevelAutoHandlingConfiguration historyLevelAutoHandlingConfiguration;

  @Before
  public void before() {
    camundaBpmProperties = new CamundaBpmProperties();
    historyLevelAutoHandlingConfiguration = new DefaultHistoryLevelAutoHandlingConfiguration();
    historyLevelAutoHandlingConfiguration.camundaBpmProperties = camundaBpmProperties;
    historyLevelAutoHandlingConfiguration.historyLevelDeterminator = historyLevelDeterminator;
  }

  @Test
  public void acceptTest() {
    when(historyLevelDeterminator.determineHistoryLevel()).thenReturn("audit");
    historyLevelAutoHandlingConfiguration.preInit(springProcessEngineConfiguration);
    verify(historyLevelDeterminator).determineHistoryLevel();
    verify(springProcessEngineConfiguration).setHistory(Mockito.anyString());
  }

  @Test
  public void notAcceptTest() {
    when(historyLevelDeterminator.determineHistoryLevel()).thenReturn(null);
    historyLevelAutoHandlingConfiguration.preInit(springProcessEngineConfiguration);
    verify(historyLevelDeterminator).determineHistoryLevel();
    verify(springProcessEngineConfiguration, times(0)).setHistory(Mockito.anyString());
  }

}
