package org.camunda.bpm.engine.test.cfg;


import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProcessEngineConfigurationImplTest {


    @Test
    public void shouldInitHistoryLevelByObject() throws Exception {
        ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();
        config.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);

        config.initHistoryLevel();

        assertThat(config.getHistoryLevels().size(), is(4));
        assertThat(config.getHistoryLevel(), is(HistoryLevel.HISTORY_LEVEL_FULL));
        assertThat(config.getHistory(), is(HistoryLevel.HISTORY_LEVEL_FULL.getName()));
    }

    @Test
    public void shouldInitHistoryLevelByString() throws Exception {
        ProcessEngineConfigurationImpl config = new StandaloneInMemProcessEngineConfiguration();

        config.setHistory(HistoryLevel.HISTORY_LEVEL_FULL.getName());

        config.initHistoryLevel();

        assertThat(config.getHistoryLevels().size(), is(4));
        assertThat(config.getHistoryLevel(), is(HistoryLevel.HISTORY_LEVEL_FULL));
        assertThat(config.getHistory(), is(HistoryLevel.HISTORY_LEVEL_FULL.getName()));
    }
}