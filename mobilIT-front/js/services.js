'use strict';

angular.module('Services', [])
    .value('Config', {
        nominatimurl: 'http://nominatim.openstreetmap.org',
        neo4jurl: 'http://172.17.127.183:7474/mobilit',
        SUCCES: 'TRUE'
    })

    /* nominatim service */
    .factory('Nominatim', function ($http, $location, $rootScope, Config) {
        return{
            locate: function (location) {
                var url = Config.nominatimurl + '/search?q=' + location + '&format=json&json_callback=JSON_CALLBACK';
                return $http.jsonp(url)
                    .then(function (response) {
                        if (response.status == 200) {
                            return response.data;
                        } else {
                            $rootScope.error = response.data;
                            $location.path('/error');
                        }
                    });
            }
        }
    })

    /* Neo4j service */
    .factory('Neo4j', function ($http, $location, $rootScope, Config) {
        return{
            search: function (type, lat1, long1, lat2, long2) {
                var url = Config.neo4jurl + '/search/' + type + '?lat1=' + lat1 + '&long1=' + long1 + '&lat2=' + lat2 + '&long2=' +  long2 ;
                return $http.get(url)
                    .then(function (response) {
                        if (response.status == 200) {
                            return response.data;
                        } else {
                            $rootScope.error = response.data;
                            $location.path('/error');
                        }
                    });
            }
        }
    });