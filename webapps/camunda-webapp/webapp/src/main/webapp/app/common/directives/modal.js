/* global ngDefine: false */

ngDefine('camunda.common.directives.modal', [ 'module:ui.bootstrap:angular-ui' ], function(module) {
  /**
   * @name modal
   * @memberof cam.common.directives
   * @type angular.directive
   * @description Provides a widget for modal windows
   * @example
      TODO
   */
  module.config([ '$dialogProvider', function($dialogProvider) {

    $dialogProvider.options({
      backdropClick: false,
      backdropFade: true
    });

  }]);
});
