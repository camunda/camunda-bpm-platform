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

"use strict";

var CAM = {};

/**
 * The core process engine.
 * @author Daniel Meyer
 */
(function(CAM) {

  var ExecutionException = (function () {

    function ExecutionException(message, activityExecution) {
      this.message = message;
      this.activityExecution = activityExecution;
      throw message;
    }

    return ExecutionException;
  })(); 

  /** 
   * the activity types to be used by the process engine.
   * An activity type realizes the process language specific 
   * behavior of an activity.
   * 
   */
  var activityTypes = { };

  var LISTENER_START = "start";
  var LISTENER_END = "end";
  var LISTENER_TAKE = "take";

  // static utility functions ////////////////////////////////////

  var getActivitiesByType = function(activityDefinition, type, recursive) {
    var baseElements = [];
    for (var i = 0; i < activityDefinition.baseElements.length; i++) { 
      var childActivity = activityDefinition.baseElements[i];
      if(!!childActivity.type && childActivity.type == type){
        baseElements.push(childActivity);
        if(recursive) {
          baseElements = baseElements.concat(getActivitiesByType(childActivity, type, recursive));
        }
      }        
    }
    return baseElements;
  };

  var getActivityById = function(activityDefinition, id) {
    for (var i = 0; i < activityDefinition.baseElements.length; i++) { 
      var chidActivity = activityDefinition.baseElements[i];
      if(!!chidActivity.id && chidActivity.id == id){
        return chidActivity;
      }        
    }
    return null;
  };

  var getActivityType = function(activityDefinition) {
    var type = activityDefinition.type;
    if(!!type) {
      return activityTypes[type];
    } else {
      return null;
    }      
  };

  var getSequenceFlows = function(activityDefinition, scopeActivity) {
    var result = [];
    if(!!activityDefinition.outgoing) {
      var outgoingSequenceFlowIds = activityDefinition.outgoing;
      
      for (var i = 0; i < outgoingSequenceFlowIds.length; i++) { 
        var sequenceFlowId = outgoingSequenceFlowIds[i];
        result.push(getActivityById(scopeActivity, sequenceFlowId));      
      }
    }

    return result;
  };


  ///////////////////////////////////////////////////////////////
  
  var ActivityExecution = (function () {

    // constructor
    function ActivityExecution(activityDefinition, parentExecution) { 

      if(!activityDefinition) {
        throw new ExecutionException("Activity definition cannot be null", this);
      }
          
      this.activityDefinition = activityDefinition;    
      // a list of child activity executions
      this.activityExecutions = [];
      // indicates whether the execution has been ended
      this.isEnded = false;
      // the parent execution
      this.parentExecution = parentExecution;   
      // the variables of this execution
      this.variables = {};  

      this.startDate = null; 
      this.endDate = null; 
    }

    ActivityExecution.prototype.bindVariableScope = function(scope) {
      if(!!this.parentExecution) {
        this.parentExecution.bindVariableScope(scope);
      }
      var variables = this.variables;
      for(var varName in variables) {
        scope[varName] = variables[varName];
      }
    }

    ActivityExecution.prototype.executeActivities = function(activities) {
      for (var i = 0; i < activities.length; i++) {
        this.executeActivity(activities[i]);        
      }; 
    };

    ActivityExecution.prototype.executeActivity = function(activity, sequenceFlow) {          
      var childExecutor = new ActivityExecution(activity, this);                 
      this.activityExecutions.push(childExecutor);
       if(!!sequenceFlow) {
        childExecutor.incomingSequenceFlowId = sequenceFlow.id; 
      }
      childExecutor.start();
    };

    ActivityExecution.prototype.invokeListeners = function(type, sequenceFlow) {      
      var listeners = this.activityDefinition.listeners;
      if(!!listeners) {
        for(var i = 0; i < listeners.length; i++) {
          var listener = listeners[i];
          if(!!listener[type]) {
            listener[type](this, sequenceFlow);
          }
        }
      }
    };
   
    ActivityExecution.prototype.start = function() {   
      this.startDate = new Date();

      // invoke listeners on activity start
      this.invokeListeners(LISTENER_START);  

      // if the activity is async, we do not execute it right away 
      // but simpley return. Execution can be continued using the 
      // continue() function
      if(!!this.activityDefinition.asyncCallback) {
        this.activityDefinition.asyncCallback(this);
      } else {
        this.continue();
      }
    };

    ActivityExecution.prototype.continue = function() {
   
      // execute activity type
      var activityType = getActivityType(this.activityDefinition);
      activityType.execute(this);      
    };

    ActivityExecution.prototype.end = function(notifyParent) {
      this.isEnded = true;
      this.endDate = new Date();

      // invoke listeners on activity end
      this.invokeListeners(LISTENER_END);      
      
      if(!!this.parentExecution) {
        // remove from parent
        var parent = this.parentExecution;
        // notify parent
        if(notifyParent) {
          parent.hasEnded(this);   
        }        
      }   
    };

    ActivityExecution.prototype.takeAll = function(sequenceFlows) {
      for(var i = 0; i < sequenceFlows.length; i++) {
        this.take(sequenceFlows[i]);
      }
    };

    ActivityExecution.prototype.take = function(sequenceFlow) {
      var toId = sequenceFlow.targetRef;
      var toActivity = getActivityById(this.parentExecution.activityDefinition, toId);
      if(!toActivity) {
        throw new ExecutionException("cannot find activity with id '"+toId+"'");
      }      
      // end this activity
      this.end(false);

      // invoke listeners on sequence flow take      
      this.invokeListeners(LISTENER_TAKE, sequenceFlow);     

      // have the parent execute the next activity
      this.parentExecution.executeActivity(toActivity, sequenceFlow);
    };

    ActivityExecution.prototype.signal = function(definitionId) {
      var signalFn = function (execution) {
        if(execution.isEnded) {
          throw new ExecutionException("cannot signal an ended activity instance", execution);
        }
        var type = getActivityType(execution.activityDefinition);      
        if(!!type.signal) {
          type.signal(execution);
        } else {
          execution.end();
        }
      };

      if (definitionId) {
        for (var index in this.activityExecutions) {
          var execution = this.activityExecutions[index];
          if (execution.activityDefinition.id == definitionId) {
            signalFn(execution);
            break;
          }
        }
      }else {
        signalFn(this);
      }
    };

    /**
     * called by the child activity executors when they are ended
     */
    ActivityExecution.prototype.hasEnded = function(activityExecution) {
      var allEnded = true;
      for(var i; i < this.activityExecutions.length; i++) {
        allEnded &= this.activityExecutions[i].isEnded;
      }

      if(allEnded) {
        var activityType = getActivityType(this.activityDefinition);
        if(!!activityType.allActivitiesEnded) {
          activityType.allActivitiesEnded(this);
        } else {
          this.end();
        }
      }
    };

    /**
     * an activity instance is a java script object that holds the state of an 
     * ActivityExecution. It can be regarded as the serialized representation
     * of an execution tree. 
     */
    ActivityExecution.prototype.getActivityInstance = function() {      
      var activityInstance = {
        "activityId" : this.activityDefinition.id,
        "isEnded" : this.isEnded,
        "startDate" : this.startDate,
        "endDate" : this.endDate,
      }
      if(this.activityExecutions.length > 0) {
        activityInstance["activities"] = [];
        for(var i = 0; i < this.activityExecutions.length; i++) {
          activityInstance.activities.push(this.activityExecutions[i].getActivityInstance());
        }  
      }      
      return activityInstance;
    };

    return ActivityExecution;
  })();


  // export public APIs 
  CAM.ActivityExecution = ActivityExecution;
  CAM.ExecutionException = ExecutionException;
  CAM.activityTypes = activityTypes;
  CAM.getActivitiesByType = getActivitiesByType;
  CAM.getActivityById = getActivityById;
  CAM.getActivityType = getActivityType;
  CAM.getSequenceFlows = getSequenceFlows;

  CAM.LISTENER_START = LISTENER_START;
  CAM.LISTENER_END = LISTENER_END;
  CAM.LISTENER_TAKE = LISTENER_TAKE;

})(CAM);


