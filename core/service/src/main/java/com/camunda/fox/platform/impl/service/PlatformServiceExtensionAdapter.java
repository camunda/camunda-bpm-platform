/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl.service;

import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * <p>Adapter class for implementing {@link PlatformServiceExtension PlatformServiceExtensions}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class PlatformServiceExtensionAdapter implements PlatformServiceExtension {

  public void onPlatformServiceStart(PlatformService platformService) {
  }

  public void onPlatformServiceStop(PlatformService platformService) {
  }

  public void beforeProcessEngineControllerStart(ProcessEngineController processEngineController) {
  }

  public void afterProcessEngineControllerStart(ProcessEngineController processEngineController) {
  }

  public void beforeProcessEngineControllerStop(ProcessEngineController processEngineController) {
  }

  public void afterProcessEngineControllerStop(ProcessEngineController processEngineController) {
  }

  public void beforeProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
  }

  public void afterProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngineController, String deploymentId) {
  }

  public void beforeProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
  }

  public void afterProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
  }

}
