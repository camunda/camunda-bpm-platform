'use strict';

var defaultConfig = {
  'dateFormat': {
    'monthName': 'MMMM',
    'day': 'DD',
    'abbr': 'lll',
    'normal': 'LLL',
    'long': 'LLLL',
    'short': 'LL'
  },
  'locales': {
    'availableLocales': ['en'],
    'fallbackLocale': 'en'
  }
};

module.exports = function(config, app) {
  return [function() {
    this.getDateFormat = function(formatName) {
      var dateFormatObj = config.dateFormat || defaultConfig.dateFormat;
      return dateFormatObj[formatName] || defaultConfig.dateFormat[formatName];
    };

    this.getFallbackLocale = function() {
      if(config.locales && config.locales.fallbackLocale) {
        return config.locales.fallbackLocale;
      } else {
        return defaultConfig.locales.fallbackLocale;
      }
    };

    this.getAvailableLocales = function() {
      if(config.locales && config.locales.availableLocales) {
        return config.locales.availableLocales;
      } else {
        return defaultConfig.locales.availableLocales;
      }
    };

    this.getDateLocales = function() {
      return config.camDateLocales;
    };

    this.getAppVendor = function() {
      return config.app && config.app.vendor ? config.app.vendor : 'Camunda';
    };

    this.getAppName = function() {
      return config.app && config.app.name ? config.app.name : app;
    };

    this.$get = function() {
      return this;
    };
  }];
};
