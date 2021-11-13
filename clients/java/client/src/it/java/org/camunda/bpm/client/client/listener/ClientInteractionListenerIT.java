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
package org.camunda.bpm.client.client.listener;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.listener.DefaultClientInteractionListener;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;
import org.camunda.bpm.client.util.PropertyUtil;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.BPMN_ERROR_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.PropertyUtil.DEFAULT_PROPERTIES_PATH;
import static org.camunda.bpm.client.util.PropertyUtil.loadProperties;

public class ClientInteractionListenerIT {

    protected static final String BASE_URL;

    static {
        Properties properties = loadProperties(DEFAULT_PROPERTIES_PATH);
        String engineRest = properties.getProperty(PropertyUtil.CAMUNDA_ENGINE_REST);
        String engineName = properties.getProperty(PropertyUtil.CAMUNDA_ENGINE_NAME);
        BASE_URL = engineRest + engineName;
    }

    protected ClientRule clientRule = new ClientRule(() -> ExternalTaskClient.create().baseUrl(BASE_URL)); // without lock duration
    protected EngineRule engineRule = new EngineRule();
    protected ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

    protected ExternalTaskClient client;

    protected ProcessDefinitionDto processDefinition;
    protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

    @Before
    public void setup() throws Exception {
        client = clientRule.client();
        handler.clear();
        processDefinition = engineRule.deploy(BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
    }

    @Test
    public void shouldUseClientInteractionListener() {

        ExternalTaskClient client = null;
        List<String> states = new LinkedList<>();

        try {
            // given
            engineRule.startProcessInstance(processDefinition.getId());

            ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
                    .clientInteractionListener(new DefaultClientInteractionListener() {

                        @Override
                        public void onFetchAndLock(List<TopicRequestDto> topics) {
                            System.out.println("state: onFetchAndLock");
                            states.add("onFetchAndLock");
                        }

                        @Override
                        public void fetchAndLockDone(List<TopicRequestDto> topics, List<ExternalTask> externalTasks) {
                            System.out.println("state: fetchAndLockDone");
                            states.add("fetchAndLockDone");
                        }

                        @Override
                        public void fetchAndLockFail(List<TopicRequestDto> topics, EngineClientException e) {
                            System.out.println("state: fetchAndLockFail");
                            states.add("fetchAndLockFail");
                        }

                        @Override
                        public void onSetVariable(String processInstanceId, Map<String, Object> variables) {
                            System.out.println("state: onSetVariable");
                            states.add("onSetVariable");
                        }

                        @Override
                        public void setVariableDone(String processInstanceId, Map<String, Object> variables) {
                            System.out.println("state: setVariableDone");
                            states.add("setVariableDone");
                        }

                        @Override
                        public void setVariableFail(String processInstanceId, Map<String, Object> variables) {
                            System.out.println("state: setVariableFail");
                            states.add("setvariableFail");
                        }

                        @Override
                        public void onComplete(String taskId, Map<String, Object> variables, Map<String, Object> localVariables){
                            System.out.println("state: onComplete");
                            states.add("onComplete");
                        }

                        @Override
                        public void completeDone(String taskId, Map<String, Object> variables, Map<String, Object> localVariables){
                            System.out.println("state: completeDone");
                            states.add("completeDone");
                        }

                        @Override
                        public void completeFail(String taskId, Map<String, Object> variables, Map<String, Object> localVariables){
                            System.out.println("state: completeFail");
                            states.add("completeFail");
                        }

                        @Override
                        public void onFailure(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout){
                            System.out.println("state: onFailure");
                            states.add("onFailure");
                        }

                        @Override
                        public void failureDone(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout){
                            System.out.println("state: failureDone");
                            states.add("failureDone");
                        }

                        @Override
                        public void failureFail(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout){
                            System.out.println("state: failureFail");
                            states.add("failureFail");
                        }

                        @Override
                        public void onBpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables){
                            System.out.println("state: onBpmnError");
                            states.add("onBpmnError");
                        }

                        @Override
                        public void bpmnErrorDone(String taskId, String errorCode, String errorMessage, Map<String, Object> variables){
                            System.out.println("state: bpmnErrorDone");
                            states.add("bpmnErrorDone");
                        }

                        @Override
                        public void bpmnErrorFail(String taskId, String errorCode, String errorMessage, Map<String, Object> variables){
                            System.out.println("state: bpmnErrorFail");
                            states.add("bpmnErrorFail");
                        }

                        @Override
                        public void onExtendLock(String taskId, long newDuration){
                            System.out.println("state: onExtendLock");
                            states.add("onExtendLock");
                        }

                        @Override
                        public void extendLockDone(String taskId, long newDuration){
                            System.out.println("state: extendLockDone");
                            states.add("extendLockDone");
                        }

                        @Override
                        public void extendLockFail(String taskId, long newDuration){
                            System.out.println("state: extendLockFail");
                            states.add("extendLockFail");
                        }

                        @Override
                        public void onUnlock(String taskId){
                            System.out.println("state: onUnlock");
                            states.add("onUnlock");
                        }

                        @Override
                        public void unlockDone(String taskId){
                            System.out.println("state: unlockDone");
                            states.add("unlockDone");
                        }

                        @Override
                        public void unlockFail(String taskId){
                            System.out.println("state: unlockFail");
                            states.add("unlockFail");
                        }

                    })
                    .baseUrl(BASE_URL);
            // then
            thrown.expect(ExternalTaskClientException.class);

            // when
            client = clientBuilder.build();
            client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
                    .handler(handler)
                    .open();

            List<String> assertSetVariableSuccessStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onSetVariable", "setVariableDone",
                    "onComplete","completeDone");


            List<String> assertSetVariableFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onSetVariable", "setVariableFail",
                    "onComplete","completeDone");

            List<String> assertFetchAndLockFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockFail");

            List<String> assertCompleteFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onComplete","completeDone");

            List<String> assertExtendLockSuccessStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onExtendLock","extendLockDone",
                    "onComplete","completeDone");

            List<String> assertExtendLockFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onExtendLock","extendLockFail");

            List<String> assertLockFailedStates = Arrays.asList("onUnlock","unlockFail");

            List<String> assertBpmnErrorSuccessStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onBpmnError","bpmnErrorDone",
                    "onComplete","completeDone");

            List<String> assertBpmnErrorFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onBpmnError","bpmnErrorFail",
                    "onComplete","completeDone");

            List<String> assertFailureSuccessStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onFailure","failureDone");

            List<String> assertFailureFailedStates = Arrays.asList("onFetchAndLock", "fetchAndLockDone",
                    "onUnlock","unlockDone",
                    "onFailure","failureDone");


            // then
            clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

            System.out.println("On Set Variables (successfully Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertSetVariableSuccessStates);

            System.out.println("On Set Variables (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertSetVariableFailedStates);

            System.out.println("On Fetch And Lock (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertFetchAndLockFailedStates);

            System.out.println("On Complete (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertCompleteFailedStates);

            System.out.println("On Extend Lock (successfully Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertExtendLockSuccessStates);

            System.out.println("On Extend Lock (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertExtendLockFailedStates);

            System.out.println("On Lock (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertLockFailedStates);

            System.out.println("On BpmnError (successfully Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertBpmnErrorSuccessStates);

            System.out.println("On BpmnError (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertBpmnErrorFailedStates);

            System.out.println("On Failure (successfully Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertFailureSuccessStates);

            System.out.println("On Failure (failed Test): ");
            assertThat(states).containsExactlyInAnyOrderElementsOf(assertFailureFailedStates);

        } finally {
            if (client != null) {
                client.stop();
            }
        }
    }
}

