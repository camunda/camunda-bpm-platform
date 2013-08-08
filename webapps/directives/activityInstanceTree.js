'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var buttonTemplate =
  '  <button ng-show="!activityInstanceTree.isOpen" type="button" ng-click="open(activityInstanceTree)" class="btn-link">' + 
  '    <i class="icon-plus"></i>' +
  '  </button>' +  
  '  <button ng-show="activityInstanceTree.isOpen" type="button" ng-click="close(activityInstanceTree)" class="btn-link">' + 
  '    <i class="icon-minus"></i>' +
  '  </button>';
  
  var labelTemplate = '<span id="{{ activityInstanceTree.id }}" class="clickable-tree-node" ng-class="{\'activity-highlight\' : activityInstanceTree.isSelected}">{{ activityInstanceTree.name }}</span>';
  var childrenTemplate = '<ul ng-show="activityInstanceTree.isOpen">' + 
                            '<li ng-repeat="item in getChildren() | orderBy:\'name\'" class="none-list-style">' + 
                               '<div activity-instance-tree="item" selection="selection" on-element-click="propogateSelection(id, activityId, $event)"/>' +
                            '</li>' +
                         '</ul>';
  
  
  var Directive = function ($compile) {
    return {
      restrict: 'EAC',
      scope: {
        activityInstanceTree: '=',
        onElementClick: '&',
        selection: '='
      },
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch('activityInstanceTree', function (newValue) {
          if (!newValue || newValue.$loaded === false) {
            return;
          }
          createTree(newValue);
        });

        scope.$watch('selection.activityInstanceIds', function(newValue, oldValue) {
          if (!scope.activityInstanceTree) {
            return;
          }

          if (oldValue) {
            if (oldValue.indexOf(scope.activityInstanceTree.id) != -1) {
              scope.activityInstanceTree.isSelected = false;
            }
          }
          if (newValue) {
            if (newValue.indexOf(scope.activityInstanceTree.id) != -1) {
              scope.activityInstanceTree.isSelected = true;
            }
          }
        });
        
        var nodeHandler = function ($event) {
          $event.stopPropagation();

          var targetId = $($event.target).attr('id');

          if (targetId !== scope.activityInstanceTree.id) {
            return;
          }

          var ctrlKey = $event.ctrlKey,
              selectedNode = $event.data;

          scope.onElementClick({id: selectedNode.id, activityId: selectedNode.activityId || selectedNode.targetActivityId, $event: $event});
          scope.$apply();

        };

        function createTree (activityInstanceTree) {
          var template = labelTemplate;
          
          if (angular.isArray(activityInstanceTree.childActivityInstances) ||
              angular.isArray(activityInstanceTree.childTransitionInstances)) {
            if (activityInstanceTree.childActivityInstances.length > 0 ||
                activityInstanceTree.childTransitionInstances.length > 0 ) {
              template = buttonTemplate + template;
              template += childrenTemplate;

              // initially all nodes are open
              activityInstanceTree.isOpen = true;
            }

          }
          
          var newElement = angular.element(template);
          $compile(newElement)(scope);
          element.replaceWith(newElement);
          
          newElement.click(activityInstanceTree, nodeHandler);
          
        }

        scope.propogateSelection = function (id, activityId, $event) {
          scope.onElementClick({id: id, activityId: activityId, $event: $event});
        }
        
        scope.getChildren = function () {
          return scope.activityInstanceTree.childActivityInstances.concat(scope.activityInstanceTree.childTransitionInstances);
        }

        scope.open = function(node) {
          node.isOpen = true;
        };
        
        scope.close= function(node) {
          node.isOpen = false;
        };
        
      }
    };
  };
  
  module
    .directive('activityInstanceTree', Directive);
  
});
