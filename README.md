# kuama-cordova-background-wss

## What it does
Registers a background /on boot service that will call each 25s (more or less) a ssl remote api, and then generate a notification that will launch the activity of your cordova app when clicked.

This plugin was developed for internal use, so if you want to include this in your project you will have to edit
the `Notificator` class, since it's expecting a json in a precise format to generate the notification.

## Dependencies
the plugin depends on 

- [FileChooser](http://github.com/don/cordova-filechooser.git) since your user should specify the path to the p12 for the ssl api call
- [cordova-plugin-filepath](https://github.com/hiddentao/cordova-plugin-filepath.git) to parse the path of the certificate file once it gets chosen
- [cordova-plugin-app-version](https://github.com/whiteoctober/cordova-plugin-app-version.git) to retrieve the package name of your application (is needed to bootstrap your application on notifications tap)
- [android.support.v4](https://github.com/floatinghotpot/cordova-plugin-android-support-v4.git) For the notifications to work in most of the android devices out there

## Supported platforms
- __Android__

## Usage

```javascript
var backgroundService = cordova.plugins.NotificationsFetcherService;
var configData = {};

//retrieve the certificate, probably on click of one of your buttons..
function showFilePicker (ev) {
    ev.preventDefault();
    //thanks to FileChooser
    fileChooser.open(setCertificatePath, handleCertificatePathError);
}

function setCertificatePath (uri) {
    //thanks to cordova-plugin-filepath
    window.FilePath.resolveNativePath(uri, function (absolutePath) {
        configData.pathToCertFile = absolutePath.replace('file:', '');
    }, handleCertificatePathError);
}

function handleCertificatePathError (error) {
    console.log(error);
}

//getPackageName thanks to cordova-plugin-app-version
cordova.getAppVersion.getPackageName(function (packagename) {
    configData.url = 'https://' + configData.backendUrl + 'services/notification';
    configData.mainPackageName = packagename;
    configData.mainClassName   = "MainActivity";
    configData.certFilePassword = configData.certPwd;
    configData.certFilePath = configData.pathToCertFile;
    backgroundService.setConfiguration(configData, function() {
        backgroundService.start();
    }, function() {
        console.error('could not start service');
    });
}); 

```