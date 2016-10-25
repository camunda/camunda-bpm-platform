'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-cockpit-source.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
require('camunda-commons-ui/vendor/prism');

module.exports = [
  '$window',
  function(
    $window
  ) {

    return {

      restrict: 'A',

      scope: {
        name: '=',
        source: '='
      },

      template: template,

      link: function($scope, $element) {

        var Prism = $window.Prism;

        var Extensions = {
          'js'         : 'javascript',
          'html'       : 'markup',
          'xml'        : 'markup',
          'py'         : 'python',
          'rb'         : 'ruby',
          'bpmn'       : 'markup',
          'cmmn'       : 'markup',
          'dmn'        : 'markup'
        };

        var name = $scope.name;

        $scope.extension = function() {
          if (name) {
            var extension = (name.match(/\.([\w-]+)$/) || ['', ''])[1];
            extension = extension && extension.toLowerCase();
            return Extensions[extension] || extension;
          }
        };

        $scope.$watch('source', function(source) {
          if (source) {
            var codeElement = angular.element('code', $element);
            Prism.highlightElement(codeElement[0]);
          }
        });
      }

    };

  }];
