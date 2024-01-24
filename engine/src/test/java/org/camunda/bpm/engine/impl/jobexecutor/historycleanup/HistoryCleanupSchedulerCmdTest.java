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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class HistoryCleanupSchedulerCmdTest {

    @Mock
    private CommandContext commandContext;
    @Mock
    private JobEntity jobEntity;
    @Mock
    private DbMetricsReporter dbMetricsReporter;
    @Mock
    private JobManager jobManager;
    @Spy
    private ProcessEngineConfigurationImpl engineConfigurationSpy;
    @Mock
    private MockedStatic<HistoryCleanupHelper> mockedHistoryCleanupHelper;

    private HistoryCleanupSchedulerCmd historyCleanupSchedulerCmd;
    private Map<String, Long> reports;
    private HistoryCleanupJobHandlerConfiguration configuration;
    private final String jobId = "testJobId";

    String METRICS_KEY = "Key";
    Long METRICS_VALUE = 123L;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(commandContext.getProcessEngineConfiguration()).thenReturn(engineConfigurationSpy);
        when(commandContext.getJobManager()).thenReturn(jobManager);
        when(jobManager.findJobById(jobId)).thenReturn(jobEntity);

        when(engineConfigurationSpy.getDbMetricsReporter()).thenReturn(dbMetricsReporter);

        reports = new HashMap<>();
        reports.put(METRICS_KEY, METRICS_VALUE);
        configuration = new HistoryCleanupJobHandlerConfiguration();

        // Mocking static methods
        mockedHistoryCleanupHelper = mockStatic(HistoryCleanupHelper.class);
        mockedHistoryCleanupHelper.when(() -> HistoryCleanupHelper.isWithinBatchWindow(any(Date.class), any(ProcessEngineConfigurationImpl.class))).thenReturn(false);
    }

    @After
    public void tearDown() {
        mockedHistoryCleanupHelper.close();
    }

    @Test
    public void shouldReportMetricsIfEnabled() {
        // given
        engineConfigurationSpy.setMetricsEnabled(true);
        engineConfigurationSpy.setHistoryCleanupMetricsEnabled(true);
        historyCleanupSchedulerCmd = new HistoryCleanupSchedulerCmd(false, reports, configuration, jobId);

        // when
        historyCleanupSchedulerCmd.execute(commandContext);

        // then
        verify(dbMetricsReporter, times(reports.size())).reportValueAtOnce(METRICS_KEY, METRICS_VALUE);
    }

    @Test
    public void shouldNotReportMetricsIfMetricsDisabled() {
        // given
        engineConfigurationSpy.setMetricsEnabled(false);
        engineConfigurationSpy.setHistoryCleanupMetricsEnabled(true);
        historyCleanupSchedulerCmd = new HistoryCleanupSchedulerCmd(false, reports, configuration, jobId);

        // when
        historyCleanupSchedulerCmd.execute(commandContext);

        // then
        verify(engineConfigurationSpy).isHistoryCleanupMetricsEnabled();
        verify(dbMetricsReporter, never()).reportValueAtOnce(anyString(), anyLong());
    }

    @Test
    public void shouldNotReportMetricsIfHistoryCleanupMetricsDisabled() {
        // given
        engineConfigurationSpy.setMetricsEnabled(true);
        engineConfigurationSpy.setHistoryCleanupMetricsEnabled(false);
        historyCleanupSchedulerCmd = new HistoryCleanupSchedulerCmd(false, reports, configuration, jobId);

        // when
        historyCleanupSchedulerCmd.execute(commandContext);

        // then
        verify(engineConfigurationSpy).isHistoryCleanupMetricsEnabled();
        verify(dbMetricsReporter, never()).reportValueAtOnce(anyString(), anyLong());
    }
}
