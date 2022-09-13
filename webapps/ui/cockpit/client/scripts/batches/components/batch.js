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

'use strict';

var events = require('./events');

var PAGE_SIZE = 10;

var Batch = function(camAPI, localConf, configuration) {
  this._sdk = camAPI;
  this._localConf = localConf;
  this.shouldLoadHistory = configuration.getBatchOperationAutoLoadEnded();

  this.sortingProperties = {
    runtime: 'batch-runtime-sort',
    history: 'batch-history-sort',
    job: 'batch-job-sort'
  };

  var runtimeSorting = this._loadLocal('runtime', {
    sortBy: 'batchId',
    sortOrder: 'asc'
  });
  var historySorting = this._loadLocal('history', {
    sortBy: 'startTime',
    sortOrder: 'desc'
  });

  this._batches = {
    runtime: {
      state: 'INITIAL',
      currentPage: 1,
      count: 0,
      data: null,
      users: {},
      sorting: runtimeSorting,
      query: {}
    },
    history: {
      state: 'INITIAL',
      currentPage: 1,
      count: 0,
      data: null,
      sorting: historySorting
    },
    selection: {
      state: 'INITIAL',
      type: null,
      data: {}
    }
  };

  var jobSorting = this._loadLocal('job', {sortBy: 'jobId', sortOrder: 'asc'});

  this._jobs = {
    state: 'INITIAL',
    currentPage: 1,
    count: 0,
    data: [],
    sorting: jobSorting
  };

  this.deleteModal = {
    instance: null
  };

  var self = this;
  events.on('delete:cancel', function() {
    self.deleteModal.instance && self.deleteModal.instance.dismiss('cancel');
    self.deleteModal.instance = null;
  });

  events.on('delete:confirm', function(params) {
    self._remove(params);
  });
};

Batch.prototype._loadLocal = function(type, defaultValue) {
  var localConf = this._localConf;
  return localConf && localConf.get(this.sortingProperties[type], defaultValue);
};

Batch.prototype._saveLocal = function(type, value) {
  var localConf = this._localConf;
  localConf && localConf.set(this.sortingProperties[type], value);
};

Batch.prototype.openDeleteModal = function() {
  events.emit('deleteModal:open', this.deleteModal);
};

Batch.prototype.onBatchSortChange = function(type, sorting) {
  this._batches[type].sorting = sorting;
  this._saveLocal(type, sorting);
  this._load(type);
};

Batch.prototype.onJobSortChange = function(sorting) {
  this._jobs.sorting = sorting;
  this._saveLocal('job', sorting);
  this._loadFailedJobs(this.getSelection());
};

Batch.prototype._remove = function(params) {
  var obj = this._batches.selection;
  params.id = obj.data.id;
  var self = this;

  let cb = err => {
    self.deleteModal.instance && self.deleteModal.instance.close();
    self.deleteModal.instance = null;

    if (err) {
      events.emit('batch:delete:failed', err);
    } else {
      events.emit('batch:delete:success');
      self.load();
      obj.state = 'INITIAL';
      obj.type = null;
      obj.data = {};
    }
  };

  if (obj.type === 'history') {
    return this._sdk.resource('history').batchDelete(obj.data.id, cb);
  } else {
    return this._sdk.resource('batch').delete(params, cb);
  }
};

var handleRetryResponse = function(context) {
  return function(err) {
    if (err) {
      events.emit('job:retry:failed', err);
    } else {
      events.emit('job:retry:success');
      context._load('runtime');
      context.loadDetails(context.getSelection().id, 'runtime');
    }
  };
};

Batch.prototype.retryAll = function() {
  return this._sdk.resource('job-definition').setRetries(
    {
      id: this._batches.selection.data.batchJobDefinitionId,
      retries: 1
    },
    handleRetryResponse(this)
  );
};

Batch.prototype.retryJob = function(job) {
  return this._sdk.resource('job').setRetries(
    {
      id: job.id,
      retries: 1
    },
    handleRetryResponse(this)
  );
};

Batch.prototype.deleteJob = function(job) {
  var self = this;
  return this._sdk.resource('job').delete(job.id, function(err) {
    if (err) {
      events.emit('job:delete:failed', err);
    } else {
      events.emit('job:delete:success');
      self._load('runtime');
      self.loadDetails(self.getSelection().id, 'runtime');
    }
  });
};

