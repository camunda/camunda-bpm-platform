define([], function() {
  'use strict';

  return [
    '$tooltipProvider',
  function(
    $tooltipProvider
  ) {
    $tooltipProvider.options({
      appendToBody: true,
      popupDelay: 500
    });
  }];
});
