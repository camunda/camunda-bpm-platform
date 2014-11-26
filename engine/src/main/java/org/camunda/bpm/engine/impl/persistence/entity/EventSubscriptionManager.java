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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.EventSubscription;


/**
 * @author Daniel Meyer
 */
public class EventSubscriptionManager extends AbstractManager {
  
  /** keep track of subscriptions created in the current command */
  protected List<SignalEventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<SignalEventSubscriptionEntity>();
  
  public void insert(EventSubscriptionEntity persistentObject) {
    super.insert(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.add((SignalEventSubscriptionEntity)persistentObject);
    }
  }
  
  public void deleteEventSubscription(EventSubscriptionEntity persistentObject) {
    getDbEntityManager().delete(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.remove(persistentObject);
    }
  }
    
  public EventSubscriptionEntity findEventSubscriptionbyId(String id) {
    return (EventSubscriptionEntity) getDbEntityManager().selectOne("selectEventSubscription", id);
  }

  public long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl) {
    final String query = "selectEventSubscriptionCountByQueryCriteria"; 
    return (Long) getDbEntityManager().selectOne(query, eventSubscriptionQueryImpl);
  }

  @SuppressWarnings("unchecked")
  public List<EventSubscription> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    final String query = "selectEventSubscriptionByQueryCriteria"; 
    return getDbEntityManager().selectList(query, eventSubscriptionQueryImpl, page);
  }

  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName) {
    final String query = "selectSignalEventSubscriptionsByEventName";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbEntityManager().selectList(query, eventName));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(eventName.equals(entity.getEventName())) {
        selectList.add(entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(String executionId) {
    final String query = "selectSignalEventSubscriptionsByExecution";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbEntityManager().selectList(query, executionId));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())) {
        selectList.add(entity);
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId) {
    final String query = "selectSignalEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventName", name);    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbEntityManager().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())
         && name.equals(entity.getEventName())) {
        selectList.add(entity);
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(String executionId, String type) {
    final String query = "selectEventSubscriptionsByExecutionAndType";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);    
    return getDbEntityManager().selectList(query, params);
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(String executionId) {
    final String query = "selectEventSubscriptionsByExecution";    
    return getDbEntityManager().selectList(query, executionId);
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbEntityManager().selectList(query, params);
  }

  public List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration(String type, String configuration) {
    final String query = "selectEventSubscriptionsByConfiguration";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("configuration", configuration);
    return getDbEntityManager().selectList(query, params);
  }

  public List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName) {
    final String query = "selectEventSubscriptionsByName";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);    
    return getDbEntityManager().selectList(query, params);
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    final String query = "selectEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    params.put("executionId", executionId);    
    return getDbEntityManager().selectList(query, params);
  }

  public MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName) {
    MessageEventSubscriptionEntity entity = (MessageEventSubscriptionEntity) getDbEntityManager().selectOne("selectMessageStartEventSubscriptionByName", messageName);
    return entity;
  }
   
}
