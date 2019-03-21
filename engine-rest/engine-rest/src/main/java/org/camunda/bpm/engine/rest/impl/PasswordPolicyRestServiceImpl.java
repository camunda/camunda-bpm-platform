/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyChecker;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyException;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicy;
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
    PasswordPolicy policy = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getPasswordPolicy();
    try {
      PasswordPolicyChecker.checkPassword(policy, password.getPassword());
      return Response.status(Status.NO_CONTENT).build();
    } catch (PasswordPolicyException e) {
      return Response.status(Status.BAD_REQUEST.getStatusCode()).entity(PasswordPolicyDto.fromPasswordPolicyRules(e.getPolicyRules())).build();
    }
  }
}
