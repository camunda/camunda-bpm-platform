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

        $(element).tooltip({ title: help, placement: p });
      }
    };
  };

  module.directive("help", Directive);

});
