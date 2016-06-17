'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/activity-instance-tree.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  // QUESTION: Shouldn't we use the templateUrl property instead?

function dashed(str) {
  return (str || '').replace(/([A-Z])/g, function($1) {
    return '-' + $1.toLowerCase();
  });
}

var iconNames = {
  'start-event':                            'start-event-none',
  'error-start-event':                      'start-event-error',
  'cancel-end-event':                       'end-event-cancel',
  'error-end-event':                        'end-event-error',
  'none-end-event':                         'end-event-none',
  'parallel-gateway':                       'gateway-parallel',
  'exclusive-gateway':                      'gateway-xor',
  'intermediate-compensation-throw-event':  'intermediate-event-throw-compensation'
};


var Directive = [
  '$compile',
  '$http',
  '$filter',
  function($compile, $http, $filter) {
    return {
      restrict: 'EAC',

      scope: {
        node: '=activityInstanceTree',
        onElementClick: '&',
        selection: '=',
        quickFilters: '=',
        orderChildrenBy: '&'
      },

      link: function(scope, element) {
        scope.symbolIconName = function(str) {
          var name = dashed(str);
          name = iconNames[name] ? iconNames[name] : name;
          return 'icon-'+ name;
        };

        var $nodeElement = element,
            nodeSelectedEventName = 'node.selected',
            nodeOpenedEventName = 'node.opened';

        var orderChildrenBy = scope.orderChildrenBy();

        function withTemplate(fn) {
          fn(template);
        }

        scope.$watch('node', function(newValue) {
          if (!newValue || newValue.$loaded === false) {
            return;
          }

          var children = (newValue.childActivityInstances || []).concat(newValue.childTransitionInstances || []);

          if (orderChildrenBy) {
            children = $filter('orderBy')(children, orderChildrenBy);
          }

          newValue.children = children;

          createTreeNode(newValue);
        });

        scope.$on(nodeOpenedEventName, function($event, value) {
          handleNodeEvents($event, value);
        });

        scope.$on(nodeSelectedEventName, function($event, value) {
          handleNodeEvents($event, value);
        });

        function handleNodeEvents($event, value) {
          var node = scope.node,
              eventName = $event.name;

          if (!node) {
            return;
          }

          if (eventName === nodeOpenedEventName || eventName === nodeSelectedEventName) {
            if (node.id === value.parentActivityInstanceId) {
              node.isOpen = true;
              if (node.parentActivityInstanceId && node.id !== node.parentActivityInstanceId) {
                fireNodeEvent(nodeOpenedEventName, node);
              }
            }
          }
        }

        function fireNodeEvent(name, node) {
          var id = node.id,
              parentActivityInstanceId = node.parentActivityInstanceId;

          scope.$emit(name, {
            id: id,
            parentActivityInstanceId: parentActivityInstanceId
          });
        }

        scope.$watch('selection.activityInstanceIds', function(newValue, oldValue) {
          var node = scope.node;

          if (!node) {
            return;
          }

          if (oldValue && oldValue.indexOf(node.id) != -1) {
            node.isSelected = false;
          }

          if (newValue && newValue.indexOf(node.id) != -1) {
            node.isSelected = true;

            if (node.parentActivityInstanceId) {
              fireNodeEvent(nodeSelectedEventName, node);
            }
          }
        });

        scope.deselect = function($event) {
          $event.ctrlKey = true;
          scope.select($event);
        };

        scope.select = function($event) {
          var node = scope.node;

          $event.stopPropagation();

          // propagate the change for other directives/controllers
          scope.$emit('instance-tree-selection-change');

          scope.onElementClick({
            id: node.id,
            activityId: node.activityId || node.targetActivityId,
            $event: $event
          });
        };

        function createTreeNode(node) {

          withTemplate(function(template) {
            // if finished, show collapsed
            node.isOpen = node.endTime ? false : true;

            var newElement = angular.element(template);
            $compile(newElement)(scope);
            $nodeElement.replaceWith(newElement);
            $nodeElement = newElement;
          });
        }

        scope.propogateSelection = function(id, activityId, $event) {
          scope.onElementClick({
            id: id,
            activityId: activityId,
            $event: $event
          });
        };

        scope.toggleOpen = function() {
          var node = scope.node;
          node.isOpen = !node.isOpen;
        };

      }
    };
  }];

module.exports = Directive;
