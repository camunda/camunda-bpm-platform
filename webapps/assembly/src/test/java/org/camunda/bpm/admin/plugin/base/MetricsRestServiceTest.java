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
package org.camunda.bpm.admin.plugin.base;

import org.assertj.core.groups.Tuple;
import org.camunda.bpm.admin.impl.plugin.resources.MetricsRestService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter;
import org.camunda.bpm.engine.impl.persistence.entity.TaskMeterLogEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

public class MetricsRestServiceTest extends AbstractAdminPluginTest {

  private final MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
  private MetricsRestService resource;
  private UriInfo uriInfo;
  private ManagementService managementService;
  private DbMetricsReporter dbMetricsReporter;
  private MetricsRegistry metricsRegistry;

  @Before
  public void setUp() throws Exception {
    super.before();

    var processEngine = getProcessEngine();
    var processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    dbMetricsReporter = processEngineConfiguration.getDbMetricsReporter();
    metricsRegistry = processEngineConfiguration.getMetricsRegistry();
    managementService = processEngine.getManagementService();

    resource = new MetricsRestService(processEngine.getName());

    uriInfo = Mockito.mock(UriInfo.class);
    Mockito.doReturn(queryParameters).when(uriInfo).getQueryParameters();
  }

  @After
  public void tearDown() {
    queryParameters.clear();
    managementService.deleteMetrics(null);
    managementService.deleteTaskMetrics(null);
    ClockUtil.reset();
  }

  @Test
  public void shouldThrowExceptionWhenSubscriptionStartDateNotProvided() {
    // given
    queryParameters.add("subscriptionStartDate", "");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage("subscriptionStartDate parameter has invalid value: null");
  }

  @Test
  public void shouldThrowExceptionWhenSubscriptionStartDateInvalid() {
    // given
    queryParameters.add("subscriptionStartDate", "notDate");
    queryParameters.add("groupBy", "month");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage(
            "Cannot set query parameter 'subscriptionStartDate' to value 'notDate': Cannot convert value \"notDate\" to java type java.util.Date");
  }

