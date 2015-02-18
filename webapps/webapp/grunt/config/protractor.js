module.exports = function() {
  'use strict';

  return {
    e2e: {
      options: {
        configFile: 'src/test/js/e2e/develop.conf.js',
        seleniumAddress: 'http://localhost:4444/wd/hub'
      }
    }
  };
};
