/* global ngDefine: false */
ngDefine('camunda.common.directives.bootstrap', [ 'module:ui.bootstrap:angular-ui' ], function(module) {
  'use strict';

  module.config([
          '$modalProvider', '$tooltipProvider',
  function($modalProvider,   $tooltipProvider) {
    $modalProvider.options = {
      backdrop: true, //can be also false or 'static'
      keyboard: true
    };

    $tooltipProvider.options({
      animation: true,
      popupDelay: 100,
      appendToBody: true
    });
  }]);
});
