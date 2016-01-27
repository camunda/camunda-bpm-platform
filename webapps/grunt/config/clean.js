module.exports = function() {
  'use strict';

  return {
    plugin: {
      src: [
        'target/webapp/plugin'
      ]
    },
    apps: {
      src: [
        'target/webapp/app',
        'target/webapp/lib'
      ]
    }
  };
};
