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

describe('Execution Listener', function() {

  var executionTrace = [];

  beforeEach(function() {
    executionTrace = [];
    CAM.parseListeners.splice(0,CAM.parseListeners.length);
  });  

  it('should invoke the execution listeners on start end and take', function() {

    CAM.parseListeners.push(function(activityDefinition){
      
      // add an execution listener to each activity definition in the process
      activityDefinition.listeners.push(
        {
          "start" : function(activityExecution) {
            executionTrace.push("start-"+activityExecution.activityDefinition.id);
          },
          "end" : function(activityExecution) {
            executionTrace.push("end-"+activityExecution.activityDefinition.id);
          },
          "take" : function(activityExecution,transition) {
            executionTrace.push("take-"+transition.id);
          }
        }
      );

    });

    var processDefinition = CAM.transform(
    '<?xml version="1.0" encoding="UTF-8"?>' +
    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
      'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
    
      '<process id="theProcess" isExecutable="true">' +
    
        '<startEvent id="theStart" />'+
        '<exclusiveGateway id="decision" />'+    
        '<endEvent id="end" />'+
        
        '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="decision" />'+
        '<sequenceFlow id="flow2" sourceRef="decision" targetRef="end" />'+
      
      '</process>'+
    
    '</definitions>')[0];

    var execution = new CAM.ActivityExecution(processDefinition);
    execution.start();

    expect(executionTrace).toEqual([ 
      'start-theProcess', 
        'start-theStart', 
        'end-theStart',
        'take-flow1',
        'start-decision', 
        'end-decision', 
        'take-flow2',
        'start-end', 
        'end-end', 
      'end-theProcess' ]);

  });
  
});