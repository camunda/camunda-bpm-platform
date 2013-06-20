define([ 'angular',
         'jquery', 
         'cockpit/directives/processDiagram',
         'cockpit/directives/processInstanceIncidents',
         'cockpit/resources/processDefinitionResource',
         'cockpit/resources/incidentResource',
         'angular-resource',
         'cockpit/filters/shortenNumber' ], function(angular, $) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    describe('process instance incidents directive', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      afterEach(function() {
        $(document.body).html('');
        dealoc(element);
      });

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.directives', 
                                       'cockpit.resources.process.definition',
                                       'cockpit.resources.incident', 
                                       'ngResource',
                                       'cockpit.filters.shorten.number' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));
      
      beforeEach(inject(function($httpBackend) {
        // backend definition common for all tests
        $httpBackend
          .whenGET('engine://process-definition/FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726/xml')
          .respond(
            {
              id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
              bpmn20Xml: '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:activiti="http://activiti.org/bpmn" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_QzotMMIpEeKp3rR3eqX9hQ"><bpmn2:process id="FailingProcess" name="FailingProcess" isExecutable="true">    <bpmn2:startEvent id="StartEvent_1" name="Start Event">      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>    </bpmn2:startEvent>    <bpmn2:serviceTask id="ServiceTask_1" activiti:class="org.camunda.bpm.pa.service.FailingDelegate" activiti:async="true" name="Service Task">      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>    </bpmn2:serviceTask>    <bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/>    <bpmn2:endEvent id="EndEvent_1" name="End Event">      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>    </bpmn2:endEvent>    <bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="EndEvent_1"/>  </bpmn2:process>  <bpmndi:BPMNDiagram id="BPMNDiagram_1" name="Test">    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="FailingProcess">      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_16" bpmnElement="StartEvent_1">        <dc:Bounds height="36.0" width="36.0" x="296.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNShape id="_BPMNShape_ServiceTask_3" bpmnElement="ServiceTask_1">        <dc:Bounds height="80.0" width="100.0" x="382.0" y="237.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_16" targetElement="_BPMNShape_ServiceTask_3">        <di:waypoint xsi:type="dc:Point" x="332.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="382.0" y="277.0"/>      </bpmndi:BPMNEdge>      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_18" bpmnElement="EndEvent_1">        <dc:Bounds height="36.0" width="36.0" x="532.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_3" targetElement="_BPMNShape_EndEvent_18">        <di:waypoint xsi:type="dc:Point" x="482.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="532.0" y="277.0"/>      </bpmndi:BPMNEdge>    </bpmndi:BPMNPlane>  </bpmndi:BPMNDiagram></bpmn2:definitions>'
            });
        
        $httpBackend
        .whenGET('plugin://base/default/process-instance/2c090edb-d72c-11e2-be8e-f0def1557726/incidents')
        .respond(
            [{
             id: '38866227-d72c-11e2-be8e-f0def1557726',
             incidentTimestamp: '2013-06-17T10:59:30',
             incidentType: 'failedJob',
             executionId: '2c090edb-d72c-11e2-be8e-f0def1557726',
             activityId: 'ServiceTask_1',
             processInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
             processDefinitionId: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
             causeIncidentId: null,
             rootCauseIncidentId: null,
             configuration: '2c090ee0-d72c-11e2-be8e-f0def1557726'
            }]);
       
      }));
      
      it('should show incident on activity', inject(function($rootScope, $compile, $httpBackend) {
        $httpBackend.expectGET('plugin://base/default/process-instance/2c090edb-d72c-11e2-be8e-f0def1557726/incidents');
        
        // given process diagram
        $rootScope.processDefinitionId = 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726';
        $rootScope.processInstanceId = '2c090edb-d72c-11e2-be8e-f0def1557726';
        
        element = createElement('<process-diagram process-definition-id="processDefinitionId" process-instance-incidents />');
        element = $compile(element)($rootScope);
        
        $rootScope.$digest();
        
        $httpBackend.flush();
        
        // then
        expect(element.html()).toContain('<div id="ServiceTask_1" class="bpmnElement" style="position: absolute; left: 382px; top: 237px; width: 100px; height: 80px;"><div class="badgePosition"><p class="badge badge-important">!</p></div></div>');
        expect(element.text()).toBe('Start EventService TaskEnd Event!'); 
      }));

      it('should show incident and number activity statistics on activity', inject(function($rootScope, $compile, $httpBackend) {
        $httpBackend.expectGET('plugin://base/default/process-instance/2c090edb-d72c-11e2-be8e-f0def1557726/incidents');
        
        // given process diagram
        $rootScope.processDefinitionId = 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726';
        $rootScope.processInstanceId = '2c090edb-d72c-11e2-be8e-f0def1557726';
        $rootScope.tree = {
            id: '2c090edb-d72c-11e2-be8e-f0def1557726',
            parentActivityInstanceId: null,
            activityId: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
            processInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
            processDefinitionId: 'FailingProcess:1:2bc4b300-d72c-11e2-be8e-f0def1557726',
            childActivityInstances: [],
            childTransitionInstances:
              [{ 
                id: '2c090edb-d72c-11e2-be8e-f0def1557726',
                parentActivityInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
                processInstanceId: '2c090edb-d72c-11e2-be8e-f0def1557726',
                processDefinitionId: 'FailingProcess:1:2bc4b300-d72c-11e2-be8e-f0def1557726',
                targetActivityId: 'ServiceTask_1',
                executionId: '2c090edb-d72c-11e2-be8e-f0def1557726'
              }],
            executionIds: ['2c090edb-d72c-11e2-be8e-f0def1557726']
          };
        
        element = createElement('<process-diagram process-definition-id="processDefinitionId" activity-instances="tree" process-instance-incidents />');
        element = $compile(element)($rootScope);
        
        $rootScope.$digest();
        
        $httpBackend.flush();
        
        // then
        expect(element.html()).toContain('<div id="ServiceTask_1" class="bpmnElement" style="position: absolute; left: 382px; top: 237px; width: 100px; height: 80px;"><div class="badgePosition"><p class="badge">1</p><p class="badge badge-important">!</p></div></div>');
        expect(element.text()).toBe('Start EventService TaskEnd Event1!');
      }));
      
    });
  });
});