Batch.prototype.getProgressPercentage = function(batch, type) {
  switch (type) {
    case 'success':
      return (
        (100 * batch.completedJobs) /
        (batch.completedJobs + batch.remainingJobs)
      );
    case 'failed':
      return (
        (100 * batch.failedJobs) / (batch.completedJobs + batch.remainingJobs)
      );
    case 'remaining':
      return (
        (100 * (batch.remainingJobs - batch.failedJobs)) /
        (batch.completedJobs + batch.remainingJobs)
      );
  }
};

Batch.prototype.getProgressRoundedPercentage = function(batch, type) {
  return Math.round(this.getProgressPercentage(batch, type));
};

Batch.prototype.getProgressAbsolute = function(batch, type) {
  switch (type) {
    case 'success':
      return batch.completedJobs;
    case 'failed':
      return batch.failedJobs;
    case 'remaining':
      return batch.remainingJobs - batch.failedJobs;
  }
};

Batch.prototype.isSelected = function(batch) {
  return this._batches.selection.data.id === batch.id;
};

Batch.prototype.getJobs = function() {
  return this._jobs.data;
};

Batch.prototype.getStacktraceUrl = function(job) {
  return this._sdk.baseUrl + '/job/' + job.id + '/stacktrace';
};

Batch.prototype.getJobCount = function() {
  return this._jobs.count;
};

Batch.prototype.getLoadingState = function(type) {
  if (type === 'jobs') {
    return this._jobs.state;
  }
  return this._batches[type].state;
};

Batch.prototype.getBatches = function(type) {
  return this._batches[type].data;
};

Batch.prototype.getFullName = function(createUserId) {
  let user = this._batches.runtime.users[createUserId];
  return user ? user.firstName + ' ' + user.lastName : createUserId;
};

Batch.prototype.getSelection = function() {
  return this._batches.selection.data;
};

Batch.prototype.getSelectionType = function() {
  return this._batches.selection.type;
};

Batch.prototype.getBatchCount = function(type) {
  return this._batches[type].count;
};

Batch.prototype.getPageSize = function() {
  return PAGE_SIZE;
};

Batch.prototype.getCurrentPage = function(type) {
  if (type === 'jobs') {
    return this._jobs.currentPage;
  }
  return this._batches[type].currentPage;
};

Batch.prototype.getSuspendedState = function() {
  return this.getSelection().suspended;
};

Batch.prototype.toggleSuspension = function() {
  var self = this;
  var selection = this.getSelection();
  selection.state = 'LOADING';

  return this._sdk.resource('batch').suspended(
    {
      id: selection.id,
      suspended: !selection.suspended
    },
    function(err) {
      if (err) {
        throw err;
      } // notification?? but how?
      self.loadDetails(selection.id, 'runtime');
    }
  );
};

Batch.prototype.updatePage = function(type) {
  if (type === 'job') {
    return this._loadFailedJobs(this.getSelection());
  }
  this._load(type);
};

Batch.prototype.load = function() {
  this._load('runtime');

  if (this.shouldLoadHistory) {
    this._load('history');
  }
};

Batch.prototype.loadHistory = function() {
  this.shouldLoadHistory = true;
  this._load('history');
};

Batch.prototype.loadPeriodically = function(interval) {
  var self = this;
  this.load();
  this.intervalHandle = window.setInterval(function() {
    self.load();

    // also update the state of the currently selected batch
    if (
      self._batches.selection.state === 'LOADED' &&
      self._batches.selection.type === 'runtime' &&
      self._jobs.count === 0
    ) {
      self.loadDetails(self._batches.selection.data.id, 'runtime');
    }
  }, interval);
};

Batch.prototype.stopLoadingPeriodically = function() {
  window.clearInterval(this.intervalHandle);
};

