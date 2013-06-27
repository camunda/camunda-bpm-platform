'use strict';

ngDefine('cockpit.directives', [ 'angular' ], function(module, angular) {
  
  var buttonTemplate =
  '  <button ng-show="!tree.isOpen" type="button" ng-click="open(tree)" class="invisible-button">' + 
  '    <i class="icon-plus"></i>' +
  '  </button>' +  
  '  <button ng-show="tree.isOpen" type="button" ng-click="close(tree)" class="invisible-button">' + 
  '    <i class="icon-minus"></i>' +
  '  </button>';
  
  var labelTemplate = '<span id="{{ tree.activityId + \'_\' + tree.id }}" class="clickable-tree-node" ng-class="{\'activity-highlight\' : tree.isSelected}">{{ tree.label }}</span>';
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

        scope.$watch('selection.treeToDiagramMap.activityInstances', function(newValue, oldValue) {
          if (oldValue) {
            if (oldValue.indexOf(scope.tree) != -1) {
              scope.tree.isSelected = false;
            }
          }
          if (newValue) {
            if (newValue.indexOf(scope.tree) != -1) {
              scope.tree.isSelected = true;
            }
          }          
        });
        
        var nodeHandler = function ($event) {
          var selectedNode = $event.data;
          var targetId = $($event.target).attr('id');
          
          if (targetId === selectedNode.activityId + '_' + selectedNode.id && scope.selection) {
            
            var instances = [];
            var scrollTo = selectedNode;
            
            if ($event.ctrlKey) {
              // if the 'ctrl' key has been pushed down, then select or deselect the clicked instance
              
              if (scope.selection.treeToDiagramMap && scope.selection.treeToDiagramMap.activityInstances) {
                
                var index = scope.selection.treeToDiagramMap.activityInstances.indexOf(selectedNode);
                
                if (index != -1) {
                  // if the clicked instance is already selected then deselect it.
                  angular.forEach(scope.selection.treeToDiagramMap.activityInstances, function (instance) {
                    if (instance.id != selectedNode.id) {
                      instances.push(instance);
                    }
                  });
                  scrollTo = null;
                  
                } else if (index == -1) {
                  // if the clicked instance is not already selected then select it together with other instances.
                  angular.forEach(scope.selection.treeToDiagramMap.activityInstances, function (instance) {
                    instances.push(instance);
                  });
                  instances.push(selectedNode);
                }
                
              }
            } else {
              // else, push selected node to instances array
              instances.push(selectedNode);
              
            }
            
            scope.selection.treeToDiagramMap = {activityInstances: instances, scrollTo: scrollTo};
            scope.$apply();
          }
          
        };
        
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
          
          newElement.click(tree, nodeHandler);
          
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
    .directive('tree', Directive);
  
});
