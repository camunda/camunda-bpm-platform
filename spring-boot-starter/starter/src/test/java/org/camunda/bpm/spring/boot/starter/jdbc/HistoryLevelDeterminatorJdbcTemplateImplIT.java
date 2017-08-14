package org.camunda.bpm.spring.boot.starter.jdbc;

import static org.junit.Assert.assertEquals;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { HistoryLevelDeterminatorJdbcTemplateImplTestApplication.class })
@Transactional
public class HistoryLevelDeterminatorJdbcTemplateImplIT {

  @Autowired
  private HistoryLevelDeterminator historyLevelDeterminator;

  @Test
  public void test() {
    assertEquals("full", historyLevelDeterminator.determineHistoryLevel());
  }
}
