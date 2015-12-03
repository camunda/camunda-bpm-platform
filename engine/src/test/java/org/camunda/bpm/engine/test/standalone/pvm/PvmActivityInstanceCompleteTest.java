package org.camunda.bpm.engine.test.standalone.pvm;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.EmbeddedSubProcess;
import org.camunda.bpm.engine.test.standalone.pvm.activities.End;
import org.camunda.bpm.engine.test.standalone.pvm.activities.ParallelGateway;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;

/**
 *
 * @author roman.smirnov
 *
 */
public class PvmActivityInstanceCompleteTest extends PvmTestCase {


  /**
   * +-------+   +-----+
   * | start |-->| end |
   * +-------+   +-----+
   */
  public void testSingleEnd() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    verifier.assertNonCompletingActivityInstance("start", 1);
    verifier.assertIsCompletingActivityInstance("end", 1);
  }

  /**
   *                   +----+
   *              +--->|end1|
   *              |    +----+
   *              |
   * +-----+   +----+
   * |start|-->|fork|
   * +-----+   +----+
   *              |
   *              |    +----+
   *              +--->|end2|
   *                   +----+
   */
  public void testTwoEnds() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("end1")
        .transition("end2")
      .endActivity()
      .createActivity("end1")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
      .createActivity("end2")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    verifier.assertNonCompletingActivityInstance("start", 1);
    verifier.assertNonCompletingActivityInstance("fork", 1);
    verifier.assertIsCompletingActivityInstance("end1", 1);
    verifier.assertIsCompletingActivityInstance("end2", 1);
  }

  /**
   *                   +----+
   *              +--->| a1 |---+
   *              |    +----+   |
   *              |             v
   * +-----+   +----+       +------+    +-----+
   * |start|-->|fork|       | join |--->| end |
   * +-----+   +----+       +------+    +-----+
   *              |             ^
   *              |    +----+   |
   *              +--->| a2 |---+
   *                   +----+
   */
  public void testSingleEndAfterParallelJoin() {
    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("a1")
        .transition("a2")
      .endActivity()
      .createActivity("a1")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("join")
      .endActivity()
      .createActivity("a2")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    verifier.assertNonCompletingActivityInstance("start", 1);
    verifier.assertNonCompletingActivityInstance("fork", 1);
    verifier.assertNonCompletingActivityInstance("a1", 1);
    verifier.assertNonCompletingActivityInstance("a2", 1);
    verifier.assertNonCompletingActivityInstance("join", 2);
    verifier.assertIsCompletingActivityInstance("end", 1);
  }

  /**
   *           +-------------------------------+
   *           | embeddedsubprocess            |
   *           |                               |
   * +-----+   |  +-----------+   +---------+  |   +---+
   * |start|-->|  |startInside|-->|endInside|  |-->|end|
   * +-----+   |  +-----------+   +---------+  |   +---+
   *           |                               |
   *           |                               |
   *           +-------------------------------+
   */
  public void testSimpleSubProcess() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("endInside")
        .endActivity()
        .createActivity("endInside")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    verifier.assertNonCompletingActivityInstance("start", 1);
    verifier.assertNonCompletingActivityInstance("embeddedsubprocess", 1);
    verifier.assertNonCompletingActivityInstance("startInside", 1);
    verifier.assertIsCompletingActivityInstance("endInside", 1);
    verifier.assertIsCompletingActivityInstance("end", 1);
  }

  /**
   *           +----------+
   *           | userTask |
   *           |          |
   * +-----+   |          |    +------+
   * |start|-->|          |--->| end1 |
   * +-----+   | +-----+  |
   *           +-|timer|--+
   *             +-----+
   *                |          +------+
   *                +--------->| end2 |
   *                           +------+
   */
  public void testBoundaryEvent() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("userTask")
      .endActivity()
      .createActivity("userTask")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("end1")
      .endActivity()
      .createActivity("timer")
        .behavior(new WaitState())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .attachedTo("userTask", true)
        .transition("end2")
      .endActivity()
      .createActivity("end1")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
      .createActivity("end2")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmExecution userTaskExecution = processInstance.findExecution("userTask");
    ((PvmExecutionImpl) userTaskExecution).executeActivity(processDefinition.findActivity("timer"));

    PvmExecution timerExecution = processInstance.findExecution("timer");
    timerExecution.signal(null, null);

    verifier.assertNonCompletingActivityInstance("start", 1);
    verifier.assertNonCompletingActivityInstance("userTask", 1);
    verifier.assertIsCompletingActivityInstance("end2", 1);
  }

}
