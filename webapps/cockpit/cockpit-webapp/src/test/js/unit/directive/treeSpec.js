define([ 'angular',
         'jquery',
         'cockpit/directives/tree',
         'angular-resource' ], function(angular, $) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    describe('tree directive', function() {
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
                                       'ngResource' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      beforeEach(inject(function($rootScope, $compile) {
        // given
        $rootScope.tree = {
          id: 'instance_1',
          label: 'Process',
          activityId: 'process',
          isOpen: true,
          children: [
            {
              id: 'instance_16',
              label: 'User Task 1',
              activityId: 'UserTask_1',
              children: []
            },
            {
              id: 'instance_2',
              label: 'SubProcess 1',
              activityId: 'SubProcess_1',
              isOpen: true,
              children: [
                {
                  id: 'instance_3',
                  label: 'Task 1',
                  activityId: 'Task_1',
                  children: []
                },
                {
                  id: 'instance_4',
                  label: 'Task 2',
                  activityId: 'Task_2',
                  children: []
                }
              ]
            },
            {
              id: 'instance_5',
              label: 'SubProcess 1',
              activityId: 'SubProcess_1',
              isOpen: true,
              children: [
                {
                  id: 'instance_6',
                  label: 'Task 1',
                  activityId: 'Task_1',
                  children: []
                },
                {
                  id: 'instance_7',
                  label: 'Task 2',
                  activityId: 'Task_2',
                  children: []
                }
              ]
            },
            {
              id: 'instance_8',
              label: 'ServiceTask 1',
              activityId: 'ServiceTask_1',
              children: []
            },
            {
              id: 'instance_9',
              label: 'SubProcess 2',
              activityId: 'SubProcess_2',
              isOpen: true,
              children: [
                {
                  id: 'instance_10',
                  label: 'Nested SubProcess',
                  activityId: 'SubProcess_3',
                  isOpen: true,
                  children: [
                    {
                      id: 'instance_11',
                      label: 'Task 3',
                      activityId: 'Task_3',
                      children: []
                    },
                    {
                      id: 'instance_12',
                      label: 'Task 3',
                      activityId: 'Task_3',
                      children: []
                    },
                    {
                      id: 'instance_13',
                      label: 'Task 3',
                      activityId: 'Task_3',
                      children: []
                    },                 
                    {
                      id: 'instance_14',
                      label: 'Task 3',
                      activityId: 'Task_3',
                      children: []
                    },
                    {
                      id: 'instance_15',
                      label: 'Task 3',
                      activityId: 'Task_3',
                      children: []
                    }                                        
                  ]
                }
              ]
            },
            {
              id: 'instance_17',
              label: 'A Task like this',
              activityId: 'UserTask_2',
              children: []
            }          
          ]
        };

      }));
         
      it('should render tree', inject(function($rootScope, $compile) {
        // when
        element = createElement('<div tree="tree"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();
        
        // then
        expect($('body').html()).toBe('<button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="process_instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Process</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="UserTask_2_instance_17" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">A Task like this</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="ServiceTask_1_instance_8" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">ServiceTask 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_1_instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 1</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_1_instance_3" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_2_instance_4" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 2</span></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_1_instance_5" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 1</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_1_instance_6" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_2_instance_7" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 2</span></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_2_instance_9" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 2</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_3_instance_10" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Nested SubProcess</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_11" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_12" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_13" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_14" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_15" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li></ul></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="UserTask_1_instance_16" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">User Task 1</span></li></ul>');
      }));

      it('should close tree', inject(function($rootScope, $compile) {
        // given
        element = createElement('<div tree="tree"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();
        
        // when
        $('button:eq(1)').click();

        // then
        expect($('body').html()).toBe('<button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-minus"></i>  </button><span id="process_instance_1" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Process</span><ul ng-show="tree.isOpen" class="ng-scope" style="display: none;"><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="UserTask_2_instance_17" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">A Task like this</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="ServiceTask_1_instance_8" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">ServiceTask 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_1_instance_2" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 1</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_1_instance_3" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_2_instance_4" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 2</span></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_1_instance_5" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 1</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_1_instance_6" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 1</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_2_instance_7" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 2</span></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_2_instance_9" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">SubProcess 2</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button ng-scope" style="display: none;">    <i class="icon-plus"></i>  </button>  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button ng-scope" style="">    <i class="icon-minus"></i>  </button><span id="SubProcess_3_instance_10" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Nested SubProcess</span><ul ng-show="tree.isOpen" class="ng-scope" style=""><!-- ngRepeat: item in tree.children | orderBy:\'label\' --><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_11" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_12" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_13" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_14" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="Task_3_instance_15" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">Task 3</span></li></ul></li></ul></li><li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style ng-scope"><span id="UserTask_1_instance_16" class="clickable-tree-node ng-scope ng-binding" ng-class="{\'activity-highlight\' : tree.isSelected}">User Task 1</span></li></ul>');
      }));

      it('should select one node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')
              
        // when
        node.click();

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect($rootScope.selection.treeToDiagramMap.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].id).toBe('instance_9');
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].activityId).toBe('SubProcess_2');
      }));

      it('should still be selected after a second click', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')
        
        node.click();

        // when
        node.click();

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect($rootScope.selection.treeToDiagramMap.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].id).toBe('instance_9');
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].activityId).toBe('SubProcess_2');
      }));

      it('should deselect after a second click with pressed ctrl key', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')
        
        node.click();

        // when
        node.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(false);
        expect($rootScope.selection.treeToDiagramMap.activityInstances.length).toBe(0);
      }));

      it('should select a second node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')
        
        node.click();

        // when
        var secondNode = $('#Task_2_instance_7')
        secondNode.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(true);
        expect(secondNode.hasClass('activity-highlight')).toBe(true);

        expect($rootScope.selection.treeToDiagramMap.activityInstances.length).toBe(2);
      }));

      it('should deselect first selection', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')       
        node.click();

        var secondNode = $('#Task_2_instance_7')
        secondNode.trigger({type: 'click', ctrlKey: true});

        // when
        node.trigger({type: 'click', ctrlKey: true});

        // then
        expect(node.hasClass('activity-highlight')).toBe(false);
        expect(secondNode.hasClass('activity-highlight')).toBe(true);

        expect($rootScope.selection.treeToDiagramMap.activityInstances.length).toBe(1);
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].id).toBe('instance_7');
        expect($rootScope.selection.treeToDiagramMap.activityInstances[0].activityId).toBe('Task_2');
      }));

      it('should highlight a node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        var nodeToHighlight = $rootScope.tree.children[1].children[1];

        $rootScope.selection.treeToDiagramMap = {activityInstances: [ nodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + nodeToHighlight.activityId + '_' + nodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two nodes', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        // when
        var firstNodeToHighlight = $rootScope.tree.children[1].children[1];
        var secondNodeToHighlight = $rootScope.tree.children[3];

        $rootScope.selection.treeToDiagramMap = {activityInstances: [ firstNodeToHighlight, secondNodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + firstNodeToHighlight.activityId + '_' + firstNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
        expect($('#' + secondNodeToHighlight.activityId + '_' + secondNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should deselect first selected node and highlight another node', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var node = $('#SubProcess_2_instance_9')       
        node.click();

        // when
        var nodeToHighlight = $rootScope.tree.children[1].children[1];

        $rootScope.selection.treeToDiagramMap = {activityInstances: [ nodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#SubProcess_2_instance_9').hasClass('activity-highlight')).toBe(false);        
        expect($('#' + nodeToHighlight.activityId + '_' + nodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

      it('should highlight two bpmn elements but the one of them was highlighted before', inject(function($rootScope, $compile) {
        // given
        $rootScope.selection = {};

        element = createElement('<div tree="tree" selection="selection"></div>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        var firstNodeToHighlight = $rootScope.tree.children[1].children[1];
        var secondNodeToHighlight = $rootScope.tree.children[3];

        $('#' + firstNodeToHighlight.activityId + '_' + firstNodeToHighlight.id).click();

        $rootScope.$digest();

        // when
        $rootScope.selection.treeToDiagramMap = {activityInstances: [ firstNodeToHighlight, secondNodeToHighlight ]};

        $rootScope.$digest();

        // then
        expect($('#' + firstNodeToHighlight.activityId + '_' + firstNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
        expect($('#' + secondNodeToHighlight.activityId + '_' + secondNodeToHighlight.id).hasClass('activity-highlight')).toBe(true);
      }));

    });
  });
});