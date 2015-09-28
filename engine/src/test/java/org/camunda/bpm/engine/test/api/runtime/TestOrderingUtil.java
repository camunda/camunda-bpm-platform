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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * This class provides utils to verify the sorting of queries of engine entities.
 * Assuming we sort over a property x, there are two valid orderings when some entities
 * have values where x = null: Either, these values precede the overall list, or they trail it.
 * Thus, this class does not use regular comparators but a {@link NullTolerantComparator}
 * that can be used to assert a list of entites in both ways.
 *
 * @author Thorben Lindhauer
 *
 */
public class TestOrderingUtil {

  // EXECUTION

  public static NullTolerantComparator<Execution> executionByProcessInstanceId() {
    return new NullTolerantComparator<Execution>() {
      public int compare(Execution o1, Execution o2) {
        return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
      }

      public boolean hasNullProperty(Execution object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<Execution> executionByProcessDefinitionId(final ProcessEngine processEngine) {
    final RuntimeService runtimeService = processEngine.getRuntimeService();

    return new NullTolerantComparator<Execution>() {
      public int compare(Execution o1, Execution o2) {
        ProcessInstance processInstance1 = runtimeService.createProcessInstanceQuery()
            .processInstanceId(o1.getProcessInstanceId()).singleResult();
        ProcessInstance processInstance2 = runtimeService.createProcessInstanceQuery()
            .processInstanceId(o2.getProcessInstanceId()).singleResult();

        return processInstance1.getProcessDefinitionId().compareTo(processInstance2.getProcessDefinitionId());
      }

      public boolean hasNullProperty(Execution object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<Execution> executionByProcessDefinitionKey(ProcessEngine processEngine) {
    final RuntimeService runtimeService = processEngine.getRuntimeService();
    final RepositoryService repositoryService = processEngine.getRepositoryService();

    return new NullTolerantComparator<Execution>() {
      public int compare(Execution o1, Execution o2) {
        ProcessInstance processInstance1 = runtimeService.createProcessInstanceQuery()
            .processInstanceId(o1.getProcessInstanceId()).singleResult();
        ProcessDefinition processDefinition1 = repositoryService.getProcessDefinition(processInstance1.getProcessDefinitionId());

        ProcessInstance processInstance2 = runtimeService.createProcessInstanceQuery()
            .processInstanceId(o2.getProcessInstanceId()).singleResult();
        ProcessDefinition processDefinition2 = repositoryService.getProcessDefinition(processInstance2.getProcessDefinitionId());


        return processDefinition1.getId().compareTo(processDefinition2.getId());
      }

      public boolean hasNullProperty(Execution object) {
        return false;
      }
    };
  }

  // CASE EXECUTION

  public static NullTolerantComparator<CaseExecution> caseExecutionByDefinitionId() {
    return new NullTolerantComparator<CaseExecution>() {
      public int compare(CaseExecution o1, CaseExecution o2) {
        return o1.getCaseDefinitionId().compareTo(o2.getCaseDefinitionId());
      }

      public boolean hasNullProperty(CaseExecution object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<CaseExecution> caseExecutionByDefinitionKey(ProcessEngine processEngine) {
    final RepositoryService repositoryService = processEngine.getRepositoryService();
    return new NullTolerantComparator<CaseExecution>() {
      public int compare(CaseExecution o1, CaseExecution o2) {
        CaseDefinition definition1 = repositoryService.getCaseDefinition(o1.getCaseDefinitionId());
        CaseDefinition definition2 = repositoryService.getCaseDefinition(o1.getCaseDefinitionId());

        return definition1.getId().compareTo(definition2.getId());
      }

      public boolean hasNullProperty(CaseExecution object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<CaseExecution> caseExecutionById() {
    return new NullTolerantComparator<CaseExecution>() {
      public int compare(CaseExecution o1, CaseExecution o2) {
        return o1.getId().compareTo(o2.getId());
      }

      public boolean hasNullProperty(CaseExecution object) {
        return false;
      }
    };
  }

  // TASK

  public static NullTolerantComparator<Task> taskById() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getId().compareTo(o2.getId());
      }

      public boolean hasNullProperty(Task object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByName() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getName().compareTo(o2.getName());
      }

      public boolean hasNullProperty(Task object) {
        return object.getName() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByPriority() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getPriority() - o2.getPriority();
      }

      public boolean hasNullProperty(Task object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByAssignee() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getAssignee().compareTo(o2.getAssignee());
      }

      public boolean hasNullProperty(Task object) {
        return object.getAssignee() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByDescription() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getDescription().compareTo(o2.getDescription());
      }

      public boolean hasNullProperty(Task object) {
        return object.getDescription() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByProcessInstanceId() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
      }

      public boolean hasNullProperty(Task object) {
        return object.getProcessInstanceId() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByExecutionId() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getExecutionId().compareTo(o2.getExecutionId());
      }

      public boolean hasNullProperty(Task object) {
        return object.getExecutionId() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByCreateTime() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return compareDates(o1.getCreateTime(), o2.getCreateTime());
      }

      public boolean hasNullProperty(Task object) {
        return object.getCreateTime() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByDueDate() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return compareDates(o1.getDueDate(), o2.getDueDate());
      }

      public boolean hasNullProperty(Task object) {
        return object.getDueDate() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByFollowUpDate() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return compareDates(o1.getFollowUpDate(), o2.getFollowUpDate());
      }

      public boolean hasNullProperty(Task object) {
        return object.getFollowUpDate() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByCaseInstanceId() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getCaseInstanceId().compareTo(o2.getCaseInstanceId());
      }

      public boolean hasNullProperty(Task object) {
        return object.getCaseInstanceId() == null;
      }
    };
  }

  public static NullTolerantComparator<Task> taskByCaseExecutionId() {
    return new NullTolerantComparator<Task>() {
      public int compare(Task o1, Task o2) {
        return o1.getCaseExecutionId().compareTo(o2.getCaseExecutionId());
      }

      public boolean hasNullProperty(Task object) {
        return object.getCaseExecutionId() == null;
      }
    };
  }

  // HISTORIC JOB LOG

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByTimestamp() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return compareDates(o1.getTimestamp(), o2.getTimestamp());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getJobId().compareTo(o2.getJobId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobDefinitionId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getJobDefinitionId().compareTo(o2.getJobDefinitionId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobDueDate() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return compareDates(o1.getJobDueDate(), o2.getJobDueDate());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return object.getJobDueDate() == null;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobRetries() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return Integer.valueOf(o1.getJobRetries()).compareTo(Integer.valueOf(o2.getJobRetries()));
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByActivityId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getActivityId().compareTo(o2.getActivityId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return object.getActivityId() == null;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByExecutionId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getExecutionId().compareTo(o2.getExecutionId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessInstanceId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessDefinitionId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getProcessDefinitionId().compareTo(o2.getProcessDefinitionId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessDefinitionKey(ProcessEngine processEngine) {
    final RepositoryService repositoryService = processEngine.getRepositoryService();

    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        ProcessDefinition processDefinition1 = repositoryService.getProcessDefinition(o1.getProcessDefinitionId());
        ProcessDefinition processDefinition2 = repositoryService.getProcessDefinition(o1.getProcessDefinitionId());
        return processDefinition1.getId().compareTo(processDefinition2.getId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByDeploymentId() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return o1.getDeploymentId().compareTo(o2.getDeploymentId());
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobPriority() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        return Long.valueOf(o1.getJobPriority()).compareTo(Long.valueOf(o2.getJobPriority()));
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogPartiallyByOccurence() {
    return new NullTolerantComparator<HistoricJobLog>() {
      public int compare(HistoricJobLog o1, HistoricJobLog o2) {
        Long firstCounter = Long.valueOf(((HistoricJobLogEventEntity)o1).getSequenceCounter());
        Long secondCounter = Long.valueOf(((HistoricJobLogEventEntity)o2).getSequenceCounter());
        return firstCounter.compareTo(secondCounter);
      }

      public boolean hasNullProperty(HistoricJobLog object) {
        return false;
      }
    };
  }

  // jobs

  public static NullTolerantComparator<Job> jobByPriority() {
    return new NullTolerantComparator<Job>() {

      @Override
      public int compare(Job o1, Job o2) {
        return Long.valueOf(o1.getPriority())
            .compareTo(Long.valueOf(o2.getPriority()));
      }

      @Override
      public boolean hasNullProperty(Job object) {
        return false;
      }

    };
  }

  // external task

  public static NullTolerantComparator<ExternalTask> externalTaskById() {
    return new NullTolerantComparator<ExternalTask>() {

      @Override
      public int compare(ExternalTask o1, ExternalTask o2) {
        return o1.getId().compareTo(o2.getId());
      }

      @Override
      public boolean hasNullProperty(ExternalTask object) {
        return false;
      }

    };
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessInstanceId() {
    return new NullTolerantComparator<ExternalTask>() {

      @Override
      public int compare(ExternalTask o1, ExternalTask o2) {
        return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
      }

      @Override
      public boolean hasNullProperty(ExternalTask object) {
        return object.getProcessInstanceId() == null;
      }

    };
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessDefinitionId() {
    return new NullTolerantComparator<ExternalTask>() {

      @Override
      public int compare(ExternalTask o1, ExternalTask o2) {
        return o1.getProcessDefinitionId().compareTo(o2.getProcessDefinitionId());
      }

      @Override
      public boolean hasNullProperty(ExternalTask object) {
        return object.getProcessDefinitionId() == null;
      }

    };
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessDefinitionKey() {
    return new NullTolerantComparator<ExternalTask>() {

      @Override
      public int compare(ExternalTask o1, ExternalTask o2) {
        return o1.getProcessDefinitionKey().compareTo(o2.getProcessDefinitionKey());
      }

      @Override
      public boolean hasNullProperty(ExternalTask object) {
        return object.getProcessDefinitionKey() == null;
      }

    };
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByLockExpirationTime() {
    return new NullTolerantComparator<ExternalTask>() {

      @Override
      public int compare(ExternalTask o1, ExternalTask o2) {
        return o1.getLockExpirationTime().compareTo(o2.getLockExpirationTime());
      }

      @Override
      public boolean hasNullProperty(ExternalTask object) {
        return object.getLockExpirationTime() == null;
      }

    };
  }

  // general

  public static <T> NullTolerantComparator<T> inverted(final NullTolerantComparator<T> comparator) {
    return new NullTolerantComparator<T>() {
      public int compare(T o1, T o2) {
        return - comparator.compare(o1, o2);
      }

      public boolean hasNullProperty(T object) {
        return comparator.hasNullProperty(object);
      }
    };
  }

  public static <T> NullTolerantComparator<T> hierarchical(final NullTolerantComparator<T> baseComparator,
      final NullTolerantComparator<T>... minorOrderings) {
    return new NullTolerantComparator<T>() {
      public int compare(T o1, T o2, boolean nullPrecedes) {
        int comparison = baseComparator.compare(o1, o2, nullPrecedes);

        int i = 0;
        while (comparison == 0 && i < minorOrderings.length) {
          NullTolerantComparator<T> comparator = minorOrderings[i];
          comparison = comparator.compare(o1, o2, nullPrecedes);
          i++;
        }

        return comparison;
      }

      public int compare(T o1, T o2) {
        throw new UnsupportedOperationException();
      }

      public boolean hasNullProperty(T object) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public abstract static class NullTolerantComparator<T> implements Comparator<T> {

    public int compare(T o1, T o2, boolean nullPrecedes) {
      boolean o1Null = hasNullProperty(o1);
      boolean o2Null = hasNullProperty(o2);

      if (o1Null) {
        if (o2Null) {
          return 0;
        } else {
          if (nullPrecedes) {
            return -1;
          } else {
            return 1;
          }
        }
      } else {

        if (o2Null) {
          if (nullPrecedes) {
            return 1;
          } else {
            return -1;
          }
        }
      }

      return compare(o1, o2);
    }

    public abstract boolean hasNullProperty(T object);
  }

  public static <T> void verifySorting(List<T> actualElements, NullTolerantComparator<T> expectedOrdering) {
    // check two orderings: one in which values with null properties are at the front of the list
    boolean leadingNullOrdering = orderingConsistent(actualElements, expectedOrdering, true);

    if (leadingNullOrdering) {
      return;
    }

    // and one where the values with null properties are at the end of the list
    boolean trailingNullOrdering = orderingConsistent(actualElements, expectedOrdering, false);
    TestCase.assertTrue("Ordering not consistent with comparator", trailingNullOrdering);
  }

  public static <T> boolean orderingConsistent(List<T> actualElements, NullTolerantComparator<T> expectedOrdering, boolean nullPrecedes) {
    for (int i = 0; i < actualElements.size() - 1; i++) {
      T currentExecution = actualElements.get(i);
      T nextExecution = actualElements.get(i + 1);

      int comparison = expectedOrdering.compare(currentExecution, nextExecution, nullPrecedes);
      if (comparison > 0) {
        return false;
      }
    }

    return true;
  }

  public static <T> void verifySortingAndCount(Query<?, T> query, int expectedCount, NullTolerantComparator<T> expectedOrdering) {
    List<T> elements = query.list();
    TestCase.assertEquals(expectedCount, elements.size());

    verifySorting(elements, expectedOrdering);
  }

  public static int compareDates(Date date1, Date date2) {
    boolean before = date1.before(date2);
    boolean after = date1.after(date2);

    if (before) {
      return -1;
    } else if (after) {
      return 1;
    } else {
      return 0;
    }
  }
}
