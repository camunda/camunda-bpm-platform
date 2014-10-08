define([], function() {
  'use strict';

  return [
    'camDateFormatProvider',
  function(
    camDateFormatProvider
  ) {
    camDateFormatProvider.setDateFormat('MMMM', 'monthName');
    camDateFormatProvider.setDateFormat('DD', 'day');
  }];
});
