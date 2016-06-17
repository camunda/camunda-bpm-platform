'use strict';

  /**
   * Interpolate a given dynamic name which contains markup. The result will be set
   * as the <code>name</code> attribute on the element.
   * @memberof cam.cockpit.directives
   * @name dynamicName
   * @type angular.Directive
   * @example
    <div ng-repeat="item in items">
      <input cam-dynamic-name="anElement{{ $index }}">
    </div>
    <!-- result -->
    <div>
      <input name="anElement0">
      <input name="anElement1">
      <input name="anElement2">
      ....
    </div>
   */
module.exports = [ '$interpolate', '$compile', function($interpolate, $compile) {

  return {
    restrict: 'A',
    priority: 9999,
    terminal: true, //Pause Compilation
    link: function(scope, element, attr) {
      element.attr('name', $interpolate(attr.camDynamicName)(scope));

        //Resume compilation at priority 9999 so that our directive doesn't get re-compiled
      $compile(element, null, 9999)(scope);
    }
  };
}];
