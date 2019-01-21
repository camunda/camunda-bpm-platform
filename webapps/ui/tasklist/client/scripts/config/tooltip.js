  'use strict';

  module.exports = [
    '$uibTooltipProvider',
    function(
    $tooltipProvider
  ) {
      $tooltipProvider.options({
        appendToBody: true,
        popupDelay: 500
      });
    }];
