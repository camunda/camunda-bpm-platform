define(['angular'], function(angular) {
  'use strict';

  var config = window.camTasklistConf;
  var defaultConfig = {
    "dateFormat": {
      "monthName": "MMMM",
      "day": "DD",
      "abbr": "lll",
      "normal": "LLL",
      "long": "LLLL",
      "short": "LL"
    },
    "locales": {
      "availableLocales": ["en", "de", "fr"],
      "preferredLocal": "en"
    }
  };
  return [function() {
    this.getDateFormat = function(formatName) {
      var dateFormatObj = config.dateFormat || defaultConfig.dateFormat;
      return dateFormatObj[formatName] || defaultConfig.dateFormat[formatName];
    };

    this.getAvailableLanguages = function() {
      if(config.locales && config.locales.availableLocales) {
        return config.locales.availableLocales;
      } else {
        return defaultConfig.locales.availableLocales;
      }
    };

    this.getPreferredLanguage = function() {
      if(config.locales && config.locales.preferredLocal) {
        return config.locales.preferredLocal;
      } else {
        return defaultConfig.locales.preferredLocal;
      }
    };


    this.$get = function() {
      return this;
    };
  }];
});
