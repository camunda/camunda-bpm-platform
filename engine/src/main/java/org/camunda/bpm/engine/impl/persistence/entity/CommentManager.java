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

package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class CommentManager extends AbstractHistoricManager {

  public void delete(DbEntity dbEntity) {
    checkHistoryEnabled();
    super.delete(dbEntity);
  }

  public void insert(DbEntity dbEntity) {
    checkHistoryEnabled();
    super.insert(dbEntity);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    return getDbEntityManager().selectList("selectCommentsByTaskId", taskId);
  }

  @SuppressWarnings("unchecked")
  public List<Event> findEventsByTaskId(String taskId) {
    checkHistoryEnabled();

    ListQueryParameterObject query = new ListQueryParameterObject();
    query.setParameter(taskId);
    query.getOrderingProperties().add(new QueryOrderingProperty(new QueryPropertyImpl("TIME_"), Direction.DESCENDING));

    return getDbEntityManager().selectList("selectEventsByTaskId", query);
  }

  public void deleteCommentsByTaskId(String taskId) {
    checkHistoryEnabled();
    getDbEntityManager().delete(CommentEntity.class, "deleteCommentsByTaskId", taskId);
  }

  public void deleteCommentsByProcessInstanceIds(List<String> processInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceIds", processInstanceIds);
    deleteComments(parameters);
  }

  public void deleteCommentsByTaskProcessInstanceIds(List<String> processInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskProcessInstanceIds", processInstanceIds);
    deleteComments(parameters);
  }

  public void deleteCommentsByTaskCaseInstanceIds(List<String> caseInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskCaseInstanceIds", caseInstanceIds);
    deleteComments(parameters);
  }

  protected void deleteComments(Map<String, Object> parameters) {
    getDbEntityManager().deletePreserveOrder(CommentEntity.class, "deleteCommentsByIds", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findCommentsByProcessInstanceId(String processInstanceId) {
    checkHistoryEnabled();
    return getDbEntityManager().selectList("selectCommentsByProcessInstanceId", processInstanceId);
  }

  public CommentEntity findCommentByTaskIdAndCommentId(String taskId, String commentId) {
    checkHistoryEnabled();

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("taskId", taskId);
    parameters.put("id", commentId);

    return (CommentEntity) getDbEntityManager().selectOne("selectCommentByTaskIdAndCommentId", parameters);
  }

}
