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
  'OFL-1.1'
];

const DEV_LICENSES = [
  'CC-BY-3.0',
  'CC-BY-4.0',
  'ODC-By-1.0',
  'Unlicense',
  'Zlib'
];

const ALLOWED_PACKAGES = [
  'argparse@2.0.1',
  'caniuse-lite@1.0.30001458' // uses CC BY 4.0, permitted as of https://jira.camunda.com/browse/OB-26
];

const parseResults = (allowedLicenses, resolve, reject) =>
  function(err, packages) {
    if (err) {
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
          ? licenses.every(license => allowedLicenses.includes(license))
          : licenses.some(license => allowedLicenses.includes(license));

        if (!approved) {
          licenseWarning += `${id} uses ${licenses.join(' OR ')}\n`;
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
      production: true,
      excludePrivatePackages: true
    },
    parseResults(
      PRODUCTION_LICENSES,
      () => console.log('Production packages license check passed'),// eslint-disable-line
      warn => {
        console.warn('License check did not pass');// eslint-disable-line
        console.warn(warn);// eslint-disable-line
        process.exit(1);
      }
    )
  );
  checker.init(
    {
      start: path.resolve(__dirname, '..'),
      development: true,
      excludePrivatePackages: true
    },
    parseResults(
      [...PRODUCTION_LICENSES, ...DEV_LICENSES],
      () => console.log('Development packages license check passed'),// eslint-disable-line
      warn => {
        console.warn('License check did not pass');// eslint-disable-line
        console.warn(warn);// eslint-disable-line
        process.exit(1);
      }
    )
  );
}
