ngDefine('camunda.common.directives', [ 'jquery' ], function(module, $) {

  var Directive = function() {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var help = attrs.helpText || scope.$eval(attrs["helpTextVar"]);
        var p = "right";

        if (attrs.helpPlacement) {
          p = scope.$eval(attrs.helpPlacement);
        }

        var shown;

        function show() {
          var e = this;

          // optimization to work correctly with menues
          if ($(e).is('.open')) {
            return;
          }

          var shown = true;
          
          setTimeout(function() {
            if (shown) {
              $(e).tooltip('show');
            }
          }, 200);
        }

        function hide() {
          $(this).tooltip('hide');
          shown = false;
        }

        $(element)
          .tooltip({ title: help, placement: p, trigger: 'manual', container: 'body' })
          .click(hide)
          .hover(show, hide);
      }
    };
  };

  module.directive("help", Directive);

});
