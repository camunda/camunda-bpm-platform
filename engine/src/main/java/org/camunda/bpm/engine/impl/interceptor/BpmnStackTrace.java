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
package org.camunda.bpm.engine.impl.interceptor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 *
 * @author Daniel Meyer
 *
 */
public class BpmnStackTrace {

  private final static ContextLogger LOG = ProcessEngineLogger.CONTEXT_LOGGER;

  protected List<AtomicOperationInvocation> perfromedInvocations = new ArrayList<AtomicOperationInvocation>();

  public void printStackTrace(boolean verbose) {
    if(perfromedInvocations.isEmpty()) {
      return;
    }

    StringWriter writer = new StringWriter();
    writer.write("BPMN Stack Trace:\n");

    if(!verbose) {
      logNonVerbose(writer);
    }
    else {
      logVerbose(writer);
    }

    LOG.bpmnStackTrace(writer.toString());

    perfromedInvocations.clear();
  }

  protected void logNonVerbose(StringWriter writer) {

    // log the failed operation verbosely
    writeInvocation(perfromedInvocations.get(perfromedInvocations.size() - 1), writer);

    // log human consumable trace of activity ids and names
    List<Map<String, String>> activityTrace = collectActivityTrace();
    logActivityTrace(writer, activityTrace);
  }

  protected void logVerbose(StringWriter writer) {
    // log process engine developer consumable trace
    Collections.reverse(perfromedInvocations);
    for (AtomicOperationInvocation invocation : perfromedInvocations) {
      writeInvocation(invocation, writer);
    }
  }

  protected void logActivityTrace(StringWriter writer, List<Map<String, String>> activities) {
    for (int i = 0; i < activities.size(); i++) {
      if(i != 0) {
        writer.write("\t  ^\n");
        writer.write("\t  |\n");
      }
      writer.write("\t");

      Map<String, String> activity = activities.get(i);
      String activityId = activity.get("activityId");
      writer.write(activityId);

      String activityName = activity.get("activityName");
      if (activityName != null) {
        writer.write(", name=");
        writer.write(activityName);
      }

      writer.write("\n");
    }
  }

  protected List<Map<String, String>> collectActivityTrace() {
    List<Map<String, String>> activityTrace = new ArrayList<Map<String, String>>();
    for (AtomicOperationInvocation atomicOperationInvocation : perfromedInvocations) {
      String activityId = atomicOperationInvocation.getActivityId();
      if(activityId == null) {
        continue;
      }

      Map<String, String> activity = new HashMap<String, String>();
      activity.put("activityId", activityId);

      String activityName = atomicOperationInvocation.getActivityName();
      if (activityName != null) {
        activity.put("activityName", activityName);
      }

      if(activityTrace.isEmpty() ||
          !activity.get("activityId").equals(activityTrace.get(0).get("activityId"))) {
        activityTrace.add(0, activity);
      }
    }
    return activityTrace;
  }

  public void add(AtomicOperationInvocation atomicOperationInvocation) {
    perfromedInvocations.add(atomicOperationInvocation);
  }

  protected void writeInvocation(AtomicOperationInvocation invocation, StringWriter writer) {
    writer.write("\t");
    writer.write(invocation.getActivityId());
    writer.write(" (");
    writer.write(invocation.getOperation().getCanonicalName());
    writer.write(", ");
    writer.write(invocation.getExecution().toString());

    if(invocation.isPerformAsync()) {
      writer.write(", ASYNC");
    }

    if(invocation.getApplicationContextName() != null) {
      writer.write(", pa=");
      writer.write(invocation.getApplicationContextName());
    }

    writer.write(")\n");
  }

}
