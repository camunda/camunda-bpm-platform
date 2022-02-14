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
package org.camunda.bpm.engine.impl.cmd;

import java.nio.charset.StandardCharsets;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

public class GetLicenseKeyCmd extends LicenseCmd implements Command<String> {

  @Override
  public String execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkCamundaAdminOrPermission(CommandChecker::checkReadLicenseKey);

    // case I: license is stored as BLOB
    ResourceEntity licenseResource = commandContext.getResourceManager().findLicenseKeyResource();
    if (licenseResource != null) {
      return new String(licenseResource.getBytes(), StandardCharsets.UTF_8);
    }

    // case II: license is stored in properties
    PropertyEntity licenseProperty = commandContext.getPropertyManager().findPropertyById(LICENSE_KEY_PROPERTY_NAME);
    if (licenseProperty != null) {
      return licenseProperty.getValue();
    }

    return null;
  }
}