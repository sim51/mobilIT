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
    $scope.fromMarker = L.marker(new L.LatLng(0, 0));
    $scope.fromMarker.addTo($scope.map);

    // to marker
    $scope.toMarker = L.marker(new L.LatLng(0, 0));
    $scope.toMarker.addTo($scope.map);

    // center view
    $scope.map.setView([0, 0], 15);

    $scope.geojsonLayers = [];
    $scope.gesjsonDatas = [];
    $scope.displayMode = [];
    $scope.displayResultToolBar = false;

    var modeOfTransportList = ['car', 'cycle', 'pedestrian'];

    var modeOfTransportColors = {
        'car': '#FFA500',
        'cycle': '#00FFFF',
        'pedestrian': '#FF00FF'
    };

    $scope.displayMode = [];
    for (var x in modeOfTransportList) {
        $scope.displayMode[x] = false;
    }

    $scope.error = [];

    navigator.geolocation.getCurrentPosition(function (position) {
        $scope.map.removeLayer($scope.fromMarker);
        $scope.fromMarker = new L.marker(new L.LatLng(position.coords.latitude, position.coords.longitude));
        $scope.fromMarker.addTo($scope.map);
        $scope.map.setView([position.coords.latitude, position.coords.longitude], 15);
    });

    $scope.search = function () {
        $scope.displayResultToolBar = false;
        $scope.error = null;
        Nominatim.locate($scope.from).then(function (locationResp) {
            if (locationResp[0]) {
                $scope.map.removeLayer($scope.fromMarker);
                $scope.fromMarker = new L.marker(new L.LatLng(locationResp[0].lat, locationResp[0].lon));
                $scope.fromMarker.addTo($scope.map);
                $scope.map.setView([locationResp[0].lat, locationResp[0].lon], 15);

                Nominatim.locate($scope.to).then(function (locationResp) {
                    if (locationResp[0]) {
                        $scope.map.removeLayer($scope.toMarker);
                        $scope.toMarker = new L.marker(new L.LatLng(locationResp[0].lat, locationResp[0].lon));
                        $scope.toMarker.addTo($scope.map);
                        $scope.map.setView([locationResp[0].lat, locationResp[0].lon], 15);


                        for (var x in modeOfTransportList) {
                            $scope.searchForMode(modeOfTransportList[x]);
                        }
                        $scope.displayResultToolBar = true;
                    }
                    else {
                        $scope.error = "Adresse d'arrivée non trouvée";
                    }
                });
            }
            else {
                $scope.error = "Adresse de départ non trouvée";
            }
        });
    }

    $scope.searchForMode = function (mode) {
        Neo4j.search(mode,
                $scope.fromMarker.getLatLng().lat,
                $scope.fromMarker.getLatLng().lng,
                $scope.toMarker.getLatLng().lat,
                $scope.toMarker.getLatLng().lng
            ).then(function (response) {
                $scope.gesjsonDatas[mode] = response;
                $scope.undisplayLayer(mode);
                $scope.displayLayer(mode);

                if(!$scope.distance)
                    $scope.distance = [];
                var map = _.map(response.features, function (feature) { return  feature.properties.length });
                $scope.distance[mode] = Math.round10((_.reduce(map, function(memo, num){ return memo + num; }, 0) / 1000), -1);

                if(!$scope.time)
                    $scope.time = [];
                var mapTime = _.map(response.features, function (feature) { return  feature.properties.time });
                $scope.time[mode] = mapTime[0];
            });
    };


    $scope.undisplayLayer = function (aModeOfTransport) {
        console.log("action undisplay 2");
        if ($scope.geojsonLayers[aModeOfTransport]) {
            console.log("action undisplay 3");
            $scope.map.removeLayer($scope.geojsonLayers[aModeOfTransport]);
        }
        $scope.displayMode[aModeOfTransport] = false;
    };

    $scope.displayLayer = function (aModeOfTransport) {
        if($scope.gesjsonDatas[aModeOfTransport]) {
            $scope.undisplayLayer[aModeOfTransport];
 
            var geojsonLayerStyle = {
                'color': modeOfTransportColors[aModeOfTransport],
            };

            // geojson layer
            $scope.geojsonLayers[aModeOfTransport] = new L.GeoJSON(null, {
                style: geojsonLayerStyle,
                onEachFeature: function (feature, layer) {
                    if (feature.properties) {
                        var popupString = '<div class="popup">';
                        for (var k in feature.properties) {
                            var v = feature.properties[k];
                            popupString += k + ': ' + v + '<br />';
                        }
                        popupString += '</div>';
                        layer.bindPopup(popupString, {
                            maxHeight: 200
                        });
                    }
                }
            });
            $scope.map.addLayer($scope.geojsonLayers[aModeOfTransport]);
            $scope.geojsonLayers[aModeOfTransport].addData($scope.gesjsonDatas[aModeOfTransport]);
            $scope.map.fitBounds($scope.geojsonLayers[aModeOfTransport].getBounds());
            $scope.displayMode[aModeOfTransport] = true;
        }
    }; 

    $scope.changeDisplayMode = function (aModeOfTransport) {
        console.log("klkfdlk");
        if ($scope.displayMode[aModeOfTransport]) {
            console.log("action undisplay");
            $scope.undisplayLayer(aModeOfTransport);
        }
        else {
            console.log("action display");
            $scope.displayLayer(aModeOfTransport);
        }
    };
}

/*
 *  Error.
 */
function ErrorCtrl() {
}