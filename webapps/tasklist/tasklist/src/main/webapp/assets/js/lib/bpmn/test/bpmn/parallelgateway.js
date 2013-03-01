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

describe('Parallel Gateway', function() {

  it('should fork multiple diverging flows', function() {

    var processDefinition = CAM.transform(
    '<?xml version="1.0" encoding="UTF-8"?>' +
    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
      'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
    
      '<process id="theProcess" isExecutable="true">' +
    
        '<startEvent id="theStart" />'+
        '<parallelGateway id="fork" />'+    
        '<endEvent id="end1" />'+
        '<endEvent id="end2" />'+
        
        '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />'+
        '<sequenceFlow id="flow2" sourceRef="fork" targetRef="end1" />'+        
        '<sequenceFlow id="flow3" sourceRef="fork" targetRef="end2" />'+
                    
      '</process>'+
    
    '</definitions>')[0];
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(4);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("fork");
    expect(processInstance.activities[2].activityId).toBe("end1");    
    expect(processInstance.activities[3].activityId).toBe("end2");    
 
  });

  it('should join multiple converging flows', function() {

    var processDefinition = CAM.transform(
    '<?xml version="1.0" encoding="UTF-8"?>' +
    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
      'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
    
      '<process id="theProcess" isExecutable="true">' +
    
        '<startEvent id="theStart" />'+
        '<parallelGateway id="fork" />'+    
        '<parallelGateway id="join" />'+

        '<endEvent id="end" />'+
        
        '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />'+
        '<sequenceFlow id="flow2" sourceRef="fork" targetRef="join" />'+        
        '<sequenceFlow id="flow3" sourceRef="fork" targetRef="join" />'+
        '<sequenceFlow id="flow4" sourceRef="join" targetRef="end" />'+
                    
      '</process>'+
    
    '</definitions>')[0];
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(5);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("fork");
    expect(processInstance.activities[2].activityId).toBe("join");    
    expect(processInstance.activities[3].activityId).toBe("join");    
    expect(processInstance.activities[4].activityId).toBe("end");   
 
  });

});