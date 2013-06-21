'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var buttonTemplate =
  '  <button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button">' + 
  '    <i class="icon-plus"></i>' +
  '  </button>' +  
  '  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button">' + 
  '    <i class="icon-minus"></i>' +
  '  </button>';
  
  var labelTemplate = '<span ng-click="selectNode(tree)" ng-class="{\'activity-highlight\' : tree.isSelected}">{{ tree.label }}</span>';
  var childrenTemplate = '<ul ng-show="tree.isOpen">' + 
                            '<li ng-repeat="item in tree.children | orderBy:\'label\'" class="none-list-style">' + 
                               '<div tree="item" selection="selection" />' +
                            '</li>' +
                         '</ul>';
  
  
  var Directive = function ($compile) {
    return {
      restrict: 'EAC',
      scope: {
        tree: '=', 
        selection: '='
      },
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch('tree', function (newValue) {
          if (newValue) {
            createTree(newValue);
          }
        });

        scope.$watch(function() { return scope.selection.selectedActivityInstances; }, function (newValue, oldValue) {
          if (oldValue) {
            deselectNode(oldValue);
          }
          if (newValue) {
            selectNode(newValue);
          }
        });
        
        function selectNode(selectedActivityInstances) {
          if (selectedActivityInstances.indexOf(scope.tree) != -1) {
            scope.tree.isSelected = true;
          }
        }

        function deselectNode(deselectedActivityInstances) {
          if (deselectedActivityInstances.indexOf(scope.tree) != -1) {
            scope.tree.isSelected = false;
          }
        }
        
        scope.$watch(function() { return scope.selection.selectedActivityIdsInTree; }, function (newValue, oldValue) {
          if (oldValue) {
            deselectNodeWithActivityId(oldValue);
          }
          if (newValue) {
            selectNodeWithActivityId(newValue);
          }
        });
        
        function selectNodeWithActivityId(selectedActivityIds) {
          if (selectedActivityIds.indexOf(scope.tree.activityId) != -1) {
            scope.tree.isSelected = true;
          }
        }

        function deselectNodeWithActivityId(deselectedActivityIds) {
          if (deselectedActivityIds.indexOf(scope.tree.activityId) != -1) {
            scope.tree.isSelected = false;
          }
        }
        
        function createTree (tree) {
          var template = labelTemplate;
          
          if (angular.isArray(tree.children)) {
            if (tree.children.length > 0) {
              template = buttonTemplate + template;
              template += childrenTemplate;
            }
          }
          
          var newElement = angular.element(template);
          $compile(newElement)(scope);
          element.replaceWith(newElement);
        }
        
        scope.open = function(node) {
          node.isOpen = true;
        };
        
        scope.close= function(node) {
          node.isOpen = false;
        };
        
        scope.selectNode = function (node) {
          scope.selection.selectedActivityInstances = [];
          scope.selection.selectedActivityInstances.push(node);
        };
      }
    };
  };
  
  module
    .directive('tree', Directive);
  
});