Batch.prototype.loadDetails = function(id, type) {
  var obj = this._batches.selection;
  obj.state = 'LOADING';
  obj.type = type;
  var self = this;

  var cb = function(err, data) {
    var loadingFailed = function(errMsg) {
      events.emit('load:details:failed');

      obj.data = errMsg;
      obj.state = 'ERROR';
    };

    var loadingSuccessful = function() {
      events.emit('load:details:completed');

      obj.state = 'LOADED';

      if (type === 'runtime') {
        self._loadFailedJobs(obj.data);
      }
    };

    if (err || (typeof data.length !== 'undefined' && data.length === 0)) {
      // if the runtime version of the batch was requested,
      // try again with history (it may have finished in the meantime)
      if (type === 'runtime') {
        events.emit('details:switchToHistory');
        self.loadDetails(id, 'history');
      } else {
        loadingFailed(err.message);
      }
    } else {
      obj.data = data.length ? data[0] : data;

      var cb = function(err, data) {
        if (err) {
          if (err.status === 404) {
            obj.data.user = userId;
            loadingSuccessful();
          } else {
            loadingFailed(err.message);
          }
        } else {
          if (data.firstName && data.lastName) {
            obj.data.user = data.firstName + ' ' + data.lastName;
          } else {
            obj.data.user = userId;
          }

          loadingSuccessful();
        }
      };

      var userId = obj.data.createUserId;
      if (userId) {
        this._sdk.resource('user').profile({id: userId}, cb);
      } else {
        loadingSuccessful();
      }
    }
  }.bind(this);

  switch (type) {
    case 'runtime':
      this._sdk.resource('batch').statistics({batchId: id, maxResults: 1}, cb);
      break;
    case 'history':
      this._sdk.resource('history').singleBatch(id, cb);
      break;
  }
};

Batch.prototype._loadFailedJobs = function(data) {
  var jobId = data.batchJobDefinitionId;
  var obj = this._jobs;

  obj.state = 'LOADING';

  var params = {
    jobDefinitionId: jobId,
    withException: true,
    noRetriesLeft: true,
    firstResult: (obj.currentPage - 1) * PAGE_SIZE,
    maxResults: PAGE_SIZE,
    sorting: [this._jobs.sorting]
  };

  this._sdk.resource('job').list(
    params,
    function(err, data) {
      if (err) {
        obj.data = err.message;
        obj.state = 'ERROR';
      } else {
        obj.data = data;

        delete params.sorting;

        this._sdk.resource('job').count(params, function(err, data) {
          obj.state = data ? 'LOADED' : 'EMPTY';
          obj.count = data;
          events.emit('load:jobs:completed');
        });
      }
    }.bind(this)
  );
};

Batch.prototype._load = function(type) {
  var obj = this._batches[type];

  if (!obj.data) {
    obj.state = 'LOADING';
  }

  var params = {
    firstResult: (obj.currentPage - 1) * PAGE_SIZE,
    maxResults: PAGE_SIZE
  };

  var countCb = function(err, data) {
    obj.state = data.count ? 'LOADED' : 'EMPTY';
    obj.count = data.count;
    events.emit('load:' + type + ':completed');
  };
  var cb = function(err, data) {
    obj.data = data.items || data;

    if (type === 'runtime') {
      // fetch unique usernames
      let uniqueUserIds = new Set(data.map(item => item.createUserId));
      this._sdk.resource('user').list(
        {
          idIn: Array.from(uniqueUserIds).toString(),
          maxResults: uniqueUserIds.size
        },
        (err, data) => {
          obj.users = {};
          data.forEach(item => (obj.users[item.id] = item));
        }
      );
    }

    if (typeof data.count !== 'undefined') {
      countCb(err, data);
    } else {
      switch (type) {
        case 'runtime':
          return this._sdk.resource('batch').statisticsCount(params, countCb);
        case 'history':
          return this._sdk.resource('history').batchCount(params, countCb);
      }
    }
  }.bind(this);

  if (obj.sorting) {
    params.sortBy = obj.sorting.sortBy;
    params.sortOrder = obj.sorting.sortOrder;
  }

  if (obj.query) {
    params = {...params, ...obj.query};
  }

  switch (type) {
    case 'runtime':
      return this._sdk.resource('batch').statistics(params, cb);
    case 'history':
      params.completed = true;
      return this._sdk.resource('history').batch(params, cb);
  }
};

Batch.prototype.onBatchQueryChange = function(type, query) {
  if (JSON.stringify(query) === JSON.stringify(this._batches[type].query)) {
    return;
  }
  this._batches[type].query = query;
  this._load(type);
};

Batch.prototype.sortingKeys = [
  'id',
  'startTime',
  'executionStartTime',
  'endTime',
  'type',
  'user',
  'totalJobs',
  'completedJobs',
  'remainingJobs',
  'batchJobsPerSeed',
  'invocationsPerBatchJob',
  'tenantId',
  'batchJobDefinitionId',
  'monitorJobDefinitionId',
  'seedJobDefinitionId'
];

module.exports = Batch;
