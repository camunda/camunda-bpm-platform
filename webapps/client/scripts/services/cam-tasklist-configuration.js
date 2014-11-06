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
      "preferredLocale": "en"
    }
  };
  return [function() {
    this.getDateFormat = function(formatName) {
      var dateFormatObj = config.dateFormat || defaultConfig.dateFormat;
      return dateFormatObj[formatName] || defaultConfig.dateFormat[formatName];
    };

    this.getPreferredLocale = function() {
      if(config.locales && config.locales.preferredLocale) {
        return config.locales.preferredLocale;
      } else {
        return defaultConfig.locales.preferredLocale;
      }
    };

    this.getDateLocales = function() {
      return config.camDateLocales;
    };


    this.$get = function() {
      return this;
    };
  }];
});
