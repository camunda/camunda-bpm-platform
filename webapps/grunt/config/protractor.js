module.exports = function(config) {
  'use strict';

  return {
    e2e: {
      options: {
        configFile: config.protractorConfig,
        seleniumAddress: 'http://localhost:4444/wd/hub'
      }
    }
  };
};
