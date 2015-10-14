/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Daniel Meyer
 *
 */
public class BpmnStackTrace {

  public final static Logger log = Logger.getLogger(BpmnStackTrace.class.getName());

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

    log.severe(writer.toString());

    perfromedInvocations.clear();
  }

  protected void logNonVerbose(StringWriter writer) {

    // log the failed operation verbosely
    writeInvocation(perfromedInvocations.get(perfromedInvocations.size() - 1), writer);

    // log human consumable trace of activity ids only
    List<String> activities = collectActivityTrace();
    logActivityTrace(writer, activities);
  }

  protected void logVerbose(StringWriter writer) {
    // log process engine developer consumable trace
    Collections.reverse(perfromedInvocations);
    for (AtomicOperationInvocation invocation : perfromedInvocations) {
      writeInvocation(invocation, writer);
    }
  }

  protected void logActivityTrace(StringWriter writer, List<String> activities) {
    for (int i = 0; i < activities.size(); i++) {
      if(i != 0) {
        writer.write("\t  ^\n");
        writer.write("\t  |\n");
      }
      writer.write("\t");
      writer.write(activities.get(i));
      writer.write("\n");
    }
  }

  protected List<String> collectActivityTrace() {
    List<String> activities = new ArrayList<String>();
    for (AtomicOperationInvocation atomicOperationInvocation : perfromedInvocations) {
      String activityId = atomicOperationInvocation.getActivityId();
      if(activityId == null) {
        continue;
      }
      if(activities.isEmpty() ||
          !activityId.equals(activities.get(0))) {
        activities.add(0, activityId);
      }
    }
    return activities;
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
