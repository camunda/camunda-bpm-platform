module.exports = function(config) {
  config = config || {};

  var grunt = config.grunt;

  return {
    plugin: {
      src: [
        'target/webapp/plugin'
      ]
    }
  };
};
