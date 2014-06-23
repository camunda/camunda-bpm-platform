module.exports = function() {
  return {
    options: {},
    assets: {
      files: [
        {
          expand: true,
          cwd: 'client',
          src: [
            '*.{ico,txt}',
            'index.html'
          ],
          dest: 'dist/'
        },
        {
          expand: true,
          cwd: 'client/fonts',
          src: ['*/*.{eot,svg,ttf,woff}'],
          dest: 'dist/fonts/'
        },
        {
          expand: true,
          cwd: 'client/bower_components/bootstrap/fonts',
          src: ['**'],
          dest: 'dist/fonts/bootstrap'
        },
        {
          expand: true,
          cwd: 'client/images',
          src: ['**'],
          dest: 'dist/images/'
        },
        // -----------------------
        {
          expand: true,
          cwd: 'client/scripts',
          src: ['**/*.{jpg,png,gif,webp}'],
          dest: 'dist/scripts/'
        }
      ]
    },
    sdk: {
      files: [
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk.js',
          dest: 'client/bower_components/camunda-bpm-sdk-js/index.js'
        },
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk-mock.js',
          dest: 'client/bower_components/camunda-bpm-sdk-js-mock/index.js'
        }
      ]
    }
  };
};
