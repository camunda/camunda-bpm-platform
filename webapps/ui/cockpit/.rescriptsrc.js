const {appendWebpackPlugin} = require('@rescripts/utilities')
const { LicenseWebpackPlugin } = require('license-webpack-plugin');

module.exports = appendWebpackPlugin(    
  new LicenseWebpackPlugin({
    outputFilename: '../dependencies.json',
    perChunkOutput: false,
    renderLicenses: (modules) => {
      const usedModules = modules.map((module) => {
        const {
          name,
          version
        } = module.packageJson;
        const id = `${name}@${version}`;

        return {
          id,
          ...module
        };
      }, {});

      return JSON.stringify(usedModules, null, 2);
    }
  })
);