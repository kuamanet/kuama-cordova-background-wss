/**
 * Created by dan on 27/02/17.
 */
var exec = require('cordova/exec');

module.exports = {
    start: function (name, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'BackgroundServicePlugin', 'BackgroundServicePlugin.start', [name]);
    },
    setConfiguration: function(configuration, successCallback, failureCallback) {
        return exec(successCallback,
            failureCallback,
            'BackgroundServicePlugin',
            'BackgroundServicePlugin.setConfiguration',
            [configuration]);
    }
};
