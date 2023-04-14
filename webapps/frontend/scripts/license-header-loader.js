/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var fs = require('fs');

const addMissingLicenseHeaders = (filePath, source) => {
  // This fix ensures windows compatibility
  // See https://github.com/camunda/camunda-bpm-platform/issues/2824
  const rowFile = filePath.replace(/\\/g, '/');
  if (
    rowFile &&
    !rowFile.endsWith('.json') &&
    !/@license|@preserve|@lic|@cc_on|^\/\**!/i.test(source)
  ) {
    let pkg = null;
    if (rowFile.includes('node_modules')) {
      pkg = rowFile.replace(
        /^(.*)node_modules\/(@[a-z-\d.]+\/[a-z-\d.]+)?([a-z-\d.]+)?(.*)$/,
        (match, p1, p2, p3) => p2 || p3
      );
    } else if (!rowFile.includes('camunda-bpm-sdk-js')) {
      pkg = rowFile.replace(
        /^(@[a-z-\d.]+\/[a-z-\d.]+)?([a-z-\d.]+)?(.*)$/,
        (match, p1, p2) => p2 || p1
      );
    }

    if (pkg) {
      let packagePath = `${process.cwd()}/node_modules/${pkg}`;
      if (!fs.existsSync(packagePath)) {
        packagePath = `${process.cwd()}/node_modules/camunda-bpm-webapp/node_modules/${pkg}`;
      }

      let licenseInfo = null;
      try {
        licenseInfo = fs.readFileSync(`${packagePath}/LICENSE`, 'utf8');
      } catch (e) {
        try {
          licenseInfo = fs.readFileSync(`${packagePath}/LICENSE.md`, 'utf8');
        } catch (e) {
          try {
            licenseInfo = fs.readFileSync(
              `${packagePath}/LICENSE-MIT.txt`,
              'utf8'
            );
          } catch (e) {
            try {
              licenseInfo = fs.readFileSync(
                `${packagePath}/LICENSE.txt`,
                'utf8'
              );
            } catch (e) {
              console.log(`${pkg} has no license file. 🤷‍`);// eslint-disable-line
            }
          }
        }
      }

      let packageJsonPath = require.resolve(`${packagePath}/package.json`);
      const {version, license} = require(packageJsonPath);
      if (licenseInfo) {
        return `/*!\n@license ${pkg}@${version}\n${licenseInfo}*/\n${source}`;
      } else if (license) {
        console.log(`${pkg} has a "license" property. 🤷‍`);// eslint-disable-line
        return `/*! @license ${pkg}@${version} (${license}) */\n${source}`;
      }
    }
  } else {
    return source;
  }
};

module.exports = function(source) {
  return addMissingLicenseHeaders(this.resourcePath, source);
};
