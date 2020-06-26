window['adrum-start-time'] = new Date().getTime();
(function(config){
    config.appKey = appKey;
    config.adrumExtUrlHttp = adrumExtUrlHttp;
    config.adrumExtUrlHttps = adrumExtUrlHttps;
    config.beaconUrlHttp = beaconUrlHttp;
    config.beaconUrlHttps = beaconUrlHttps;
    config.xd = {enable : false};
})(window['adrum-config'] || (window['adrum-config'] = {}));
