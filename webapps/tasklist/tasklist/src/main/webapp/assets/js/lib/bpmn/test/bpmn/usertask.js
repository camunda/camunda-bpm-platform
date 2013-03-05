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

describe('Usertask', function() {

  it('should handle user tasks as wait states', function() {

    var processDefinition = CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
      '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
      
        '<process id="theProcess" isExecutable="true">' +
      
          '<startEvent id="theStart" />'+   
          '<userTask id="userTask" />'+
          '<endEvent id="theEnd" />'+
          
          '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="userTask" />'+           
          '<sequenceFlow id="flow2" sourceRef="userTask" targetRef="theEnd" />'+   
         
        '</process>'+
      
      '</definitions>')[0];

  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    // the activity is NOT ended
    expect(execution.isEnded).toBe(false);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("userTask");    

    // send a signal to the usertask:
    execution.activityExecutions[1].signal();  

    // now the process is ended
    expect(execution.isEnded).toBe(true);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("userTask");    
    expect(processInstance.activities[2].activityId).toBe("theEnd");    

  });


});