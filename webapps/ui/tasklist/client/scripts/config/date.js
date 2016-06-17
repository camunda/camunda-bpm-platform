  'use strict';
  module.exports = [
    'camDateFormatProvider',
    'configurationProvider',
    function(
    camDateFormatProvider,
    configurationProvider
  ) {
      var dateProperties = ['monthName', 'day', 'abbr', 'normal', 'long', 'short'];
      for(var i = 0; i < dateProperties.length; i++) {
        camDateFormatProvider.setDateFormat(configurationProvider.getDateFormat(dateProperties[i]), dateProperties[i]);
      }

    }];
