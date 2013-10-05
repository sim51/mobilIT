'use strict';

/* App Module */
var app = angular.module('mobilIT', ['Services']);

app.config(function ($routeProvider) {
    $routeProvider
        .when('/', {templateUrl: 'partials/form.html', controller: FormCtrl})
        .when('/search', {templateUrl: 'partials/search.html', controller: SearchCtrl})
        .when('/detail', {templateUrl: 'partials/detail.html', controller: DetailCtrl})
        .when('/error', {templateUrl: 'partials/error.html', controller: ErrorCtrl})
        .otherwise({redirectTo: '/'});
});

app.run(function ($rootScope, $location) {
});