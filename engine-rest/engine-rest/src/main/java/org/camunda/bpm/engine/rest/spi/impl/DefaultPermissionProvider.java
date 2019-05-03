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
package org.camunda.bpm.engine.rest.spi.impl;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.impl.util.ResourceTypeUtil;
import org.camunda.bpm.engine.rest.spi.PermissionProvider;

/**
 * Default implementation of SPI {@link PermissionProvider}
 * 
 * @author Yana.Vasileva
 *
 */
public class DefaultPermissionProvider implements PermissionProvider {

  @Override
  public Permission getPermissionForName(String name, int resourceType) {
    return ResourceTypeUtil.getPermissionByNameAndResourceType(name, resourceType);
  }

}
