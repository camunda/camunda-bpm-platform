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

const TU1 = [
  {metric: 'task-users', sum: 3, subscriptionYear: 2024, subscriptionMonth: 8}
];

const TU2 = [
  {metric: 'task-users', sum: 3, subscriptionYear: 2024, subscriptionMonth: 4},
  {metric: 'task-users', sum: 3, subscriptionYear: 2023, subscriptionMonth: 1}
];

const sortedMockMonthly = [
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2023,
    subscriptionMonth: 11
  },
  {
    metric: 'process-instances',
    sum: 183,
    subscriptionYear: 2024,
    subscriptionMonth: 7
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2023,
    subscriptionMonth: 12
  },
  {
    metric: 'process-instances',
    sum: 69,
    subscriptionYear: 2024,
    subscriptionMonth: 6
  },
  {
    metric: 'process-instances',
    sum: 60,
    subscriptionYear: 2024,
    subscriptionMonth: 3
  },
  {
    metric: 'process-instances',
    sum: 1769,
    subscriptionYear: 2024,
    subscriptionMonth: 8
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 7
  },
  {
    metric: 'process-instances',
    sum: 48,
    subscriptionYear: 2023,
    subscriptionMonth: 9
  },
  {
    metric: 'process-instances',
    sum: 81,
    subscriptionYear: 2024,
    subscriptionMonth: 5
  },
  {
    metric: 'process-instances',
    sum: 1418,
    subscriptionYear: 2024,
    subscriptionMonth: 9
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 3
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 5
  },
  {
    metric: 'process-instances',
    sum: 58,
    subscriptionYear: 2023,
    subscriptionMonth: 12
  },
  {
    metric: 'process-instances',
    sum: 107,
    subscriptionYear: 2023,
    subscriptionMonth: 10
  },
  {
    metric: 'process-instances',
    sum: 107,
    subscriptionYear: 2023,
    subscriptionMonth: 11
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 6
  },
  {
    metric: 'process-instances',
    sum: 4,
    subscriptionYear: 2023,
    subscriptionMonth: 8
  },
  {
    metric: 'process-instances',
    sum: 1025,
    subscriptionYear: 2024,
    subscriptionMonth: 10
  },
  {
    metric: 'process-instances',
    sum: 114,
    subscriptionYear: 2024,
    subscriptionMonth: 1
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2023,
    subscriptionMonth: 9
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 8
  },
  {
    metric: 'process-instances',
    sum: 114,
    subscriptionYear: 2024,
    subscriptionMonth: 2
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 10
  },
  {
    metric: 'process-instances',
    sum: 70,
    subscriptionYear: 2024,
    subscriptionMonth: 4
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 9
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 4
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 2
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2024,
    subscriptionMonth: 1
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2023,
    subscriptionMonth: 8
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2023,
    subscriptionMonth: 10
  },
  {
    metric: 'decision-instances',
    sum: 0,
    subscriptionYear: 2022,
    subscriptionMonth: 10
  }
];

module.exports = {
  sortedMockMonthly,
  TU1,
  TU2
};
