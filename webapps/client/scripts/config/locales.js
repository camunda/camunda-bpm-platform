define(['moment'], function(moment) {
  'use strict';
  return [
    '$translateProvider',
    'configurationProvider',
  function(
    $translateProvider,
    configurationProvider
  ) {

    $translateProvider.useLoader('localeLoader', {
      prefix: angular.element('base').attr('app-root') + '/app/tasklist/locales/',
      suffix: '.json',
      callback: function(err, data) {
        if(!err && data && data.camDateLocales) {
          moment.lang(configurationProvider.getPreferredLocale() || 'en', data.camDateLocales);
        }
      }
    });
    $translateProvider.preferredLanguage(configurationProvider.getPreferredLocale());
  }];
});
