package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.spring.subscription.TestClassSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TestClassSubscription.class})
@EnableTaskSubscription(baseUrl = "http://localhost:8080/rest")
public class SimpleConfiguration {

    @TaskSubscription(topicName = "methodSubscription")
    @Bean
    public ExternalTaskHandler methodSubscription() {
        return (externalTask, externalTaskService) -> {

            // interact with the external task

        };
    }
}
