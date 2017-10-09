package org.camunda.bpm.spring.boot.starter.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.HistoryLevelAudit;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class HistoryLevelDeterminatorJdbcTemplateImplTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private CamundaBpmProperties camundaBpmProperties;

  @Before
  public void before() {
    camundaBpmProperties = new CamundaBpmProperties();
  }

  @Test
  public void afterPropertiesSetTest1() throws Exception {
    camundaBpmProperties = new CamundaBpmProperties();
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
    assertEquals(ProcessEngineConfiguration.HISTORY_FULL, determinator.defaultHistoryLevel);
  }

  @Test
  public void afterPropertiesSetTest2() throws Exception {
    camundaBpmProperties = new CamundaBpmProperties();
    final String historyLevelDefault = "defaultValue";
    camundaBpmProperties.setHistoryLevelDefault(historyLevelDefault);
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
    assertEquals(historyLevelDefault, determinator.defaultHistoryLevel);
  }

  @Test(expected = IllegalArgumentException.class)
  public void afterPropertiesSetTest3() throws Exception {
    new HistoryLevelDeterminatorJdbcTemplateImpl().afterPropertiesSet();
  }

  @Test(expected = IllegalArgumentException.class)
  public void afterPropertiesSetTest4() throws Exception {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.afterPropertiesSet();
  }

  @Test(expected = IllegalArgumentException.class)
  public void afterPropertiesSetTest5() throws Exception {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
  }

  @Test
  public void determinedTest() throws Exception {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    final String defaultHistoryLevel = "test";
    determinator.setDefaultHistoryLevel(defaultHistoryLevel);
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
    HistoryLevel historyLevel = new HistoryLevelAudit();
    when(jdbcTemplate.queryForObject(determinator.getSql(), Integer.class)).thenReturn(historyLevel.getId());
    String determineHistoryLevel = determinator.determineHistoryLevel();
    assertEquals(historyLevel.getName(), determineHistoryLevel);
  }

  @Test
  public void determinedExceptionIgnoringTest() throws Exception {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    final String defaultHistoryLevel = "test";
    determinator.setDefaultHistoryLevel(defaultHistoryLevel);
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
    when(jdbcTemplate.queryForObject(determinator.getSql(), Integer.class)).thenThrow(new DataRetrievalFailureException(""));
    String determineHistoryLevel = determinator.determineHistoryLevel();
    assertEquals(determinator.defaultHistoryLevel, determineHistoryLevel);
    verify(jdbcTemplate).queryForObject(determinator.getSql(), Integer.class);
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void determinedExceptionNotIgnoringTest() throws Exception {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setIgnoreDataAccessException(false);
    final String defaultHistoryLevel = "test";
    determinator.setDefaultHistoryLevel(defaultHistoryLevel);
    determinator.setJdbcTemplate(jdbcTemplate);
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    determinator.afterPropertiesSet();
    when(jdbcTemplate.queryForObject(determinator.getSql(), Integer.class)).thenThrow(new DataRetrievalFailureException(""));
    determinator.determineHistoryLevel();
  }

  @Test
  public void getSqlTest() {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    determinator.setCamundaBpmProperties(camundaBpmProperties);
    assertEquals("SELECT VALUE_ FROM ACT_GE_PROPERTY WHERE NAME_='historyLevel'", determinator.getSql());
    camundaBpmProperties.getDatabase().setTablePrefix("TEST_");
    assertEquals("SELECT VALUE_ FROM TEST_ACT_GE_PROPERTY WHERE NAME_='historyLevel'", determinator.getSql());
  }

  @Test
  public void getHistoryLevelFromTest() {
    HistoryLevelDeterminatorJdbcTemplateImpl determinator = new HistoryLevelDeterminatorJdbcTemplateImpl();
    assertEquals(determinator.getDefaultHistoryLevel(), determinator.getHistoryLevelFrom(-1));
    assertFalse(determinator.historyLevels.isEmpty());
    HistoryLevel customHistoryLevel = new HistoryLevel() {

      @Override
      public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
        return false;
      }

      @Override
      public String getName() {
        return "custom";
      }

      @Override
      public int getId() {
        return Integer.MAX_VALUE;
      }
    };

    determinator.addCustomHistoryLevels(Collections.singleton(customHistoryLevel));
    assertTrue(determinator.historyLevels.contains(customHistoryLevel));

    for (HistoryLevel historyLevel : determinator.historyLevels) {
      assertEquals(historyLevel.getName(), determinator.getHistoryLevelFrom(historyLevel.getId()));
    }
  }

}
