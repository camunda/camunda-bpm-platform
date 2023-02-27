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

const checker = require('license-checker');
const path = require('path');

const PRODUCTION_LICENSES = [
  '0BSD',
  'Apache 2.0',
  'Apache-2.0',
  'Apache-2.0 WITH LLVM-exception',
  'BSD',
  'BSD-2-Clause',
  'BSD-3-Clause',
  'CC0-1.0',
  'ISC',
  'MIT',
  'WTFPL',
  'SIL'
];

const ALLOWED_PACKAGES = [
  'extract-loader',
  'dmn-js',
  'bpmn-js',
  '@bpmn-io/form-js',
  'mousetrap',
  'cmmn-js',
  'inherits-browser',
  '@bpmn-io/form-js-playground',
  '@bpmn-io/form-js-editor',
  '@bpmn-io/form-js-viewer',
  'dmn-js-literal-expression',
  'dmn-js-drd',
  'dmn-js-decision-table',
  'dmn-js-shared',
  'inherits',
  'hat',
  'caniuse-lite@1.0.30001458'
];

const parseResults = (resolve, reject) =>
  function (err, packages) {
    if (err) {
      console.log(err);
      throw err;
    } else {
      const entries = Object.entries(packages);
      let licenseWarning = '';

      for (const [id, info] of entries) {
        if (ALLOWED_PACKAGES.includes(id)) {
          continue;
        }

        let licenses = info.licenses;
        let hasMultipleLicenses = false;

        if (typeof licenses === 'string') {
          licenses = licenses.replace(/[()*]/g, '');
          if (licenses.includes('AND')) {
            licenses = licenses.split(' AND ');
            hasMultipleLicenses = true;
          } else {
            licenses = licenses.split(' OR ');
          }
        }

        licenses = typeof licenses === 'object' ? licenses : [licenses];

        let approved = hasMultipleLicenses
          ? licenses.every((license) => PRODUCTION_LICENSES.includes(license))
          : licenses.some((license) => PRODUCTION_LICENSES.includes(license));

        if (!approved) {
          licenseWarning += `${id} uses ${licenses.join(' OR/AND ')}\n`;
        }
      }

      if (licenseWarning) {
        reject(licenseWarning);
      } else {
        resolve();
      }
    }
  };

/*
Main entry point for license check
 */
if (require.main === module) {
  checker.init(
    {
      start: path.resolve(__dirname, '..'),
      excludePrivatePackages: true,
      production: true,
    },
    parseResults(
      () => console.log('License check passed'),
      (warn) => {
        console.warn('License check did not pass');
        console.warn(warn);
        process.exit(1);
      }
    )
  );
}

module.exports = {
  allowedPackages: ALLOWED_PACKAGES,
};
