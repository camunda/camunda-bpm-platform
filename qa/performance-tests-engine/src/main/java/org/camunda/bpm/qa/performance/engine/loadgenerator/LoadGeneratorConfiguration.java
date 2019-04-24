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
package org.camunda.bpm.qa.performance.engine.loadgenerator;

/**
 * @author Daniel Meyer
 *
 */
public class LoadGeneratorConfiguration {

  /** the number of threads to use when generating load */
  protected int numOfThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

  /** controls how often the worker runnables are executed */
  protected int numberOfIterations = 10000;

  protected Runnable[] setupTasks;

  protected Runnable[] workerTasks;

  protected boolean color = true;

  public int getNumOfThreads() {
    return numOfThreads;
  }

  public Runnable[] getSetupTasks() {
    return setupTasks;
  }

  public Runnable[] getWorkerTasks() {
    return workerTasks;
  }

  public void setNumOfThreads(int numOfThreads) {
    this.numOfThreads = numOfThreads;
  }

  public void setSetupTasks(Runnable[] setupRunnables) {
    this.setupTasks = setupRunnables;
  }

  public void setWorkerTasks(Runnable[] workerRunnables) {
    this.workerTasks = workerRunnables;
  }

  public int getNumberOfIterations() {
    return numberOfIterations;
  }

  public void setNumberOfIterations(int numberOfIterations) {
    this.numberOfIterations = numberOfIterations;
  }

  public boolean isColor() {
    return color;
  }

  public void setColor(boolean color) {
    this.color = color;
  }

}
