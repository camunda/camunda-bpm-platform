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

describe('Parse listerner', function() {

  var activityIds; 

  beforeEach(function() {
    activityIds = [];    
    CAM.parseListeners.splice(0,CAM.parseListeners.length);
  });  

  it('should invoke the parse listener for each activity created', function() {

    CAM.parseListeners.push(function(activityDefinition){
      activityIds.push(activityDefinition.id);    
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

    expect(activityIds).toEqual(["theStart","decision","end","theProcess"]);
    
  });

  it('should support multiple parse listeners', function() {

    CAM.parseListeners.push(function(activityDefinition){
       activityIds.push(activityDefinition.id);    
    });

    CAM.parseListeners.push(function(activityDefinition){
       activityIds.push(activityDefinition.id);    
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

    expect(activityIds).toEqual(["theStart","theStart","decision","decision","end","end","theProcess","theProcess"]);

  });
  
});