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

import static org.camunda.bpm.qa.performance.engine.loadgenerator.CompletionSignalingRunnable.wrap;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Meyer
 *
 */
public class LoadGenerator {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  public static final String ANSI_CLEAR_LINE = "\u001B[2K";
  public static final String CLEAR_LINE = ANSI_CLEAR_LINE + "\r";

  protected LoadGeneratorConfiguration configuration;

  public LoadGenerator(LoadGeneratorConfiguration configuration) {
    this.configuration = configuration;
  }

  public void execute() throws InterruptedException {

    ExecutorService executorService = Executors.newFixedThreadPool(configuration.getNumOfThreads());

    runSetup(executorService);

    runWorkers(executorService);
  }

  private void runWorkers(ExecutorService executorService) throws InterruptedException {

    final int numberOfIterations = configuration.getNumberOfIterations();
    final int taskCount = numberOfIterations * configuration.getWorkerTasks().length;
    final CountDownLatch sync = new CountDownLatch(taskCount);

    final Timer timer = new Timer();
    timer.scheduleAtFixedRate(new ProgressReporter(taskCount, sync, configuration.isColor()), 2000, 2000);

    System.out.println("Generating load. Total tasks: "+taskCount+"... ");

    for (int i = 1; i <= numberOfIterations; i++) {

      for (Runnable runnable : configuration.getWorkerTasks()) {
        executorService.execute(wrap(runnable, sync));
      }

    }

    sync.await();

    timer.cancel();

    if(configuration.isColor()) System.out.print(CLEAR_LINE + ANSI_GREEN);
    System.out.println("Finished generating load.");
    if(configuration.isColor()) System.out.print(ANSI_RESET);

    executorService.shutdown();
  }

  private void runSetup(ExecutorService executorService) throws InterruptedException {
    CountDownLatch sync = new CountDownLatch(configuration.getSetupTasks().length);

    System.out.print("Running setup ... ");

    for (Runnable r : configuration.getSetupTasks()) {
      executorService.execute(wrap(r, sync));
    }


    sync.await();

    if(configuration.isColor()) System.out.print(ANSI_GREEN);

    System.out.println("Done");

    if(configuration.isColor()) System.out.print(ANSI_RESET);
  }

  static class ProgressReporter extends TimerTask {

    private int totalWork;
    protected CountDownLatch sync;

    protected boolean color;

    public ProgressReporter(int totalWork, CountDownLatch latch, boolean color) {
      this.totalWork = totalWork;
      this.sync = latch;
      this.color = color;
    }

    @Override
    public void run() {
      final long tasksCompleted = totalWork - sync.getCount();
      final double progress = (100d / totalWork) * tasksCompleted;


      StringBuilder statusMessage = new StringBuilder();

      if(color) statusMessage.append(CLEAR_LINE + ANSI_YELLOW);

      statusMessage.append(String.format("%6.2f", progress));
      statusMessage.append("% done");

      if(color) statusMessage.append(ANSI_RESET);
      if(!color) statusMessage.append("\n");

      System.out.print(statusMessage);
    }

  }


}
