module.exports = function() {
  return {
    options: {},
    assets: {
      files: [
        {
          expand: true,
          cwd: 'client',
          src: ['*.{html,ico,txt}'],
          dest: 'dist/'
        },
        {
          expand: true,
          cwd: 'client/fonts',
          src: ['**'],
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
          src: ['**/*.{html,jpg,png,gif,webp}'],
          dest: 'dist/scripts/'
        }
      ]
    }
  };
};
