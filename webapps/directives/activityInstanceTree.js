/* global ngDefine: false */
ngDefine('cockpit.directives', [ 'angular', 'require' ], function(module, angular, require) {
  'use strict';

  var TEMPLATE_URL = require.toUrl('./activity-instance-tree.html');

  var Directive = [ '$compile', '$http', '$templateCache',
            function ($compile, $http, $templateCache) {

    return {
      restrict: 'EAC',
      scope: {
        node: '=activityInstanceTree',
        onElementClick: '&',
        selection: '='
      },
      link: function(scope, element /*, attrs, processDiagram */ ) {

        var $nodeElement = element;

        function withTemplate(fn) {
          $http.get(TEMPLATE_URL, { cache: $templateCache })
            .success(function(content) {
              fn(content);
            }).error(function(response, code, headers, config) {
              throw new Error('Failed to load template: ' + config.url);
            });
        }

        scope.$watch('node', function (newValue) {
          if (!newValue || newValue.$loaded === false) {
            return;
          }

          newValue.children = (newValue.childActivityInstances || []).concat(newValue.childTransitionInstances || []);

          createTreeNode(newValue);
        });

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
          }
        });

        scope.deselect = function($event) {
          $event.ctrlKey = true;
          scope.select($event);
        };

        scope.select = function($event) {

          var node = scope.node;

          $event.stopPropagation();

          // var ctrlKey = $event.ctrlKey;

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

        scope.propogateSelection = function (id, activityId, $event) {
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

  module
    .directive('activityInstanceTree', Directive);

});
