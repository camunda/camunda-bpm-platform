/* global ngDefine: false */

ngDefine('cockpit.directives', [], function(module) {
  'use strict';
  /**
   * @name inPlaceTextField
   * @memberof cam.common.directives
   * @type angular.directive
   * @description Provides a widget for in place editing of a simple text variable.
   *
   * @example
   * <div cam-in-place-text-field context="someObject" property="keyOfTheObject" />
   */
  module.directive('camInPlaceTextField', [
  function() {
    /**
     * Callback used when initialization of the directive completes.
     * @param {angular.Scope} scope - the directive scope
     */
    function initialized(scope) {
      scope.value = scope.context[scope.property] || scope.defaultValue || null;

      scope.enter = function() {
        scope.editing = true;
        scope.value = scope.context[scope.property];
      };

      scope.submit = function() {
        var editForm = this;

        // it seems that, because this method
        // is called from the <form ng-submit="...">
        // "this" is having the "uncommited `value`"
        if (scope.context[scope.property] === editForm.value) {
          scope.leave();
          return;
        }

        // the value has change, so we do update the scope.context property
        scope.context[scope.property] = editForm.value;

        if (angular.isFunction(scope.$parent[scope.submitCallback])) {
          scope.$parent[scope.submitCallback](editForm);
        }
        scope.leave();
      };

      scope.leave = function() {
        scope.editing = false;
      };
    }

    return {
      restrict: 'E',

      scope: {
        // from context to form value
        unserializeCallback:  '@unserialize',
        // from form to context value
        serializeCallback:    '@serialize',

        initializeCallback:   '@initialize',
        enterCallback:        '@enter',
        validateCallback:     '@validate',
        submitCallback:       '@submit',
        successCallback:      '@success',
        errorCallback:        '@error',
        leaveCallback:        '@leave',
        context:              '=',
        property:             '@',
        defaultValue:         '@default'
      },

      templateUrl: require.toUrl('./app/common/directives/in-place-text-field.html'),

      link: function postLink(scope) {
        if (!scope.property) {
          throw new Error('You must specify a property of the context to be editable');
        }

        var initialize = scope.initializeCallback ? scope.$parent[scope.initializeCallback] : function(scope, cb) {cb()};

        initialize(scope, function(err) {
          initialized(scope);
        });
      }
    };
  }]);
});
