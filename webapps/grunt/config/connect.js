module.exports = function(config) {
  config = config || {};

  return {
    options: {
      port: config.connectPort || 7070
    },
    dev: {
      options: {
        base: ['<%= buildTarget %>']
      }
    }
  };
};
