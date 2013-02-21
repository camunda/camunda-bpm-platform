'use strict';

/* Services */

angular.module('cockpit.services', [
                                    'cockpit.service.debouncer',
                                    'cockpit.service.uri',
                                    'cockpit.service.request.status',
                                    'cockpit.service.cockpit.http.interceptor',
                                    'cockpit.service.http.utils',
                                    'cockpit.service.error'
                                    ]);