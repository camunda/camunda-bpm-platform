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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyData;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class SetLicenseKeyCmd extends LicenseCmd implements Command<Object> {

  protected String licenseKey;

  public SetLicenseKeyCmd(final String licenseKey) {
    this.licenseKey = licenseKey;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull("licenseKey", licenseKey);

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCamundaAdmin();

    final ResourceManager resourceManager = commandContext.getResourceManager();
    ResourceEntity key = resourceManager.findLicenseKeyResource();
    if (key != null) {
      new DeleteLicenseKeyCmd(false, false).execute(commandContext);
    }
    key = new ResourceEntity();
    key.setName(LICENSE_KEY_PROPERTY_NAME);
    key.setBytes(licenseKey.getBytes(StandardCharsets.UTF_8));
    // set license key as byte array BLOB
    resourceManager.insertResource(key);

    // set license key byte array id property
    new SetPropertyCmd(LICENSE_KEY_BYTE_ARRAY_ID, key.getId()).execute(commandContext);

    // cleanup legacy property
    new DeletePropertyCmd(LICENSE_KEY_PROPERTY_NAME).execute(commandContext);

    // add raw license to telemetry data if not there already
    addToTelemetry(licenseKey, commandContext);

    return null;
  }

  protected void addToTelemetry(String licenseKey, CommandContext context) {
    TelemetryRegistry telemetryRegistry = context.getProcessEngineConfiguration().getTelemetryRegistry();
    LicenseKeyData currentLicenseData = telemetryRegistry.getLicenseKey();
    // only report license body without signature, if present
    String newLicenseData = licenseKey.contains(";") ? licenseKey.split(";", 2)[1] : licenseKey;
    if (currentLicenseData == null || !newLicenseData.equals(currentLicenseData.getRaw())) {
      telemetryRegistry.setLicenseKey(new LicenseKeyData(null, null, null, null, null, newLicenseData));
    }
  }
}