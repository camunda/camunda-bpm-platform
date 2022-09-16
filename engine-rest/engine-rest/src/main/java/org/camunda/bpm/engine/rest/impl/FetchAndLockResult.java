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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockResult {

  protected List<LockedExternalTaskDto> tasks = new ArrayList<LockedExternalTaskDto>();
  protected Throwable throwable;

  public FetchAndLockResult(List<LockedExternalTaskDto> tasks) {
    this.tasks = tasks;
  }

  public FetchAndLockResult(Throwable throwable) {
    this.throwable = throwable;
  }

  public List<LockedExternalTaskDto> getTasks() {
    return tasks;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public boolean wasSuccessful() {
    return throwable == null;
  }

  public static FetchAndLockResult successful(List<LockedExternalTaskDto> tasks) {
    return new FetchAndLockResult(tasks);
  }

  public static FetchAndLockResult failed(Throwable throwable) {
    return new FetchAndLockResult(throwable);
  }

  @Override
  public String toString() {
    return "FetchAndLockResult [tasks=" + tasks + ", throwable=" + throwable + "]";
  }

}