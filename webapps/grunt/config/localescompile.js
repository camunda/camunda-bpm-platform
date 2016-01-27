module.exports = function(config, localesConfig, pathConfig) {
  'use strict';

  localesConfig[pathConfig.appName + '_locales'] = {
      options: {
        dest: pathConfig.buildTarget + '/locales',
        onlyProd: 1,
        anOption: 'for production'
      },
      src: [
        'node_modules/camunda-commons-ui/resources/locales/*.json',
        'node_modules/camunda-commons-ui/lib/*/locales/*.json',
        pathConfig.sourceDir + '/scripts/**/locales/*.json'
      ]
  };
};
