  'use strict';

  module.exports = [
    '$tooltipProvider',
    function(
    $tooltipProvider
  ) {
      $tooltipProvider.options({
        appendToBody: true,
        popupDelay: 500
      });
    }];
