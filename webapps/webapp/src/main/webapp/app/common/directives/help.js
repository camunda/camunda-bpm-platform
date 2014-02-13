/* global ngDefine: false */
ngDefine('camunda.common.directives.help', [ 'jquery' ], function(module, $) {
  'use strict';

  var Directive = function() {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var help = attrs.helpText || scope.$eval(attrs.helpTextVar);
        var p = 'right';

        if (attrs.helpPlacement) {
          p = scope.$eval(attrs.helpPlacement);
        }

        var shown;

        function show() {
          /*jshint validthis: true */
          var self = this;

          // optimization to work correctly with menues
          if ($(self).is('.open')) {
            return;
          }

          shown = true;

          setTimeout(function() {
            if (shown) {
              $(self).tooltip('show');
            }
          }, 200);
        }

        function hide() {
          shown = false;
          /*jshint validthis: true */
          $(this).tooltip('hide');
        }

        $(element)
          .tooltip({ title: help, placement: p, trigger: 'manual', container: 'body' })
          .click(hide)
          .hover(show, hide);
      }
    };
  };

  module.directive('help', Directive);

});
