module.exports = function(config, localesConfig, pathConfig) {
  'use strict';

  localesConfig[pathConfig.appName + '_locales'] = {
      options: {
        dest: pathConfig.buildTarget + '/locales',
        onlyProd: 1,
        anOption: 'for production'
      },
      src: [
        '<%= pkg.gruntConfig.enTranslationDir %>/' + pathConfig.appName + '/en.json'
      ]
  };
};