  @Test
  public void shouldThrowExceptionWhenSubscriptionStartDateNotInPast() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2100).withMonthOfYear(1).withDayOfMonth(31).toString());
    queryParameters.add("groupBy", "month");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageStartingWith("subscriptionStartDate parameter has invalid value: Sun Jan 31");
  }

  @Test
  public void shouldThrowExceptionWhenGroupByNotProvided() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class).hasMessage("groupBy parameter has invalid value: null");
  }

  @Test
  public void shouldThrowExceptionWhenGroupByInvalid() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");
    queryParameters.add("groupBy", "day");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class).hasMessage("groupBy parameter has invalid value: day");
  }

  @Test
  public void shouldThrowExceptionWhenStartDateInvalid() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");
    queryParameters.add("startDate", "notDate");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage(
            "Cannot set query parameter 'startDate' to value 'notDate': Cannot convert value \"notDate\" to java type java.util.Date");
  }

  @Test
  public void shouldThrowExceptionWhenEndDateInvalid() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");
    queryParameters.add("endDate", "notDate");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage(
            "Cannot set query parameter 'endDate' to value 'notDate': Cannot convert value \"notDate\" to java type java.util.Date");
  }

  @Test
  public void shouldThrowExceptionWhenStartDateNotBeforeEndDate() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");
    queryParameters.add("startDate", "2023-01-31T16:54:00.000+0100");
    queryParameters.add("endDate", "2023-01-31T16:54:00.000+0100");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class).hasMessage("endDate parameter must be after startDate");
  }

  @Test
  public void shouldThrowExceptionWhenMetricsInvalid() {
    // given
    queryParameters.add("subscriptionStartDate", "2024-01-01");
    queryParameters.add("groupBy", "month");
    queryParameters.add("metrics", "a,process-instances");

    // when
    assertThatThrownBy(() -> resource.getAggregatedMetrics(uriInfo))
        // then
        .isInstanceOf(InvalidRequestException.class)
        .hasMessage("Cannot set query parameter 'metrics' to value 'a,process-instances'");
  }

  @Test
  public void shouldReturnAggregatedMetricsFilteredByMetricsParameter() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "year");
    queryParameters.add("metrics", String.format("%s,%s", Metrics.PROCESS_INSTANCES, Metrics.FLOW_NODE_INSTANCES));

    // generate metrics for all available meters
    var metricNames = metricsRegistry.getDbMeters().keySet();
    metricNames.forEach(metric -> metricsRegistry.markOccurrence(metric, 1));
    dbMetricsReporter.reportNow();

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then - only the two selected metrics are returned
    assertThat(actual).hasSize(2);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(
            tuple(Metrics.PROCESS_INSTANCES, 1L, new DateTime().getYear(), null),
            tuple(Metrics.FLOW_NODE_INSTANCES, 1L, new DateTime().getYear(), null));
  }

  @Test
  public void shouldReturnAggregatedMetricsFilteredByStartDateParameter() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "year");
    queryParameters.add("startDate", "2022-01-01");
    queryParameters.add("metrics", Metrics.PROCESS_INSTANCES);

    // generate metrics for 2021 and current year
    var dateTime = new DateTime().withYear(2021);
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    ClockUtil.reset();
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then - the metric from 2021 is not returned
    assertThat(actual).hasSize(1);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactly(
            tuple(Metrics.PROCESS_INSTANCES, 1L, new DateTime().getYear(), null));
  }

  @Test
  public void shouldReturnAggregatedMetricsFilteredByEndDateParameter() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "year");
    queryParameters.add("endDate", "2022-01-01");
    queryParameters.add("metrics", Metrics.PROCESS_INSTANCES);

    // generate metrics for 2021 and current year
    var dateTime = new DateTime().withYear(2021);
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    ClockUtil.reset();
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then - the metric from current year is not returned
    assertThat(actual).hasSize(1);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactly(
            tuple(Metrics.PROCESS_INSTANCES, 1L, dateTime.getYear(), null));
  }

  @Test
  public void shouldReturnAggregatedMetricsGroupedByMonth() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(4).withDayOfMonth(15).toString());
    queryParameters.add("groupBy", "month");
    queryParameters.add("metrics", Metrics.PROCESS_INSTANCES);

    // generate metrics for 2021-02-15T00:00:00.000+01:00 & 2021-02-14T23:59:59.999+01:00
    var dateTime = new DateTime().withYear(2021)
        .withMonthOfYear(2)
        .withDayOfMonth(15)
        .withTimeAtStartOfDay()
        .minusMillis(1);
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    dateTime = dateTime.withDayOfMonth(15).withTimeAtStartOfDay();
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    ClockUtil.reset();

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then - metrics are grouped by months (Jan, Feb) with respect to the subscriptionStartDate
    assertThat(actual).hasSize(2);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(
            tuple(Metrics.PROCESS_INSTANCES, 1L, dateTime.getYear(), 2),
            tuple(Metrics.PROCESS_INSTANCES, 1L, dateTime.getYear(), 1));
  }

  @Test
  public void shouldReturnAggregatedMetricsGroupedByYear() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(4).withDayOfMonth(15).toString());
    queryParameters.add("groupBy", "year");
    queryParameters.add("metrics", Metrics.PROCESS_INSTANCES);

    // generate metrics for 2021-04-15T00:00:00.000+01:00 & 2021-04-14T23:59:59.999+01:00
    var dateTime = new DateTime().withYear(2021)
        .withMonthOfYear(4)
        .withDayOfMonth(15)
        .withTimeAtStartOfDay()
        .minusMillis(1);
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    dateTime = dateTime.withDayOfMonth(15).withTimeAtStartOfDay();
    ClockUtil.setCurrentTime(dateTime.toDate());
    metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, 1);
    dbMetricsReporter.reportNow();
    ClockUtil.reset();

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then - metrics are grouped by years (2020, 2021) with respect to the subscriptionStartDate
    assertThat(actual).hasSize(2);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(
            tuple(Metrics.PROCESS_INSTANCES, 1L, dateTime.getYear(), null),
            tuple(Metrics.PROCESS_INSTANCES, 1L, dateTime.minusYears(1).getYear(), null));
  }

  @Test
  public void shouldReturnAnnualAggregatedMetricsForTU() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "year");
    queryParameters.add("metrics", Metrics.TASK_USERS);

    // generate TU metric - counts _unique_ task workers (unique ASSIGNEE_HASH_)
    processEngineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(commandContext -> {
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee", ClockUtil.getCurrentTime()));
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee", ClockUtil.getCurrentTime()));
      ClockUtil.reset();
      return null;
    });

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then
    assertThat(actual).hasSize(1);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactly(
            tuple(Metrics.TASK_USERS, 1L, new DateTime().getYear(), null));
  }

  @Test
  public void shouldReturnMonthlyAggregatedMetricsForTU() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("startDate", "2023-01-01");
    queryParameters.add("groupBy", "month");
    queryParameters.add("metrics", Metrics.TASK_USERS);

    // generate TU metric - counts _unique_ task workers (unique ASSIGNEE_HASH_) for the selected period (startDate, endDate)
    DateTime now = new DateTime();
    DateTime prevMonth = now.minusMonths(1);
    processEngineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(commandContext -> {
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee1", ClockUtil.getCurrentTime()));
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee3", ClockUtil.getCurrentTime()));

      ClockUtil.setCurrentTime(prevMonth.toDate());
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee1", ClockUtil.getCurrentTime()));
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee2", ClockUtil.getCurrentTime()));

      ClockUtil.setCurrentTime(now.withYear(2022).toDate());
      commandContext.getMeterLogManager().insert(new TaskMeterLogEntity("assignee1", ClockUtil.getCurrentTime()));
      ClockUtil.reset();
      return null;
    });

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then
    assertThat(actual).hasSize(2);
    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(
            tuple(Metrics.TASK_USERS, 1L, now.getYear(), now.getMonthOfYear()),
            tuple(Metrics.TASK_USERS, 2L, prevMonth.getYear(), prevMonth.getMonthOfYear()));
  }

  protected void generateMetrics(int year, int month, int times) {
    var dateTime = new DateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(10);

    for (int i = 1; i <= times; i++) {
      ClockUtil.setCurrentTime(dateTime.toDate());

      metricsRegistry.markOccurrence(Metrics.ROOT_PROCESS_INSTANCE_START, i);
      metricsRegistry.markOccurrence(Metrics.ACTIVTY_INSTANCE_START, i);
      metricsRegistry.markOccurrence(Metrics.EXECUTED_DECISION_INSTANCES, i);
      metricsRegistry.markOccurrence(Metrics.EXECUTED_DECISION_ELEMENTS, i);
      dbMetricsReporter.reportNow();

      int i1 = i;
      processEngineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(commandContext -> {
        for (int j = 1; j <= i1; j++) {
          commandContext.getMeterLogManager().insert(
              new TaskMeterLogEntity(UUID.randomUUID().toString(), ClockUtil.getCurrentTime()));
        }
        return null;
      });

      dateTime = dateTime.minusDays(1);
    }
    ClockUtil.reset();
  }

  @Test
  public void shouldReturnAggregatedMetricsByYear() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "year");

    // generate metrics for last 5 days & last 3 years
    generateMetrics(2024, 2, 2);
    generateMetrics(2023, 2, 3);

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then
    assertThat(actual).hasSize(10);
    // generate tuples
    var tuples = new ArrayList<Tuple>();
    Consumer<String> generateAssertTuple = (String metric) -> {
      tuples.add(tuple(metric, 3L, 2024, null));
      tuples.add(tuple(metric, 6L, 2023, null));
    };
    generateAssertTuple.accept(Metrics.PROCESS_INSTANCES);
    generateAssertTuple.accept(Metrics.FLOW_NODE_INSTANCES);
    generateAssertTuple.accept(Metrics.DECISION_INSTANCES);
    generateAssertTuple.accept(Metrics.EXECUTED_DECISION_ELEMENTS);
    generateAssertTuple.accept(Metrics.TASK_USERS);

    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(tuples.toArray(new Tuple[]{}));
  }

  @Test
  public void shouldReturnAggregatedMetricsByMonth() {
    // given
    queryParameters.add("subscriptionStartDate", new DateTime().withYear(2020).withMonthOfYear(1).withDayOfMonth(1).toString());
    queryParameters.add("groupBy", "month");

    // generate metrics for last 5 days & last 3 years
    generateMetrics(2024, 2, 2);
    generateMetrics(2024, 1, 3);

    // when
    var actual = resource.getAggregatedMetrics(uriInfo);

    // then
    assertThat(actual).hasSize(10);
    // generate tuples
    var tuples = new ArrayList<Tuple>();
    Consumer<String> generateAssertTuple = (String metric) -> {
      tuples.add(tuple(metric, 3L, 2024, 2));
      tuples.add(tuple(metric, 6L, 2024, 1));
    };
    generateAssertTuple.accept(Metrics.PROCESS_INSTANCES);
    generateAssertTuple.accept(Metrics.FLOW_NODE_INSTANCES);
    generateAssertTuple.accept(Metrics.DECISION_INSTANCES);
    generateAssertTuple.accept(Metrics.EXECUTED_DECISION_ELEMENTS);
    generateAssertTuple.accept(Metrics.TASK_USERS);

    assertThat(actual).extracting("metric", "sum", "subscriptionYear", "subscriptionMonth")
        .containsExactlyInAnyOrder(tuples.toArray(new Tuple[]{}));
  }

}
