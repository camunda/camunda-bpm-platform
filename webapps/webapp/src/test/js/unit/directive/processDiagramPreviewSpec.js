/* global define: false, describe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, expect: false */
define([
  'angular',
  'jquery',
  'cockpit/directives/processDiagramPreview',
  'cockpit/resources/processDefinitionResource',
  'camunda-common/services/uri',
  'angular-resource'
], function(angular, $) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    describe('process diagram preview directive', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      afterEach(function() {
        $(document.body).html('');
        /* global dealoc: false */
        dealoc(element);
      });

      beforeEach(function () {
        angular.module('testmodule', [ 'cockpit.directives',
                                       'cockpit.resources',
                                       'camunda.common.services.uri',
                                       'ngResource' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      beforeEach(inject(function($httpBackend) {
        // backend definition common for all tests
        $httpBackend
          .when('GET', 'engine://engine/process-definition/FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726/xml')
          .respond(
            {
              id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726',
              bpmn20Xml: '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:activiti="http://activiti.org/bpmn" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_QzotMMIpEeKp3rR3eqX9hQ"><bpmn2:process id="FailingProcess" name="FailingProcess" isExecutable="true">    <bpmn2:startEvent id="StartEvent_1" name="Start Event">      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>    </bpmn2:startEvent>    <bpmn2:serviceTask id="ServiceTask_1" activiti:class="org.camunda.bpm.pa.service.FailingDelegate" activiti:async="true" name="Service Task">      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>    </bpmn2:serviceTask>    <bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/>    <bpmn2:endEvent id="EndEvent_1" name="End Event">      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>    </bpmn2:endEvent>    <bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="EndEvent_1"/>  </bpmn2:process>  <bpmndi:BPMNDiagram id="BPMNDiagram_1" name="Test">    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="FailingProcess">      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_16" bpmnElement="StartEvent_1">        <dc:Bounds height="36.0" width="36.0" x="296.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNShape id="_BPMNShape_ServiceTask_3" bpmnElement="ServiceTask_1">        <dc:Bounds height="80.0" width="100.0" x="382.0" y="237.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_16" targetElement="_BPMNShape_ServiceTask_3">        <di:waypoint xsi:type="dc:Point" x="332.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="382.0" y="277.0"/>      </bpmndi:BPMNEdge>      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_18" bpmnElement="EndEvent_1">        <dc:Bounds height="36.0" width="36.0" x="532.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_3" targetElement="_BPMNShape_EndEvent_18">        <di:waypoint xsi:type="dc:Point" x="482.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="532.0" y="277.0"/>      </bpmndi:BPMNEdge>    </bpmndi:BPMNPlane>  </bpmndi:BPMNDiagram></bpmn2:definitions>'
            });
      }));

      xit('should render process diagram preview', inject(function($rootScope, $compile, $httpBackend) {
        // given
        $rootScope.processDefinitionId = 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726';

        // when
        element = createElement('<div process-diagram-preview process-definition-id="processDefinitionId"></div>');
        element = $compile(element)($rootScope);

        $httpBackend.expectGET('engine://engine/process-definition/FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726/xml');

        $rootScope.$digest();
        $httpBackend.flush();

        // then
        expect(element.attr('id')).toBe('processDiagram_FailingProcess_1_d91f75f6-d1cb-11e2-95b0-f0def1557726');
        expect(element.html()).toBe('<svg overflow="hidden" width="800" height="600" style="width: 800px; height: 600px;"><defs></defs><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)"><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="332.00000000 277.00000000 382.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M374 281 L 382 277 L374 273 Z" d="M 374 281L 382 277L 374 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="482.00000000 277.00000000 532.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M524 281 L 532 277 L524 273 Z" d="M 524 281L 532 277L 524 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,296.75000000,259.75000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="16.5" cy="16.5" r="16.5" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,314.75000000,312.25000000)">Start Event</text><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,382.00000000,237.00000000)"><rect fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" width="100" height="80" ry="5" rx="5" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></rect><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 20.347,4.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 L 7.46,17.788 4.897,20.35 2.506,17.958 5.066,15.397 4.124,13.12 l -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 20.347 4.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622L 7.46 17.788 4.897 20.35 2.506 17.958 5.066 15.397 4.124 13.12l-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 15.141,11.426 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.0511855,0 -3.7139999,-1.662815 -3.7139999,-3.714 0,-2.0511859 1.6628144,-3.7140003 3.7139999,-3.7140003 2.051186,0 3.714,1.6628144 3.714,3.7140003 z" d="m 15.141 11.426c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.7140-1.6628-3.7140-3.714 0-2.0512 1.6628-3.7140 3.7140-3.7140 2.0512 0 3.714 1.6628 3.714 3.7140z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 26.347,10.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 -2.277,-0.943 -2.563,2.562 -2.391,-2.392 2.56,-2.561 -0.942,-2.277 -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 26.347 10.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622-2.277-0.943-2.563 2.562-2.391-2.392 2.56-2.561-0.942-2.277-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)" fill-rule="evenodd"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 21.141,17.426001 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.051186,0 -3.714,-1.662815 -3.714,-3.714 0,-2.051186 1.662814,-3.714 3.714,-3.714 2.051186,0 3.714,1.662814 3.714,3.714 z" d="m 21.141 17.4260c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.714-1.6628-3.714-3.714 0-2.0512 1.6628-3.714 3.714-3.714 2.0512 0 3.714 1.6628 3.714 3.714z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,432.00000000,277.00000000)">Service Task</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="332.00000000 277.00000000 382.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M374 281 L 382 277 L374 273 Z" d="M 374 281L 382 277L 374 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,533.50000000,260.50000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="3" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="15" cy="15" r="15" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,551.50000000,311.50000000)">End Event</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="482.00000000 277.00000000 532.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M524 281 L 532 277 L524 273 Z" d="M 524 281L 532 277L 524 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g></g></svg>');
        expect(element.text()).toBe('Start EventService TaskEnd Event');

      }));

    });
  });
});
