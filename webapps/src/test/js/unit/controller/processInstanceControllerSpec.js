/* global define: false, describe: false, xdescribe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, it: false, expect: false */
/* jshint unused: false */
define([
  'angular',
  'cockpit/pages/processInstance',
  'cockpit/resources/processDefinitionResource',
  'cockpit/resources/processInstanceResource',
  'cockpit/resources/incidentResource',
  'cockpit/filters/shorten',
  'cockpit-plugin/view',
  'angular-resource',
  'camunda-common/services/uri'
], function(angular) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('controllers', function() {

    xdescribe('process instance controller', function() {

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.pages',
                                       'cockpit.resources',
                                       'cockpit.services',
                                       'cockpit.filters',
                                       'cockpit.plugin',
                                       'camunda.common.services.uri',
                                       'ngResource' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      beforeEach(inject(function($routeParams) {
        $routeParams.processDefinitionId = 'aProcessDefinitionId';
        $routeParams.processInstanceId = 'aProcessInstanceId';
      }));


      beforeEach(inject(function($httpBackend) {
        // backend definition common for all tests
        $httpBackend
          .when('GET', 'engine://engine/process-definition/aProcessDefinitionId')
          .respond(
            {
              category: 'http://www.signavio.com/bpmn20',
              deploymentId: 'ffa2fcb1-df08-11e2-a76d-f0def1557726',
              description: null,
              diagram: null,
              id: 'aProcessDefinitionId',
              key: 'FailingProcess',
              name: 'FailingProcess',
              suspended: false,
              version: 1
            });

        $httpBackend
          .when('GET', 'engine://engine/process-definition/aProcessDefinitionId/xml')
          .respond(
            {
              id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
              bpmn20Xml: '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>'
            });

        $httpBackend
          .when('GET', 'engine://engine/process-instance/aProcessInstanceId/activity-instances')
          .respond(
              {
                id : 'instance_1',
                parentActivityInstanceId : null,
                activityId : 'FailingProcess',
                processInstanceId : 'instance_1',
                processDefinitionId : 'aProcessDefinitionId',
                childActivityInstances : [ {
                  id : 'instance_2',
                  parentActivityInstanceId : 'instance_1',
                  activityId : 'ServiceTask_1',
                  processInstanceId : 'instance_1',
                  processDefinitionId : 'aProcessDefinitionId',
                  childActivityInstances : [ ],
                  childTransitionInstances : [ ],
                  executionIds : [ 'instance_2' ]
                }, {
                  id : 'instance_3',
                  parentActivityInstanceId : 'instance_1',
                  activityId : 'UserTask_1',
                  processInstanceId : 'instance_1',
                  processDefinitionId : 'aProcessDefinitionId',
                  childActivityInstances : [ ],
                  childTransitionInstances : [ ],
                  executionIds : [ 'instance_3' ]
                } ],
                childTransitionInstances : [ ],
                executionIds : [ 'instance_1' ]
              });

        $httpBackend
          .when('GET', 'plugin://base/process-instance/aProcessInstanceId/incidents')
          .respond([]);

      }));

      it('should select bpmn element', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        var serviceTask = $rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0];

        $rootScope.selection.treeDiagramMapping = {activityInstances : [ serviceTask ]};

        $rootScope.$digest();

        // then
        var selectedBpmnElements = $rootScope.selection.treeDiagramMapping.bpmnElements;
        expect(selectedBpmnElements).toBeDefined();
        expect(selectedBpmnElements.length).toBe(1);

        var bpmnElement = selectedBpmnElements[0];
        expect(bpmnElement).toBe($rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1);

      }));

      it('should select new bpmn element', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var serviceTask = $rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0];

        $rootScope.selection.treeDiagramMapping = {activityInstances : [ serviceTask ]};

        $rootScope.$digest();

        // when
        var userTask = $rootScope.processInstance.activityIdToInstancesMap.UserTask_1[0];

        $rootScope.selection.treeDiagramMapping = {activityInstances : [ userTask ]};

        $rootScope.$digest();

        // then
        var selectedBpmnElements = $rootScope.selection.treeDiagramMapping.bpmnElements;
        expect(selectedBpmnElements).toBeDefined();
        expect(selectedBpmnElements.length).toBe(1);

        var bpmnElement = selectedBpmnElements[0];
        expect(bpmnElement).toBe($rootScope.processInstance.activityIdToBpmnElementMap.UserTask_1);

      }));

      it('should select all bpmn elements (Service Task and User Task)', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        var serviceTask = $rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0];
        var userTask = $rootScope.processInstance.activityIdToInstancesMap.UserTask_1[0];

        $rootScope.selection.treeDiagramMapping = {activityInstances : [ serviceTask, userTask ]};

        $rootScope.$digest();

        // then
        var selectedBpmnElements = $rootScope.selection.treeDiagramMapping.bpmnElements;
        expect(selectedBpmnElements).toBeDefined();
        expect(selectedBpmnElements.length).toBe(2);

        expect(selectedBpmnElements[0]).toBe($rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1);
        expect(selectedBpmnElements[1]).toBe($rootScope.processInstance.activityIdToBpmnElementMap.UserTask_1);
      }));

      it('should deselect all bpmn elements selections', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var serviceTask = $rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0];
        var userTask = $rootScope.processInstance.activityIdToInstancesMap.UserTask_1[0];

        $rootScope.selection.treeDiagramMapping = {activityInstances : [ serviceTask, userTask ]};

        $rootScope.$digest();

        // when
        $rootScope.selection.treeDiagramMapping = {activityInstances : [ ]};

        $rootScope.$digest();

        // then
        var selectedBpmnElements = $rootScope.selection.treeDiagramMapping.bpmnElements;
        expect(selectedBpmnElements).toBeDefined();
        expect(selectedBpmnElements.length).toBe(0);
      }));

      it('should set the bpmn element to scroll to', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        var serviceTask = $rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0];

        $rootScope.selection.treeDiagramMapping = {scrollTo : serviceTask };

        $rootScope.$digest();

        // then
        var scrollToBpmnElement = $rootScope.selection.treeDiagramMapping.scrollToBpmnElement;
        expect(scrollToBpmnElement).toBeDefined();
        expect(scrollToBpmnElement).toBe($rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1);
      }));

      it('should select node', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        var serviceTask = $rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1;

        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ serviceTask ]};

        $rootScope.$digest();

        // then
        var selectedActivityInstances = $rootScope.selection.treeDiagramMapping.activityInstances;
        expect(selectedActivityInstances).toBeDefined();
        expect(selectedActivityInstances.length).toBe(1);

        var node = selectedActivityInstances[0];
        expect(node).toBe($rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0]);

      }));

      it('should select new node', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var serviceTask = $rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1;

        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ serviceTask ]};

        $rootScope.$digest();

        // when
        var userTask = $rootScope.processInstance.activityIdToBpmnElementMap.UserTask_1;

        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ userTask ]};

        $rootScope.$digest();

        // then
        var selectedActivityInstances = $rootScope.selection.treeDiagramMapping.activityInstances;
        expect(selectedActivityInstances).toBeDefined();
        expect(selectedActivityInstances.length).toBe(1);

        var node = selectedActivityInstances[0];
        expect(node).toBe($rootScope.processInstance.activityIdToInstancesMap.UserTask_1[0]);

      }));

      it('should select two nodes', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        // when
        var serviceTask = $rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1;
        var userTask = $rootScope.processInstance.activityIdToBpmnElementMap.UserTask_1;

        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ serviceTask, userTask ]};

        $rootScope.$digest();

        // then
        var selectedActivityInstances = $rootScope.selection.treeDiagramMapping.activityInstances;
        expect(selectedActivityInstances).toBeDefined();
        expect(selectedActivityInstances.length).toBe(2);

        expect(selectedActivityInstances[0]).toBe($rootScope.processInstance.activityIdToInstancesMap.ServiceTask_1[0]);
        expect(selectedActivityInstances[1]).toBe($rootScope.processInstance.activityIdToInstancesMap.UserTask_1[0]);
      }));

      it('should deselect all selected node', inject(function($rootScope, $controller, $httpBackend) {
        // given
        var pc = $controller('ProcessInstanceController', { $scope: $rootScope });

        $rootScope.$digest();

        $httpBackend.flush();

        var serviceTask = $rootScope.processInstance.activityIdToBpmnElementMap.ServiceTask_1;
        var userTask = $rootScope.processInstance.activityIdToBpmnElementMap.UserTask_1;

        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ serviceTask, userTask ]};

        $rootScope.$digest();

        // when
        $rootScope.selection.treeDiagramMapping = {bpmnElements : [ ]};

        $rootScope.$digest();

        // then
        var selectedActivityInstances = $rootScope.selection.treeDiagramMapping.activityInstances;
        expect(selectedActivityInstances).toBeDefined();
        expect(selectedActivityInstances.length).toBe(0);
      }));

    });
  });
});
