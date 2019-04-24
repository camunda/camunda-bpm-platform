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
package org.camunda.bpm.application.impl;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationIdentifier {

  protected String name;
  protected ProcessApplicationReference reference;
  protected ProcessApplicationInterface processApplication;

  public ProcessApplicationIdentifier(String name) {
    this.name = name;
  }

  public ProcessApplicationIdentifier(ProcessApplicationReference reference) {
    this.reference = reference;
  }

  public ProcessApplicationIdentifier(ProcessApplicationInterface processApplication) {
    this.processApplication = processApplication;
  }

  public String getName() {
    return name;
  }

  public ProcessApplicationReference getReference() {
    return reference;
  }

  public ProcessApplicationInterface getProcessApplication() {
    return processApplication;
  }

  public String toString() {
    String paName = name;
    if (paName == null && reference != null) {
      paName = reference.getName();
    }
    if (paName == null && processApplication != null) {
      paName = processApplication.getName();
    }
    return "ProcessApplicationIdentifier[name=" + paName + "]";
  }
}
