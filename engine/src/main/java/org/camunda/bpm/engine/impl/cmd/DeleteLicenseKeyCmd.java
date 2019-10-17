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

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;

public class DeleteLicenseKeyCmd extends LicenseCmd implements Command<Object> {

  boolean deleteProperty;
  
  public DeleteLicenseKeyCmd(boolean deleteProperty) {
    this.deleteProperty = deleteProperty;
  }
  
  @Override
  public Object execute(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkCamundaAdmin();

    final ResourceManager resourceManager = commandContext.getResourceManager();
    final PropertyManager propertyManager = commandContext.getPropertyManager();
    
    PropertyEntity licenseProperty = propertyManager.findPropertyById(LICENSE_KEY_BYTE_ARRAY_ID);

    // delete license key BLOB
    ResourceEntity licenseKey = resourceManager.findLicenseKeyResource();
    if(licenseKey != null) {
      resourceManager.deleteLicenseKeyResource(licenseProperty.getRevision());
    }

    // always delete license key legacy property if it still exists
    new DeletePropertyCmd(LICENSE_KEY_PROPERTY_NAME).execute(commandContext);

    if(deleteProperty) {
      // delete license key byte array id
      new DeletePropertyCmd(LICENSE_KEY_BYTE_ARRAY_ID).execute(commandContext);
    }

    return null;
  }
}