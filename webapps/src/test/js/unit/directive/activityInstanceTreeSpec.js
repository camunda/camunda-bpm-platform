/* global define: false, describe: false, xdescribe: false, beforeEach: false, afterEach: false, module: false, inject: false, xit: false, it: false, expect: false */
/* jshint unused: false */
define([
  'angular',
  'jquery',
  'cockpit/directives/activityInstanceTree',
  'angular-resource'
], function(angular, $) {
  'use strict';

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    xdescribe('tree directive', function() {
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
                                       'ngResource' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      beforeEach(inject(function($rootScope, $compile) {
        // given
        $rootScope.tree = {
          id : 'instance_1',
          parentActivityInstanceId : null,
          activityId : 'Process_1',
          processInstanceId : 'instance_1',
          processDefinitionId : 'Process_1',
          childActivityInstances : [ {
            id : 'instance_2',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_1',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_3',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_3' ]
            }, {
              id : 'instance_4',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_2',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_4' ]
            } ],
            childTransitionInstances : [ {
              executionId: 'execution_1',
              id: 'transition_instance_1',
              parentActivityInstanceId: 'instance_2',
              processDefinitionId: 'Process_1',
              processInstanceId: 'instance_1',
              targetActivityId: 'ServiceTask_2'
            }],
            executionIds : [ 'instance_2' ]
          }, {
            id : 'instance_5',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_1',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_6',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_6' ]
            }, {
              id : 'instance_7',
              parentActivityInstanceId : 'instance_2',
              activityId : 'UserTask_2',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [],
              childTransitionInstances : [],
              executionIds : [ 'instance_1' ]
            } ],
            childTransitionInstances : [ {
              executionId: 'execution_2',
              id: 'transition_instance_2',
              parentActivityInstanceId: 'instance_5',
              processDefinitionId: 'Process_1',
              processInstanceId: 'instance_1',
              targetActivityId: 'ServiceTask_2'
            }],
            executionIds : [ 'instance_7' ]
          }, {
            id : 'instance_8',
            parentActivityInstanceId : 'instance_1',
            activityId : 'ServiceTask_1',
            processInstanceId : 'Process_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [],
            childTransitionInstances : [],
            executionIds : [ 'instance_8' ]
          }, {
            id : 'instance_9',
            parentActivityInstanceId : 'instance_1',
            activityId : 'SubProcess_2',
            processInstanceId : 'instance_1',
            processDefinitionId : 'Process_1',
            childActivityInstances : [ {
              id : 'instance_10',
              parentActivityInstanceId : 'instance_9',
              activityId : 'NestedSubProcess_1',
              processInstanceId : 'Process_1',
              processDefinitionId : 'Process_1',
              childActivityInstances : [ {
                id : 'instance_11',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_11' ]
              }, {
                id : 'instance_12',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_12' ]
              }, {
                id : 'instance_13',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_13' ]
              }, {
                id : 'instance_14',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_14' ]
              }, {
                id : 'instance_15',
                parentActivityInstanceId : 'instance_10',
                activityId : 'UserTask_3',
                processInstanceId : 'Process_1',
                processDefinitionId : 'Process_1',
                childActivityInstances : [],
                childTransitionInstances : [],
                executionIds : [ 'instance_15' ]
              } ],
              childTransitionInstances : [],
              executionIds : [ 'instance_10' ]
            } ],
            childTransitionInstances : [],
            executionIds : [ 'instance_9' ]
          } ],
          childTransitionInstances : [],
          executionIds : [ 'instance_1' ]
        };

      }));

      it('should render tree', inject(function($rootScope, $compile) {
        // when
        element = createElement('<div activity-instance-tree="tree"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // then
        expect($('body').html()).toBe('<button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_3" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_4" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="transition_instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_5" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_6" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_7" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="transition_instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_8" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_9" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_10" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_11" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_12" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_13" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_14" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_15" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li></ul></li></ul>');
      }));

      it('should close tree', inject(function($rootScope, $compile) {
        // given
        element = createElement('<div activity-instance-tree="tree"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        $('button:eq(1)').click();

        // then
        expect($('body').html()).toBe('<button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-minus"></i>  </button><span id="instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope" style="display: none;"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_3" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_4" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="transition_instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_5" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_6" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_7" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="transition_instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_8" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_9" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="invisible-button ng-scope">    <i class="icon-minus"></i>  </button><span id="instance_10" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span><ul ng-show="activityInstanceTree.isOpen" class="ng-scope"><!-- ngRepeat: item in getChildren() | orderBy:\'name\' --><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_11" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_12" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_13" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_14" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li><li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style ng-scope"><span id="instance_15" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}"></span></li></ul></li></ul></li></ul>');
      }));

      it('should select one node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');

        // when
        node.click();

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].id).toBe('instance_12');
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].activityId).toBe('UserTask_3');
      }));

      it('should still be selected after a second click', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');

        node.click();

        // when
        node.click();

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].id).toBe('instance_12');
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].activityId).toBe('UserTask_3');
      }));

      it('should deselect after a second click with pressed ctrl key', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');

        node.click();

        // when
        node.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(false);
        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(0);
      }));

      it('should select a second node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');

        node.click();

        // when
        var secondNode = $('#transition_instance_1');
        secondNode.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect(secondNode.hasClass('activity-highlight')).toBe(true);

        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(2);
      }));

      it('should deselect first selection', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');
        node.click();

        var secondNode = $('#transition_instance_1');
        secondNode.trigger({type: 'click', ctrlKey: true});

        // when
        node.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(false);
        expect(secondNode.hasClass('activity-highlight')).toBe(true);

        expect($rootScope.selection.treeDiagramMapping.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].id).toBe('transition_instance_1');
        expect($rootScope.selection.treeDiagramMapping.activityInstances[0].targetActivityId).toBe('ServiceTask_2');
      }));

      it('should highlight a node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        var nodeToHighlight = $rootScope.tree.childActivityInstances[1].childActivityInstances[1];

        $rootScope.selection.treeDiagramMapping = {activityInstances: [ nodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + nodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two nodes', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        var firstNodeToHighlight = $rootScope.tree.childActivityInstances[1].childActivityInstances[1];
        var secondNodeToHighlight = $rootScope.tree.childActivityInstances[3];

        $rootScope.selection.treeDiagramMapping = {activityInstances: [ firstNodeToHighlight, secondNodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + firstNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
        expect($('#' + secondNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should deselect first selected node and highlight another node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#instance_12');
        node.click();

        // when
        var nodeToHighlight = $rootScope.tree.childActivityInstances[1].childActivityInstances[1];

        $rootScope.selection.treeDiagramMapping = {activityInstances: [ nodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#instance_12').hasClass('activity-highlight')).toBe(false);
        expect($('#' + nodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two bpmn elements but the one of them was highlighted before', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div activity-instance-tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var firstNodeToHighlight = $rootScope.tree.childActivityInstances[1].childActivityInstances[1];
        var secondNodeToHighlight = $rootScope.tree.childActivityInstances[3];

        $('#' + firstNodeToHighlight.activityId + '_' + firstNodeToHighlight.id).click();

        $rootScope.$digest();

        // when
        $rootScope.selection.treeDiagramMapping = {activityInstances: [ firstNodeToHighlight, secondNodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + firstNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
        expect($('#' + secondNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

    });
  });
});
