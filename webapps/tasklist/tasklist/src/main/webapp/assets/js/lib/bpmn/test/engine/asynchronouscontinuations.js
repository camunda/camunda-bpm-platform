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

describe('Asynchronous Continuations', function() {

  var executionTrace = [];

  afterEach(function() {
    executionTrace = [];
    CAM.parseListeners.splice(0,CAM.parseListeners.length);
  });  

  it('should invoke callback function for async activities', function() {

    var interruptedExecution;

    CAM.parseListeners.push(function(activityDefinition){
      activityDefinition.asyncCallback = function(activityExecution) {
        interruptedExecution = activityExecution;
      };
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

    expect(execution.isEnded).toBe(false);

    while(interruptedExecution) {
      var e = interruptedExecution;
      interruptedExecution = null;
      e.continue();
    }

    expect(execution.isEnded).toBe(true);

  });
  
});