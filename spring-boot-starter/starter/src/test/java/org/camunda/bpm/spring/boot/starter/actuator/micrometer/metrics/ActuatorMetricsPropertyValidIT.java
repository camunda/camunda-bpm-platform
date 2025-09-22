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
package org.camunda.bpm.spring.boot.starter.actuator.micrometer.metrics;

import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.spring.boot.starter.AbstractCamundaAutoConfigurationIT;
import org.camunda.bpm.spring.boot.starter.actuator.MicrometerMetricsReporter;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TestApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "camunda.bpm.metrics.actuator.interval=1", "management.endpoints.web.exposure.include=metrics"}
)
public class ActuatorMetricsPropertyValidIT extends AbstractCamundaAutoConfigurationIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    MicrometerMetricsReporter micrometerMetricsReporter;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void actuatorMetricsCreated() {
        //MicrometerMetricsReporter bean is created
        Assert.assertTrue(applicationContext.containsBean("micrometerMetricsReporter"));

        processEngine.getManagementService().reportDbMetricsNow();
        micrometerMetricsReporter.reportNow();

        Map<String, List<String>> response = testRestTemplate.getForEntity("/actuator/metrics", Map.class).getBody();
        List<String> metricNames = response.get("names");
        List<MetricIntervalValue> dbMetrics = processEngine.getManagementService().createMetricsQuery().interval();
        long actuatorMetricsLength = metricNames.stream().filter(metric->metric.startsWith("camunda.")).count();

        //Number of metrics from DB equals to metrics coming from actuator
        Assert.assertEquals(actuatorMetricsLength, dbMetrics.size());

        for(MetricIntervalValue metric : dbMetrics) {
            Map<String, Object> actuatorMetricResponse = testRestTemplate
                    .getForEntity("/actuator/metrics/camunda." + metric.getName(), Map.class)
                    .getBody();

            List<Map<String, Object>> measurements = (List<Map<String, Object>>) actuatorMetricResponse.get("measurements");
            Double actuatorValue = (Double) measurements.get(0).get("value");

            //Checking metric value
            Assert.assertEquals(metric.getValue(), actuatorValue.longValue());
        }
    }
}
