package org.camunda.bpm.spring.boot.starter.actuator.micrometer.metrics;

import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TestApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "camunda.bpm.metrics.actuator.interval=-1"}
)
public class ActuatorMetricsPropertyDefaultIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void actuatorMetricsNotCreated() {
        //MicrometerMetricsReporter not created when camunda.bpm.metrics.actuator.interval is -1.
        Assert.assertFalse(applicationContext.containsBean("micrometerMetricsReporter"));
    }
}
