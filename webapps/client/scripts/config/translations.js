define([
  'json!locales/en.json',
  'json!locales/de.json',
  'json!locales/fr.json'
], function(
  en,
  de,
  fr
) {
  'use strict';
  return [
    '$translateProvider',
  function(
    $translateProvider
  ) {

    // Simply register translation table as object hash
    $translateProvider
      .translations('en', en)
      .translations('de', de)
      .translations('fr', fr)

      .registerAvailableLanguageKeys([
        'en',
        'de',
        'fr'
      ])
      // .preferredLanguage('en')
      // using the determinePreferredLanguage()
      // would lead to use something like "en_US"
      .determinePreferredLanguage()
      .fallbackLanguage('en')
    ;
  }];
});
