define([
  'angular',
  'text!./cam-cockpit-source.html',
  'prismjs'
], function(
  angular,
  template
) {
  'use strict';

  return [
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

      link: function ($scope, $element) {

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
          var extension = (name.match(/\.(\w+)$/) || [,''])[1];
          extension = extension && extension.toLowerCase();
          return Extensions[extension] || extension;
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

});
