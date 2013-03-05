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

describe('Sequenceflow', function() {

    it('should not support the default flow if there are no conditional flows', function() {

    var t = function () {
      CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
          'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
        
          '<process id="theProcess" isExecutable="true">' +
        
            '<startEvent id="theStart" default="flow1" />'+            
            '<endEvent id="theEnd" />'+
            
            '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="theEnd" />'+           
          
          '</process>'+
        
        '</definitions>');
    };

    expect(t).toThrow("Activity with id 'theStart' declares default flow with id 'flow1' but has no conditional flows.");

  });

  it('should support the default flow if there is one conditional flow', function() {

    var processDefinition = CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
      '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
      
        '<process id="theProcess" isExecutable="true">' +
      
          '<startEvent id="theStart" default="flow1" />'+            
          '<endEvent id="theEnd1" />'+
          '<endEvent id="theEnd2" />'+
          
          '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="theEnd1" />'+           
          '<sequenceFlow id="flow2" sourceRef="theStart" targetRef="theEnd2">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 50 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +

        '</process>'+
      
      '</definitions>')[0];

    // case 1: input  = 10 -> the conditional flow is taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd2");    

    // case 2: input  = 100 -> the default flow is taken

    execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 100;
    execution.start();

    expect(execution.isEnded).toBe(true);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd1");  

  });

  it('should support the default flow in combination with multiple conditional flows', function() {

    var processDefinition = CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
      '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
      
        '<process id="theProcess" isExecutable="true">' +
      
          '<startEvent id="theStart" default="flow1" />'+            
          '<endEvent id="theEnd1" />'+
          '<endEvent id="theEnd2" />'+
          '<endEvent id="theEnd3" />'+
          
          '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="theEnd1" />'+           
          '<sequenceFlow id="flow2" sourceRef="theStart" targetRef="theEnd2">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 50 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +
          '<sequenceFlow id="flow3" sourceRef="theStart" targetRef="theEnd3">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 20 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +

        '</process>'+
      
      '</definitions>')[0];

    // case 1: input  = 10 -> both conditional flows are taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd2");    
    expect(processInstance.activities[2].activityId).toBe("theEnd3");

    // case 1: input  = 40 -> only the first conditional flow is taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 40;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd2");  

    // case 2: input  = 100 -> the default flow is taken

    execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 100;
    execution.start();

    expect(execution.isEnded).toBe(true);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd1");    

  });

  it('should support the default flow in combination with multiple conditional and unconditional flows', function() {

    var processDefinition = CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
      '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
      
        '<process id="theProcess" isExecutable="true">' +
      
          '<startEvent id="theStart" default="flow1" />'+            
          '<endEvent id="theEnd1" />'+
          '<endEvent id="theEnd2" />'+
          '<endEvent id="theEnd3" />'+
          '<endEvent id="theEnd4" />'+
          
          '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="theEnd1" />'+           
          '<sequenceFlow id="flow2" sourceRef="theStart" targetRef="theEnd2">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 50 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +
          '<sequenceFlow id="flow3" sourceRef="theStart" targetRef="theEnd3">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 20 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +
          '<sequenceFlow id="flow4" sourceRef="theStart" targetRef="theEnd4" />'+           

        '</process>'+
      
      '</definitions>')[0];

    // case 1: input  = 10 -> both conditional flows & the unconditional flow are taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(4);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd2");    
    expect(processInstance.activities[2].activityId).toBe("theEnd3");
    expect(processInstance.activities[3].activityId).toBe("theEnd4");

    // case 1: input  = 40 -> only the first conditional flow & the unconditional flow are taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 40;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd2"); 
    expect(processInstance.activities[2].activityId).toBe("theEnd4"); 

    // case 2: input  = 100 -> the default flow & the unconditional flow are taken

    execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 100;
    execution.start();

    expect(execution.isEnded).toBe(true);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd4");    
    expect(processInstance.activities[2].activityId).toBe("theEnd1");    

  });


  it('should support multiple conditional flows', function() {

    var processDefinition = CAM.transform('<?xml version="1.0" encoding="UTF-8"?>' +
      '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" '+
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'+
      
        '<process id="theProcess" isExecutable="true">' +
      
          '<startEvent id="theStart" />'+            
          '<endEvent id="theEnd1" />'+
          '<endEvent id="theEnd2" />'+
                     
          '<sequenceFlow id="flow1" sourceRef="theStart" targetRef="theEnd1">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 50 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +
          '<sequenceFlow id="flow2" sourceRef="theStart" targetRef="theEnd2">'+
            '<conditionExpression xsi:type="tFormalExpression"><![CDATA['+ 
              'this.input <= 20 '+
            ']]></conditionExpression>'+
          '</sequenceFlow>' +

        '</process>'+
      
      '</definitions>')[0];

    // case 1: input  = 10 -> both conditional flows are taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 10;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(3);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd1");    
    expect(processInstance.activities[2].activityId).toBe("theEnd2");

    // case 1: input  = 40 -> only the first conditional flow is taken
  
    var execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 40;
    execution.start();

    expect(execution.isEnded).toBe(true);

    var processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(2);
    expect(processInstance.activities[0].activityId).toBe("theStart");
    expect(processInstance.activities[1].activityId).toBe("theEnd1");  

    // case 2: input  = 100 -> no sequenceflow is taken.
    // TODO: should this trigger an exception??

    execution = new CAM.ActivityExecution(processDefinition);    
    execution.variables.input = 100;
    execution.start();

    expect(execution.isEnded).toBe(false);

    processInstance = execution.getActivityInstance(); 
    expect(processInstance.activities.length).toBe(1);
    expect(processInstance.activities[0].activityId).toBe("theStart");  

  });

});