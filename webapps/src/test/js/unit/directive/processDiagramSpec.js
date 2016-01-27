/* global define: false, describe: false, xdescribe: false, module: false, inject: false, beforeEach: false, afterEach: false, it: false, expect: false */
define([
  'angular',
  'jquery',
  'cockpit/services/transform',
  'cockpit/filters/abbreviateNumber',
  'cockpit/directives/processDiagram'
], function(angular, $) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    xdescribe('process diagram directive', function() {
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
                                       'cockpit.services',
                                       'cockpit.filters.abbreviate.number' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));


      beforeEach(inject(function($rootScope, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:activiti="http://activiti.org/bpmn" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_QzotMMIpEeKp3rR3eqX9hQ"><bpmn2:process id="FailingProcess" name="FailingProcess" isExecutable="true">    <bpmn2:startEvent id="StartEvent_1" name="Start Event">      <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>    </bpmn2:startEvent>    <bpmn2:serviceTask id="ServiceTask_1" activiti:class="org.camunda.bpm.pa.service.FailingDelegate" activiti:async="true" name="Service Task">      <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>    </bpmn2:serviceTask>    <bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/>    <bpmn2:endEvent id="EndEvent_1" name="End Event">      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>    </bpmn2:endEvent>    <bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="EndEvent_1"/>  </bpmn2:process>  <bpmndi:BPMNDiagram id="BPMNDiagram_1" name="Test">    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="FailingProcess">      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_16" bpmnElement="StartEvent_1">        <dc:Bounds height="36.0" width="36.0" x="296.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNShape id="_BPMNShape_ServiceTask_3" bpmnElement="ServiceTask_1">        <dc:Bounds height="80.0" width="100.0" x="382.0" y="237.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_16" targetElement="_BPMNShape_ServiceTask_3">        <di:waypoint xsi:type="dc:Point" x="332.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="382.0" y="277.0"/>      </bpmndi:BPMNEdge>      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_18" bpmnElement="EndEvent_1">        <dc:Bounds height="36.0" width="36.0" x="532.0" y="259.0"/>      </bpmndi:BPMNShape>      <bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_3" targetElement="_BPMNShape_EndEvent_18">        <di:waypoint xsi:type="dc:Point" x="482.0" y="277.0"/>        <di:waypoint xsi:type="dc:Point" x="532.0" y="277.0"/>      </bpmndi:BPMNEdge>    </bpmndi:BPMNPlane>  </bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.processDefinition = {id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726', key: 'FailingProcess'};
      }));

      it('should render process diagram', inject(function($rootScope, $compile) {
        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect(element.attr('id')).toBe('processDiagram_FailingProcess_1_d91f75f6-d1cb-11e2-95b0-f0def1557726');
        expect(element.html()).toBe('<svg overflow="hidden" width="800" height="600" style="width: 800px; height: 600px;"><defs></defs><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)"><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="332.00000000 277.00000000 382.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M374 281 L 382 277 L374 273 Z" d="M 374 281L 382 277L 374 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="482.00000000 277.00000000 532.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M524 281 L 532 277 L524 273 Z" d="M 524 281L 532 277L 524 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,296.75000000,259.75000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="16.5" cy="16.5" r="16.5" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,314.75000000,312.25000000)">Start Event</text><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,382.00000000,237.00000000)"><rect fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" width="100" height="80" ry="5" rx="5" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></rect><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 20.347,4.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 L 7.46,17.788 4.897,20.35 2.506,17.958 5.066,15.397 4.124,13.12 l -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 20.347 4.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622L 7.46 17.788 4.897 20.35 2.506 17.958 5.066 15.397 4.124 13.12l-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 15.141,11.426 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.0511855,0 -3.7139999,-1.662815 -3.7139999,-3.714 0,-2.0511859 1.6628144,-3.7140003 3.7139999,-3.7140003 2.051186,0 3.714,1.6628144 3.714,3.7140003 z" d="m 15.141 11.426c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.7140-1.6628-3.7140-3.714 0-2.0512 1.6628-3.7140 3.7140-3.7140 2.0512 0 3.714 1.6628 3.714 3.7140z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 26.347,10.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 -2.277,-0.943 -2.563,2.562 -2.391,-2.392 2.56,-2.561 -0.942,-2.277 -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 26.347 10.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622-2.277-0.943-2.563 2.562-2.391-2.392 2.56-2.561-0.942-2.277-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)" fill-rule="evenodd"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 21.141,17.426001 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.051186,0 -3.714,-1.662815 -3.714,-3.714 0,-2.051186 1.662814,-3.714 3.714,-3.714 2.051186,0 3.714,1.662814 3.714,3.714 z" d="m 21.141 17.4260c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.714-1.6628-3.714-3.714 0-2.0512 1.6628-3.714 3.714-3.714 2.0512 0 3.714 1.6628 3.714 3.714z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,432.00000000,277.00000000)">Service Task</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="332.00000000 277.00000000 382.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M374 281 L 382 277 L374 273 Z" d="M 374 281L 382 277L 374 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,533.50000000,260.50000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="3" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="15" cy="15" r="15" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,551.50000000,311.50000000)">End Event</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="482.00000000 277.00000000 532.00000000 277.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M524 281 L 532 277 L524 273 Z" d="M 524 281L 532 277L 524 273Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g></g></svg><div class="bpmnElement" id="StartEvent_1" style="position: absolute; left: 296px; top: 259px; width: 36px; height: 36px;"></div><div class="bpmnElement" id="ServiceTask_1" style="position: absolute; left: 382px; top: 237px; width: 100px; height: 80px;"></div><div class="bpmnElement" id="EndEvent_1" style="position: absolute; left: 532px; top: 259px; width: 36px; height: 36px;"></div>');
        expect(element.text()).toBe('Start EventService TaskEnd Event');
      }));

      it('should render new process diagram', inject(function($rootScope, $compile, Transform) {
        // given
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="Process_1" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);
        $rootScope.processDefinition = {id: 'id_has_been_changed'};

        $rootScope.$digest();

        // then
        expect(element.attr('id')).toBe('processDiagram_id_has_been_changed');
        expect(element.html()).toBe('<svg overflow="hidden" width="800" height="600" style="width: 800px; height: 600px;"><defs></defs><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)"><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="166.00000000 242.00000000 216.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M208 246 L 216 242 L208 238 Z" d="M 208 246L 216 242L 208 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="316.00000000 242.00000000 366.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M358 246 L 366 242 L358 238 Z" d="M 358 246L 366 242L 358 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="466.00000000 242.00000000 516.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M508 246 L 516 242 L508 238 Z" d="M 508 246L 516 242L 508 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,130.75000000,224.75000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="16.5" cy="16.5" r="16.5" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,148.75000000,277.25000000)">Start Event</text><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,216.00000000,202.00000000)"><rect fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" width="100" height="80" ry="5" rx="5" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></rect><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 20.347,4.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 L 7.46,17.788 4.897,20.35 2.506,17.958 5.066,15.397 4.124,13.12 l -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 20.347 4.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622L 7.46 17.788 4.897 20.35 2.506 17.958 5.066 15.397 4.124 13.12l-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 15.141,11.426 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.0511855,0 -3.7139999,-1.662815 -3.7139999,-3.714 0,-2.0511859 1.6628144,-3.7140003 3.7139999,-3.7140003 2.051186,0 3.714,1.6628144 3.714,3.7140003 z" d="m 15.141 11.426c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.7140-1.6628-3.7140-3.714 0-2.0512 1.6628-3.7140 3.7140-3.7140 2.0512 0 3.714 1.6628 3.714 3.7140z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path><path fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 26.347,10.895 -2.561,2.56 0.943,2.277 3.624,0 0,3.383 -3.622,0 -0.943,2.277 2.563,2.563 -2.393,2.392 -2.561,-2.561 -2.277,0.943 0,3.624 -3.383,0 0,-3.622 -2.277,-0.943 -2.563,2.562 -2.391,-2.392 2.56,-2.561 -0.942,-2.277 -3.624,0 0,-3.383 3.621,0 0.944,-2.276 -2.562,-2.563 2.392,-2.392 2.56,2.56 2.277,-0.941 0,-3.625 3.384,0 0,3.621 2.276,0.943 2.562,-2.562 z" d="m 26.347 10.895-2.561 2.56 0.943 2.277 3.624 0 0 3.383-3.622 0-0.943 2.277 2.563 2.563-2.393 2.392-2.561-2.561-2.277 0.943 0 3.624-3.383 0 0-3.622-2.277-0.943-2.563 2.562-2.391-2.392 2.56-2.561-0.942-2.277-3.624 0 0-3.383 3.621 0 0.944-2.276-2.562-2.563 2.392-2.392 2.56 2.56 2.277-0.941 0-3.625 3.384 0 0 3.621 2.276 0.943 2.562-2.562z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)" fill-rule="evenodd"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 21.141,17.426001 c 0,2.051185 -1.662814,3.714 -3.714,3.714 -2.051186,0 -3.714,-1.662815 -3.714,-3.714 0,-2.051186 1.662814,-3.714 3.714,-3.714 2.051186,0 3.714,1.662814 3.714,3.714 z" d="m 21.141 17.4260c 0 2.0512-1.6628 3.714-3.714 3.714-2.0512 0-3.714-1.6628-3.714-3.714 0-2.0512 1.6628-3.714 3.714-3.714 2.0512 0 3.714 1.6628 3.714 3.714z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.70000000,0.00000000,0.00000000,0.70000000,5.00000000,5.00000000)"></path></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,266.00000000,242.00000000)">Service Task</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="166.00000000 242.00000000 216.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M208 246 L 216 242 L208 238 Z" d="M 208 246L 216 242L 208 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,366.00000000,202.00000000)"><rect fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" width="100" height="80" ry="5" rx="5" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></rect><path fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="0.69999999" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 6.0095,22.5169 h 16.8581 v -5.4831 c 0,0 -1.6331,-2.7419 -4.9581,-3.6169 h -6.475 c -3.0919,0.9331 -5.4831,4.025 -5.4831,4.025 l 0.0581,5.075 z" d="m 6.0095 22.5169h 16.8581v-5.4831c 0 0-1.6331-2.7419-4.9581-3.6169h-6.475c-3.0919 0.9331-5.4831 4.025-5.4831 4.025l 0.0581 5.075z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="0.69999999" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 9.8,19.6 0,2.8" d="m 9.8 19.6 0 2.8" stroke-dasharray="none" dojoGfxStrokeStyle="solid"></path><path fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="0.69999999" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 19.6,19.6 0,2.8" d="m 19.6 19.6 0 2.8" stroke-dasharray="none" dojoGfxStrokeStyle="solid"></path><path fill="rgb(34, 34, 34)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1.5" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 18.419,5.9159999 c 0,2.9917264 -2.425274,5.4170001 -5.417,5.4170001 -2.991727,0 -5.417,-2.4252737 -5.417,-5.4170001 0,-2.9917264 2.425273,-5.41699983 5.417,-5.41699983 2.991726,0 5.417,2.42527343 5.417,5.41699983 z" d="m 18.419 5.9160c 0 2.9917-2.4253 5.4170-5.417 5.4170-2.9917 0-5.417-2.4253-5.417-5.4170 0-2.9917 2.4253-5.4170 5.417-5.4170 2.9917 0 5.417 2.4253 5.417 5.4170z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" transform="matrix(0.75000000,0.00000000,0.00000000,0.75000000,5.00000000,5.00000000)" fill-rule="evenodd"></path><path fill="rgb(240, 239, 240)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="0.69999999" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="m 11.2301,10.5581 c 0,0 1.9698,-1.6982 3.7632,-1.2649 1.7934,0.4333 3.2368,-0.4851 3.2368,-0.4851 0.175,1.1816 0.0294,2.625 -1.0206,3.9088 0,0 0.7581,0.525 0.7581,1.05 0,0.525 0.0875,1.3125 -0.7,2.1 -0.7875,0.7875 -3.85,0.875 -4.725,0 -0.875,-0.875 -0.875,-1.2831 -0.875,-1.8669 0,-0.5838 0.4081,-0.875 0.875,-1.3419 -0.7581,-0.4081 -1.7493,-1.6625 -1.3125,-2.1 z" d="m 11.2301 10.5581c 0 0 1.9698-1.6982 3.7632-1.2649 1.7934 0.4333 3.2368-0.4851 3.2368-0.4851 0.175 1.1816 0.0294 2.625-1.0206 3.9088 0 0 0.7581 0.525 0.7581 1.05 0 0.525 0.0875 1.3125-0.7 2.1-0.7875 0.7875-3.85 0.875-4.725 0-0.875-0.875-0.875-1.2831-0.875-1.8669 0-0.5838 0.4081-0.875 0.875-1.3419-0.7581-0.4081-1.7493-1.6625-1.3125-2.1z" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,416.00000000,242.00000000)">User Task</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="316.00000000 242.00000000 366.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M358 246 L 366 242 L358 238 Z" d="M 358 246L 366 242L 358 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g><g transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,517.50000000,225.50000000)"><circle fill="rgb(255, 255, 255)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="3" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" cx="15" cy="15" r="15" stroke-dasharray="none" dojoGfxStrokeStyle="Solid" fill-rule="evenodd"></circle></g><text fill="rgb(0, 0, 0)" fill-opacity="1" stroke="none" stroke-opacity="0" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" x="0" y="0" text-anchor="middle" text-decoration="none" rotate="0" kerning="auto" text-rendering="auto" font-style="normal" font-variant="normal" font-weight="normal" font-size="12" font-family="Arial" fill-rule="evenodd" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,535.50000000,276.50000000)">End Event</text><g><polyline fill="none" fill-opacity="0" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" points="466.00000000 242.00000000 516.00000000 242.00000000" stroke-dasharray="none" dojoGfxStrokeStyle="Solid"></polyline><g><path fill="rgb(0, 0, 0)" fill-opacity="1" stroke="rgb(34, 34, 34)" stroke-opacity="1" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="4" path="M508 246 L 516 242 L508 238 Z" d="M 508 246L 516 242L 508 238Z" transform="matrix(1.00000000,0.00000000,0.00000000,1.00000000,0.00000000,0.00000000)" stroke-dasharray="none" dojoGfxStrokeStyle="solid" fill-rule="evenodd"></path></g></g></g></svg><div class="bpmnElement" id="StartEvent_1" style="position: absolute; left: 130px; top: 224px; width: 36px; height: 36px;"></div><div class="bpmnElement" id="ServiceTask_1" style="position: absolute; left: 216px; top: 202px; width: 100px; height: 80px;"></div><div class="bpmnElement" id="UserTask_1" style="position: absolute; left: 366px; top: 202px; width: 100px; height: 80px;"></div><div class="bpmnElement" id="EndEvent_1" style="position: absolute; left: 516px; top: 224px; width: 36px; height: 36px;"></div>');
        expect(element.text()).toBe('Start EventService TaskUser TaskEnd Event');

      }));

      it('should display annotations on process diagram', inject(function($rootScope, $compile) {
        // given
        $rootScope.activityStatistics = [ {id: 'ServiceTask_1', count: 4} ];

        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" annotations="activityStatistics"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect($('#ServiceTask_1').html()).toBe('<div class="badge-position"><p class="badge">4</p></div>');
      }));

      it('should display incidents on process diagram', inject(function($rootScope, $compile) {
        // given
        $rootScope.incidents = [ {id: 'ServiceTask_1', incidents: [ {incidentType: 'failedJob', incidentCount: 18}, {incidentType: 'anotherIncidentType', incidentCount: 18} ]} ];

        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" incidents="incidents"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect($('#ServiceTask_1').html()).toBe('<div class="badge-position"><p class="badge badge-important">!</p></div>');
      }));

      it('should display annotations and incidents on process diagram', inject(function($rootScope, $compile) {
        // given
        $rootScope.activityStatistics = [ {id: 'ServiceTask_1', count: 4} ];
        $rootScope.incidents = [ {id: 'ServiceTask_1', incidents: [ {incidentType: 'failedJob', incidentCount: 18}, {incidentType: 'anotherIncidentType', incidentCount: 18} ]} ];

        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" annotations="activityStatistics" incidents="incidents"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect($('#ServiceTask_1').html()).toBe('<div class="badge-position"><p class="badge">4</p><p class="badge badge-important">!</p></div>');
      }));

      it('should not display (empty list of) incidents on process diagram ', inject(function($rootScope, $compile) {
        // given
        $rootScope.incidents = [ {id: 'ServiceTask_1', incidents: [ ]} ];

        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" incidents="incidents"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect($('#ServiceTask_1').html()).toBe('');
      }));

      it('should register click event on bpmn element', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1'];

        // when
        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');

        serviceTaskElement.click();

        $rootScope.$digest();

        // then
        expect($rootScope.selection.treeDiagramMapping.bpmnElements.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.bpmnElements[0].id).toBe('ServiceTask_1');
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight bpmn element', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');

        // when
        var bpmnElements = [ ];
        var model = $rootScope.semantic[0];
        for (var i = 0; i < model.baseElements.length; i++) {
          var baseElement = model.baseElements[i];
          if (baseElement.id === 'ServiceTask_1') {
            bpmnElements.push(baseElement);
            return;
          }
        }

        $rootScope.selection.treeDiagramMapping = {bpmnElements: bpmnElements};

        $rootScope.$digest();

        // then
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should deselect first former selected bpmn element and highlight another bpmn element', inject(function($rootScope, $compile, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1', 'UserTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');
        var userTaskElement = $('#UserTask_1');

        serviceTaskElement.click();

        $rootScope.$digest();

        // when
        var bpmnElements = [ ];
        var model = $rootScope.semantic[0];
        for (var i = 0; i < model.baseElements.length; i++) {
          var baseElement = model.baseElements[i];
          if (baseElement.id === 'UserTask_1') {
            bpmnElements.push(baseElement);
            return;
          }
        }

        $rootScope.selection.treeDiagramMapping = {bpmnElements: bpmnElements};

        $rootScope.$digest();

        // then
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(false);
        expect(userTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two bpmn elements', inject(function($rootScope, $compile, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1', 'UserTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');
        var userTaskElement = $('#UserTask_1');

        // when
        var bpmnElements = [ ];
        var model = $rootScope.semantic[0];
        for (var i = 0; i < model.baseElements.length; i++) {
          var baseElement = model.baseElements[i];
          if (baseElement.id === 'UserTask_1') {
            bpmnElements.push(baseElement);
          }
          if (baseElement.id === 'ServiceTask_1') {
            bpmnElements.push(baseElement);
          }
        }

        $rootScope.selection.treeDiagramMapping = {bpmnElements: bpmnElements};

        $rootScope.$digest();

        // then
        expect(userTaskElement.hasClass('activity-highlight')).toBe(true);
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two bpmn elements but the one of them was highlighted before', inject(function($rootScope, $compile, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1', 'UserTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');
        var userTaskElement = $('#UserTask_1');

        serviceTaskElement.click();

        $rootScope.$digest();

        // when
        var bpmnElements = [ ];
        var model = $rootScope.semantic[0];
        for (var i = 0; i < model.baseElements.length; i++) {
          var baseElement = model.baseElements[i];
          if (baseElement.id === 'UserTask_1') {
            bpmnElements.push(baseElement);
          }
          if (baseElement.id === 'ServiceTask_1') {
            bpmnElements.push(baseElement);
          }
        }

        $rootScope.selection.treeDiagramMapping = {bpmnElements: bpmnElements};

        $rootScope.$digest();

        // then
        expect(userTaskElement.hasClass('activity-highlight')).toBe(true);
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should not deselect bpmn element on second click', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1'];


        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');

        serviceTaskElement.click();

        $rootScope.$digest();

        // when (second click)
        serviceTaskElement.click();

        $rootScope.$digest();

        // then
        expect($rootScope.selection.treeDiagramMapping.bpmnElements.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.bpmnElements[0].id).toBe('ServiceTask_1');
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should deselect bpmn element on second click with ctrl key', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');

        serviceTaskElement.click();

        $rootScope.$digest();

        // when
        serviceTaskElement.trigger({ type: 'click', ctrlKey: true });

        $rootScope.$digest();

        // then
        expect($rootScope.selection.treeDiagramMapping.bpmnElements.length).toBe(0);
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(false);
      }));

      it('should select second bpmn elements', inject(function($rootScope, $compile, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.processDefinition = {id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726', key: 'FailingProcess'};
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1', 'UserTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');
        serviceTaskElement.click();

        $rootScope.$digest();

        // when
        var userTaskElement = $('#UserTask_1');
        userTaskElement.trigger({ type: 'click', ctrlKey: true });

        $rootScope.$digest();

        // then
        expect($rootScope.selection.treeDiagramMapping.bpmnElements.length).toBe(2);
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(true);
        expect(userTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

      it('should deselect second bpmn elements', inject(function($rootScope, $compile, Transform) {
        // given
        var xml = '<?xml version="1.0" encoding="UTF-8"?><bpmn2:definitions targetNamespace="http://activiti.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="_gLLjYNKaEeK06IvgDdgSXA"><bpmn2:process id="FailingProcess" isExecutable="false"><bpmn2:startEvent id="StartEvent_1" name="Start Event"><bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing></bpmn2:startEvent><bpmn2:serviceTask id="ServiceTask_1" name="Service Task"><bpmn2:incoming>SequenceFlow_1</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing></bpmn2:serviceTask><bpmn2:sequenceFlow id="SequenceFlow_1" sourceRef="StartEvent_1" targetRef="ServiceTask_1"/><bpmn2:userTask id="UserTask_1" name="User Task"><bpmn2:incoming>SequenceFlow_2</bpmn2:incoming><bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="SequenceFlow_2" sourceRef="ServiceTask_1" targetRef="UserTask_1"/><bpmn2:endEvent id="EndEvent_1" name="End Event"><bpmn2:incoming>SequenceFlow_3</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="SequenceFlow_3" sourceRef="UserTask_1" targetRef="EndEvent_1"/></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1"><bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds height="36.0" width="36.0" x="130.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape id="_BPMNShape_ServiceTask_2" bpmnElement="ServiceTask_1"><dc:Bounds height="80.0" width="100.0" x="216.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_1" bpmnElement="SequenceFlow_1" sourceElement="_BPMNShape_StartEvent_2" targetElement="_BPMNShape_ServiceTask_2"><di:waypoint xsi:type="dc:Point" x="166.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="216.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds height="80.0" width="100.0" x="366.0" y="202.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_2" bpmnElement="SequenceFlow_2" sourceElement="_BPMNShape_ServiceTask_2" targetElement="_BPMNShape_UserTask_2"><di:waypoint xsi:type="dc:Point" x="316.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="366.0" y="242.0"/></bpmndi:BPMNEdge><bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds height="36.0" width="36.0" x="516.0" y="224.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge id="BPMNEdge_SequenceFlow_3" bpmnElement="SequenceFlow_3" sourceElement="_BPMNShape_UserTask_2" targetElement="_BPMNShape_EndEvent_2"><di:waypoint xsi:type="dc:Point" x="466.0" y="242.0"/><di:waypoint xsi:type="dc:Point" x="516.0" y="242.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>';
        $rootScope.semantic = Transform.transformBpmn20Xml(xml);

        $rootScope.processDefinition = {id: 'FailingProcess:1:d91f75f6-d1cb-11e2-95b0-f0def1557726', key: 'FailingProcess'};
        $rootScope.selection = {};
        $rootScope.clickableElements = [ 'ServiceTask_1', 'UserTask_1'];

        element = createElement('<div process-diagram="semantic" process-definition="processDefinition" clickable-elements="clickableElements" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var serviceTaskElement = $('#ServiceTask_1');
        serviceTaskElement.click();

        $rootScope.$digest();

        var userTaskElement = $('#UserTask_1');
        userTaskElement.trigger({ type: 'click', ctrlKey: true });

        $rootScope.$digest();

        // when
        serviceTaskElement.trigger({ type: 'click', ctrlKey: true });

        $rootScope.$digest();

        // then
        expect($rootScope.selection.treeDiagramMapping.bpmnElements.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.bpmnElements[0].id).toBe('UserTask_1');
        expect(serviceTaskElement.hasClass('activity-highlight')).toBe(false);
        expect(userTaskElement.hasClass('activity-highlight')).toBe(true);
      }));

    });
  });
});
