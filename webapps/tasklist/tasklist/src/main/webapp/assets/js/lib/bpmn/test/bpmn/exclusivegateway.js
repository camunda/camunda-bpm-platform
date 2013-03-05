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

describe('Exclusive Gateway', function() {

  it('should support one diverging flow without a condition', function() {

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

    // now the execution should be ended
    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("decision");
    expect(processInstance.activities[2].activityId).toBe("end");    
  });

  it('should not support a single diverging flow with a condition', function() {

    var t = function () {
      CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
          'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
        
          '<process id="theProcess" isExecutable="true">' +
        
            '<startEvent id="theStart" />'+
            '<exclusiveGateway id="decision" />'+    
            '<endEvent id="end" />'+
            
            '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="decision" />'+
            '<sequenceFlow id="flow2" sourceRef="decision" targetRef="end">'+
              '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
                'this.input <= 50 '+
              ']]></conditionExpression>'+
            '</sequenceFlow>' +
          
          '</process>'+
        
        '</definitions>');
    };

    expect(t).toThrow();

  });

  it('should not support multiple diverging flows without conditions', function() {

    // if there multiple outgoing sequence flows without conditions, an exception is thrown at deploy time, 
    // even if one of them is the default flow
    var t = function () {
      CAM.transform(
        '<?xml version="1.0" encoding="UTF-8"?>' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
          'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
        
          '<process id="theProcess" isExecutable="true">' +
        
            '<startEvent id="theStart" />'+
            '<exclusiveGateway id="decision" />'+    
            '<endEvent id="end1" />'+
            '<endEvent id="end2" />'+
            
            '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="decision" />'+
            '<sequenceFlow id="flow2" sourceRef="decision" targetRef="end1" />'+
            '<sequenceFlow id="flow3" sourceRef="decision" targetRef="end2" />'+
                   
          '</process>'+
        
        '</definitions>');
      };
    expect(t).toThrow();
    
  });

  it('should support two diverging flows with conditions', function() {

    var processDefinition = CAM.transform(
    '<?xml version="1.0" encoding="UTF-8"?>' +
    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
      'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
    
      '<process id="theProcess" isExecutable="true">' +
    
        '<startEvent id="theStart" />'+
        '<exclusiveGateway id="decision" />'+    
        '<endEvent id="end1" />'+
        '<endEvent id="end2" />'+
        
        '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="decision" />'+
        '<sequenceFlow id="flow2" sourceRef="decision" targetRef="end1">'+
          '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
            'this.input <= 50 '+
          ']]></conditionExpression>'+
        '</sequenceFlow>' +        
        '<sequenceFlow id="flow3" sourceRef="decision" targetRef="end2">'+
          '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
            'this.input > 50 '+
          ']]></conditionExpression>'+
        '</sequenceFlow>' +
      
      
      '</process>'+
    
    '</definitions>')[0];

    // case 1: input  = 10 -> the upper sequenceflow is taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("decision");
    expect(processInstance.activities[2].activityId).toBe("end1");    

    // case 2: input  = 100 -> the lower sequenceflow is taken

    execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 100;
    execution.start();

    expect(execution.isEnded).toBe(true);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("decision");
    expect(processInstance.activities[2].activityId).toBe("end2");    

  });

});