/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

/**
 * The BPMN 2.0 activity type module.
 *
 * This module provides the BPMN 2.0 specific runtime behavior
 * 
 * @author Daniel Meyer
 */
(function(CAM) {

  // variables & conditions //////////////////////////////////////////

  var VariableScope = (function () {

    function VariableScope(activityExecution) {
      activityExecution.bindVariableScope(this);      
    };

    VariableScope.prototype.evaluateCondition = function(condition) {            
      return eval(condition);
    };

    return VariableScope;
  })(); 

  function evaluateCondition(condition, activityExecution) {
    return new VariableScope(activityExecution).evaluateCondition(condition);    
  }

  // the default outgoing behavior for BPMN 2.0 activities //////////

  function leave(activityExecution) {

    // SEPC p.427 §13.2.1
    // Multiple outgoing Sequence Flows behaves as a parallel split. 
    // Multiple outgoing Sequence Flows with conditions behaves as an inclusive split. 
    // A mix of multiple outgoing Sequence Flows with and without conditions is considered as a combination of a parallel and an inclusive split
    
    var sequenceFlowsToTake = [];
    var availableSequenceFlows = CAM.getSequenceFlows(activityExecution.activityDefinition, 
                                                  activityExecution.parentExecution.activityDefinition);
    var defaultFlowId = activityExecution.activityDefinition.default;

    var defaultFlow = null;
    var noConditionalFlowActivated = true;    
    
    for(var i =0; i<availableSequenceFlows.length; i++) {
      var sequenceFlow = availableSequenceFlows[i];

      if(!!defaultFlowId && defaultFlowId == sequenceFlow.id) {
        defaultFlow = sequenceFlow;

      } else if(!sequenceFlow.condition) {
        sequenceFlowsToTake.push(sequenceFlow);
        
      } else if(evaluateCondition(sequenceFlow.condition, activityExecution)) {
        sequenceFlowsToTake.push(sequenceFlow);
        noConditionalFlowActivated = false;
      }
      
    }
    
    // the default flow is only activated if all conditional flows are false
    if(noConditionalFlowActivated && !!defaultFlow) {
      sequenceFlowsToTake.push(defaultFlow);
    }
    
    activityExecution.takeAll(sequenceFlowsToTake);
  }

  // actual activity types //////////////////////////////////////////

  var process = {
    "execute" : function(activityExecution) {
  
      // find start events        
      var startEvents = CAM.getActivitiesByType(activityExecution.activityDefinition, "startEvent");
      
      if(startEvents.length == 0) {
        throw "process must have at least one start event";
      }
      
      // activate all start events
      activityExecution.executeActivities(startEvents);        
    }      
  };

  var startEvent = {
    "execute" : function(activityExecution) {
      leave(activityExecution);
    }
  };

  var intermediateThrowEvent = {
    "execute" : function(activityExecution) {
      leave(activityExecution);
    }
  };

  var endEvent = {
    "execute" : function(activityExecution) {
      activityExecution.end(true);
    }
  };

  var task = {
    "execute" : function(activityExecution) {
      leave(activityExecution);
    }
  };

  var userTask = {
    "execute" : function(activityExecution) {
      // wait state
    },
    "signal" : function(activityExecution) {
      leave(activityExecution);
    }
  };

  var serviceTask = {
    "execute" : function(activityExecution) {
      leave(activityExecution);
    }
  };

  /**
   * implementation of the exclusive gateway
   */
  var exclusiveGateway = {
    "execute" : function(activityExecution) {
      var outgoingSequenceFlows = activityExecution.activityDefinition.sequenceFlows;

      var sequenceFlowToTake,
        defaultFlow;

      for(var i = 0; i<outgoingSequenceFlows.length; i++) {
        var sequenceFlow = outgoingSequenceFlows[i];
        if(!sequenceFlow.condition) {
          // we make sure at deploy time that there is only a single sequence flow without a condition
          defaultFlow = sequenceFlow;          
        } else if(evaluateCondition(sequenceFlow.condition, activityExecution)) {
          sequenceFlowToTake = sequenceFlow;
          break;
        }
      }

      if(!sequenceFlowToTake) {
        if(!defaultFlow) {
          throw "Cannot determine outgoing sequence flow for exclusive gateway '"+activityExecution.activityDefinition+"': " +
            "All conditions evaluate to false and a default sequence flow has not been specified."
        } else {
          sequenceFlowToTake = defaultFlow;
        }
      }

      activityExecution.take(sequenceFlowToTake);
    }
  };

  /**
   * implementation of the parallel gateway
   */
  var parallelGateway = {
    "execute" : function(activityExecution) {
      var outgoingSequenceFlows = CAM.getSequenceFlows(activityExecution.activityDefinition, 
                                                   activityExecution.parentExecution.activityDefinition);

      // join 
      var executionsToJoin = [];      
      var parent = activityExecution.parentExecution;
      for(var i=0; i<parent.activityExecutions.length; i++) {
        var sibling = parent.activityExecutions[i];
        if(sibling.activityDefinition == activityExecution.activityDefinition && !sibling.isEnded) {
          executionsToJoin.push(sibling);
        }
      }

      if(executionsToJoin.length == activityExecution.activityDefinition.cardinality) {
        // end all joined executions but this one,
        for(var i=0; i<executionsToJoin.length; i++) {
          var joinedExecution = executionsToJoin[i];
          if(joinedExecution != activityExecution) {
            joinedExecution.end(false);
          }
        }
        // continue with this execution
        activityExecution.takeAll(outgoingSequenceFlows);  
      }

    }
  };

  // register activity types
  CAM.activityTypes["startEvent"] = startEvent;
  CAM.activityTypes["intermediateThrowEvent"] = intermediateThrowEvent;
  CAM.activityTypes["endEvent"] = endEvent;
  CAM.activityTypes["exclusiveGateway"] = exclusiveGateway;
  CAM.activityTypes["task"] = task;
  CAM.activityTypes["userTask"] = userTask;
  CAM.activityTypes["serviceTask"] = serviceTask;
  CAM.activityTypes["process"] = process; 
  CAM.activityTypes["parallelGateway"] = parallelGateway;

})(CAM);
// if we are in a require enviroment, define the module
if (typeof define == 'function') {
  define([], function () {
    return CAM;
  });
}
