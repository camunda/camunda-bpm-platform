define([], function() {
  'use strict';
  return [
    '$translateProvider',
    'configurationProvider',
  function(
    $translateProvider,
    configurationProvider
  ) {

    $translateProvider.useStaticFilesLoader({
      prefix: angular.element('base').attr('app-root') + '/app/tasklist/locales/',
      suffix: '.json'
    });
    $translateProvider.preferredLanguage(configurationProvider.getPreferredLanguage());
  }];
});
