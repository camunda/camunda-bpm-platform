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
package org.camunda.connect.impl;

import java.io.Closeable;
import java.io.IOException;

import org.camunda.connect.spi.CloseableConnectorResponse;

/**
 * This class is a base class for implementing closeable connector responses
 * based on {@link Closeable}.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractCloseableConnectorResponse extends AbstractConnectorResponse implements CloseableConnectorResponse {

  private final static ConnectCoreLogger LOG = ConnectLogger.CORE_LOGGER;

  /**
   * Implements the default close behavior
   */
  public void close() {
    Closeable closable = getClosable();
    try {
      LOG.closingResponse(this);
      closable.close();
      LOG.successfullyClosedResponse(this);
    } catch (IOException e) {
      throw LOG.exceptionWhileClosingResponse(e);
    }
  }

  /**
   * Allows subclasses to provide the closeable resource.
   * @return the {@link Closeable} resource
   */
  protected abstract Closeable getClosable();

}
