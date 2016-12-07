module.exports = function(config, localesConfig, pathConfig) {
  'use strict';

  localesConfig[pathConfig.appName + '_locales'] = {
      options: {
        dest: pathConfig.buildTarget + '/locales',
        onlyProd: 1,
        anOption: 'for production'
      },
      src: [
        '<%= pkg.gruntConfig.commonsUiDir %>/resources/locales/*.json',
        '<%= pkg.gruntConfig.commonsUiDir %>/lib/**/locales/*.json',
        pathConfig.sourceDir + '/scripts/**/locales/*.json'
      ]
  };
};
