package org.camunda.bpm.spring.boot.starter.property;

import org.camunda.bpm.spring.boot.starter.util.SpringBootStarterException;

public class ActuatorProperty {

    private Integer interval = -1; // Default value

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        if (interval == null || interval < -1 || interval == 0) {
            throw new SpringBootStarterException("Invalid value for camunda.bpm.metrics.actuator.interval: " + interval +
                    ". Value must be -1 or greater than 0.");
        }
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "ActuatorProperty[interval=" + interval + "]";
    }
}
