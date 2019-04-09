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
package org.camunda.bpm.engine.rest.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyException;
import org.camunda.bpm.engine.rest.PasswordPolicyRestService;
import org.camunda.bpm.engine.rest.dto.passwordPolicy.PasswordDto;
import org.camunda.bpm.engine.rest.dto.passwordPolicy.PasswordPolicyDto;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyRestServiceImpl extends AbstractRestProcessEngineAware implements PasswordPolicyRestService {

  public PasswordPolicyRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public PasswordPolicyDto getPasswordPolicy() {
    PasswordPolicy policy = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getPasswordPolicy();
    return PasswordPolicyDto.fromPasswordPolicyRules(policy.getRules());
  }

  @Override
  public Response checkPassword(PasswordDto password) {
    PasswordPolicy policy = processEngine.getProcessEngineConfiguration().getPasswordPolicy();
    Map<String, Object> parameters = new HashMap<String, Object>();
    try {
      processEngine.getIdentityService().checkPasswordAgainstPolicy(policy, password.getPassword());
      parameters.put("valid", true);
      return Response.status(Status.OK).entity(parameters).build();
    } catch (PasswordPolicyException e) {
      PasswordPolicyDto policyDto = PasswordPolicyDto.fromPasswordPolicyRules(e.getPolicyRules());
      parameters.put("policy", policyDto);
      parameters.put("valid", false);
      return Response.status(Status.OK.getStatusCode()).entity(parameters).build();
    }
  }
}