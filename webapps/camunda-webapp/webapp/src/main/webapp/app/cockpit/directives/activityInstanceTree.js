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
                               '<div activity-instance-tree="item" selection="selection" />' +
                            '</li>' +
                         '</ul>';
  
  
  var Directive = function ($location, $q, $compile) {
    return {
      restrict: 'EAC',
      scope: {
        activityInstanceTree: '=', 
        selection: '='
      },
      link: function(scope, element, attrs, processDiagram) {
        
        scope.$watch('activityInstanceTree', function (newValue) {
          if (newValue) {
            createTree(newValue);
          }
        });

        scope.$watch('selection.view.activityInstances', function(newValue, oldValue) {
          var activityInstanceIds = [];

          var searchParams = $location.search().activityInstances;
          if (searchParams && angular.isString(searchParams)) {
            activityInstanceIds = searchParams.split(',');
          } else if (searchParams && angular.isArray(searchParams)) {
            activityInstanceIds = angular.copy(searchParams);
          }

          if (!scope.activityInstanceTree) {
            return;
          }

          if (oldValue) {
            if (oldValue.indexOf(scope.activityInstanceTree) != -1) {
              scope.activityInstanceTree.isSelected = false;

              var index = activityInstanceIds.indexOf(scope.activityInstanceTree.id);
              if (index !== -1) {
                activityInstanceIds.splice(index, 1);
              }
            }
          }
          if (newValue) {
            if (newValue.indexOf(scope.activityInstanceTree) != -1) {
              scope.activityInstanceTree.isSelected = true;

              var index = activityInstanceIds.indexOf(scope.activityInstanceTree.id);
              if (index === -1) {
                activityInstanceIds.push(scope.activityInstanceTree.id);
              }
            }
          }

          if (activityInstanceIds.length === 0) {
            $location.search('activityInstances', null);
          } else {
            $location.search('activityInstances', activityInstanceIds);
          }
          $location.replace();
        });
        
        var nodeHandler = function ($event) {
          $event.stopPropagation();

          var targetId = $($event.target).attr('id');

          if (targetId !== scope.activityInstanceTree.id) {
            return;
          }

          if (!scope.selection) {
            return;
          }

          var selectedNode = $event.data;
          var instances = [];
          var scrollTo = selectedNode;
          
          if ($event.ctrlKey) {
            // if the 'ctrl' key has been pushed down, then select or deselect the clicked instance
            
            if (scope.selection.view && scope.selection.view.activityInstances) {
              
              var index = scope.selection.view.activityInstances.indexOf(selectedNode);
              
              if (index != -1) {
                // if the clicked instance is already selected then deselect it.
                angular.forEach(scope.selection.view.activityInstances, function (instance) {
                  if (instance.id != selectedNode.id) {
                    instances.push(instance);
                  }
                });
                scrollTo = null;
                
              } else if (index == -1) {
                // if the clicked instance is not already selected then select it together with other instances.
                angular.forEach(scope.selection.view.activityInstances, function (instance) {
                  instances.push(instance);
                });
                instances.push(selectedNode);
              }
              
            }
          } else {
            // else, push selected node to instances array
            instances.push(selectedNode);
          }
          
          scope.selection.view = {activityInstances: instances, scrollTo: scrollTo};
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
