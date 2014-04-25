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
package org.camunda.bpm.engine.impl.pvm.runtime;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.util.List;

/**
 * <p>Base atomic operation used for implementing atomic operations which
 * create a new concurrent execution for executing an activity. This atomic
 * operation makes sure the execution is created under the correct parent.</p>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AtomicOperationCreateConcurrentExecution implements AtomicOperation {

  public void execute(InterpretableExecution execution) {

    // the execution which will continue
    InterpretableExecution propagatingExecution = execution;

    // the activity which is to be executed concurrently
    PvmActivity concurrentActivity = execution.getNextActivity();
    PvmScope concurrencyScope = concurrentActivity.getScope();

    if(isLeaf(execution)) {

      if(execution.getActivity() != null
         && execution.isScope()
         && !((ActivityImpl)execution.getActivity()).isScope()
         && concurrencyScope == execution.getActivity().getParent()
         ) {

        // Expand tree (1):
        //
        //        ...                        ...
        //         |                          |
        //      +------+                  +-------+   s=tt
        //      |  e   |       =>         |   e   |   cc=ff
        //      +------+                  +-------+
        //          s=tt                      ^
        //         cc=ff                     / \
        //                                  /   \
        //                                 /     \          Both:
        //                          +-------+   +--------+    s=ff
        //                          | CCE-1 |   | PPE    |   cc=tt
        //                          +-------+   +--------+
        //

        // 1) create new concurrent execution (CCE-1) replacing the the active scope execution (e)
        InterpretableExecution replacingExecution = (InterpretableExecution) execution.createExecution();
        replacingExecution.replace(execution); // only copy tasks(?)
        replacingExecution.setActivity((ActivityImpl) execution.getActivity());
        replacingExecution.setActive(execution.isActive());
        replacingExecution.setScope(false);
        replacingExecution.setConcurrent(true);

        execution.setActive(false);
        execution.setActivity(null);

        // 2) create new concurrent execution (PPE) for new activity instance
        propagatingExecution = createConcurrentExecution(execution, concurrentActivity);

      } else {

        // Case (1): Expand tree and set execution to concurrent:
        //
        //        ...                         ...
        //         |                           |
        //      +------+ s=tt              +-------+ s=tt
        //      |  p   | cc=ff     =>      |   p   | cc=ff
        //      +------+                   +-------+
        //         |                           ^
        //         |                          / \
        //         |                         /   \
        //         |                        /     \          Both:
        //      +------+ s=tt        +-------+   +--------+    s=ff
        //      |  e   | cc=ff       |   e   |   |  PPE   |   cc=tt
        //      +------+             +-------+   +--------+
        //
        // Case (2): Add to existing concurrency tree rooting at parent of parent
        //
        //
        //                 ...                                 ...
        //                  |                                   |
        //              +-------+   s=tt                    +--------+  s=tt
        //              |   pp  |   cc=ff                   |   pp   |  cc=ff
        //              +-------+                           +--------+
        //                  ^                                   ^    ^
        //                 / \                   =>            / \    \
        //                /   \                               /   \    \
        //               /     \     all:                    /     \    \           all:
        //        +--------+   ....   s=ff            +--------+   ....  +-------+  s=ff
        //        | parent |          cc=tt           | parent |         |  PPE  |  cc=tt
        //        +--------+                          +--------+         +-------+
        //             |                                  |
        //        +-------+ s=tt                      +-------+ s=tt
        //        | e     | cc=ff                     | e     | cc=ff
        //        +-------+                           +-------+
        //



        // Case (1)
        InterpretableExecution parent = (InterpretableExecution) execution.getParent();

        if (!parent.isConcurrent()) {
          // mark execution (e) concurrent
          execution.setConcurrent(true);

        } else {
          // Case (2)
          parent = (InterpretableExecution) parent.getParent();
        }

        propagatingExecution = createConcurrentExecution(parent, concurrentActivity);

      }

    } else {

      List<? extends ActivityExecution> childExecutions = execution.getExecutions();

      if(childExecutions.size() == 1
        && execution.getActivity() == null
        && !execution.isActive()) {

      // Add new child execution and set concurrent flag to true
      // for already existing child execution
      //
      //        ...                         ...
      //         |                           |
      //      +------+ s=tt              +-------+ s=tt
      //      |  e   | cc=ff     =>      |   e   | cc=ff
      //      +------+                   +-------+
      //         |                           ^
      //         |                          / \
      //         |                         /   \
      //         |                        /     \          Both:
      //      +------+ s=tt        +-------+   +--------+    s=ff
      //      |child | cc=ff       | child |   |  PPE   |   cc=tt
      //      +------+             +-------+   +--------+
      //

      // 1) mark existing child concurrent
      InterpretableExecution existingChild = (InterpretableExecution) childExecutions.get(0);
      existingChild.setConcurrent(true);

      // 2) create new concurrent execution (PPE) for new activity instance
      propagatingExecution = createConcurrentExecution(execution, concurrentActivity);



      } else { /* execution.getExecutions().size() > 1 */

        // Add to existing concurrency tree:
        //
        // Case (1)
        //              +-------+ s=tt                   +-------+ s=tt
        //              |   e   | cc=ff                  |   e   | cc=ff
        //              +-------+                        +-------+
        //                  ^                                ^   ^
        //                 / \                =>            / \   \
        //                /   \                            /   \   \
        //               /     \     all:                 /     \   \
        //        +-------+   ....     s=?          +-------+   ...  +-------+ s=ff
        //        |       |            cc=tt        |       |        |  PPE  | cc=tt
        //        +-------+                         +-------+        +-------+
        //
        // Case (2)
        //
        //                 ...                              ...
        //                  |                                |     s=tt
        //              +-------+ s=tt                   +-------+ cc=ff
        //              | parent| cc=ff                  | parent|<------------+
        //              +-------+                        +-------+             |
        //                  |                                |                 |
        //              +-------+ s=tt                   +-------+ s=tt     +-------+ s=ff
        //              |   e   | cc=?                   |   e   | cc=tt    |  PPE  | cc=tt
        //              +-------+                        +-------+          +-------+
        //                  ^                                ^
        //                 / \                =>            / \
        //                /   \                            /   \
        //               /     \     all:                 /     \    all:
        //        +-------+   ....     s=?          +-------+   ...    s=?
        //        |       |            cc=tt        |       |          cc=tt
        //        +-------+                         +-------+
        //
        // Case (3)
        //
        //                 ...                                   ...
        //                  |     s=tt                            |     s=tt
        //              +-------+ cc=?                        +-------+ cc=?
        //              |   pp  |<-----------+                |  pp   |<---------------------------+
        //              +-------+            |                +-------+            |               |
        //                  |                |  all:              |                |  all:         |
        //                  |     s=tt       |   s=?              |     s=tt       |   s=?         |
        //              +-------+ cc=tt     ...  cc=tt        +-------+ cc=tt     ...  cc=tt   +-------+ s=ff
        //              | parent|                             | parent|                        |  PPE  | cc=tt
        //              +-------+                             +-------+                        +-------+
        //                  |                                     |
        //              +-------+ s=tt                        +-------+ s=tt
        //              |   e   | cc=?                        |   e   | cc=?
        //              +-------+                             +-------+
        //                  ^                                     ^
        //                 / \                   =>              / \
        //                /   \                                 /   \
        //               /     \     all:                      /     \    all:
        //        +-------+   ....     s=?                +-------+   ...    s=?
        //        |       |            cc=tt              |       |          cc=tt
        //        +-------+                               +-------+
        //
        //


        // Case (1)
        InterpretableExecution concurrentRoot = execution;

        PvmScope parentScope = concurrentActivity.getParent();
        if(parentScope instanceof ActivityImpl) {
          ActivityImpl parentActivity = (ActivityImpl) parentScope;
          if (execution.getActivity() != null || execution.isActive()) {
            if(parentActivity.isScope()) {
              // Case (2)
              concurrentRoot = (InterpretableExecution) execution.getParent();
              if (!concurrentRoot.isConcurrent()) {
                execution.setConcurrent(true);

              } else {
                // Case (3)
                concurrentRoot = (InterpretableExecution) concurrentRoot.getParent();
              }
            }
          }
        }

        propagatingExecution = createConcurrentExecution(concurrentRoot, concurrentActivity);

      }
    }

    concurrentExecutionCreated(propagatingExecution);
  }

  protected abstract void concurrentExecutionCreated(InterpretableExecution propagatingExecution);

  protected InterpretableExecution createConcurrentExecution(InterpretableExecution execution, PvmActivity concurrentActivity) {
    InterpretableExecution newConcurrentExecution = (InterpretableExecution) execution.createExecution();
    newConcurrentExecution.setActivity((ActivityImpl) concurrentActivity);
    newConcurrentExecution.setScope(false);
    newConcurrentExecution.setActive(true);
    newConcurrentExecution.setConcurrent(true);
    return newConcurrentExecution;
  }

  /**
   *
   * @return  true if the execution is the root of a concurrency tree
   */
  protected boolean isConcurrentRoot(InterpretableExecution execution) {
    List<? extends ActivityExecution> executions = execution.getExecutions();
    if(executions == null || executions.size() == 0) {
      return false;
    } else {
      return executions.get(0).isConcurrent();
    }
  }

  /**
   * @return true if this execution does not have children
   */
  protected boolean isLeaf(InterpretableExecution execution) {
    return execution.getExecutions().isEmpty();
  }

  /**
   * @return the scope for this execution
   */
  protected PvmScope getCurrentScope(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    if(activity == null) {
      return null;
    } else {
      return activity.isScope() ? activity : activity.getParent();
    }
  }

  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

}
