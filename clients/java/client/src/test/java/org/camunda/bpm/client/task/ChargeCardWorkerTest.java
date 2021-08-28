package org.camunda.bpm.client.task;

import org.camunda.bpm.client.ExternalTaskClient;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ChargeCardWorkerTest {

    private final static Logger LOGGER = Logger.getLogger(ChargeCardWorkerTest.class.getName());

    public static void main(String[] args) {

        // Personal Test
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://10.1.2.51:8080/engine-rest")
                .asyncResponseTimeout(10000) // long polling timeout
                .build();

        client.subscribe("charge-card")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> vars = new HashMap<String, Object>();
                    vars.put("progress",10);

                    externalTaskService.setVariables(externalTask,vars);
                    externalTaskService.complete(externalTask);
                })
                .open();
    }
}
