module.exports = function() {
  return {
    options: {
      paths: [
        'styles',
        'scripts'
      ],
      compress: true,
      sourceMap: true,
      sourceMapURL: '/styles/styles.css.map',
      sourceMapFilename: 'dist/styles/styles.css.map'
    },
    styles: {
      files: {
        'dist/styles/styles.css': 'client/styles/styles.less'
      }
    }
  };
};
