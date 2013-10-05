'use strict';

function FormCtrl($scope, Nominatim, Neo4j) {
    //construct the map
    $scope.map = new L.Map('map');
    var cloudmade = new L.TileLayer('http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/997/256/{z}/{x}/{y}.png', {
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>',
        maxZoom: 17
    });

    $scope.map.addLayer(cloudmade);

    // from marker
    $scope.fromMarker =  L.marker(new L.LatLng(0,0));
    $scope.fromMarker.addTo($scope.map);
    // to marker
    $scope.toMarker =  L.marker(new L.LatLng(0,0));
    $scope.toMarker.addTo($scope.map);

    $scope.map.setView([0, 0], 15);

    navigator.geolocation.getCurrentPosition(function(position) {
        $scope.map.removeLayer($scope.fromMarker);
        $scope.fromMarker =  new L.marker(new L.LatLng(position.coords.latitude, position.coords.longitude));
        $scope.fromMarker.addTo($scope.map);
        $scope.map.setView([position.coords.latitude, position.coords.longitude], 15);
    });

    $scope.locateFrom = function () {
        Nominatim.locate($scope.from).then(function(locationResp){
            if(locationResp[0]){
                $scope.map.removeLayer($scope.fromMarker);
                $scope.fromMarker =  new L.marker(new L.LatLng(locationResp[0].lat,locationResp[0].lon));
                $scope.fromMarker.addTo($scope.map);
                $scope.map.setView([locationResp[0].lat, locationResp[0].lon], 15);
            }
        });
    };

    $scope.locateTo = function () {
        Nominatim.locate($scope.to).then(function(locationResp){
            if(locationResp[0]){
                $scope.map.removeLayer($scope.toMarker);
                $scope.toMarker =  new L.marker(new L.LatLng(locationResp[0].lat,locationResp[0].lon));
                $scope.toMarker.addTo($scope.map);
                $scope.map.setView([locationResp[0].lat, locationResp[0].lon], 15);

            }
        });
    };

    $scope.search = function() {
        Neo4j.search(   'car',
                        $scope.fromMarker.getLatLng().lat,
                        $scope.fromMarker.getLatLng().lng,
                        $scope.toMarker.getLatLng().lat,
                        $scope.toMarker.getLatLng().lng
        ).then(function(response){
                L.geoJson(response).addTo($scope.map);
        });
    };

}

/*
 *  Error.
 */
function ErrorCtrl() {
}