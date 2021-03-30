var exec = require("cordova/exec");

exports.start = function (success, error) {
  exec(success, error, "cordova-plugin-r6000", "start", []);
};

exports.stop = function (success, error) {
  exec(success, error, "cordova-plugin-r6000", "stop", []);
};

exports.register = function (success, error) {
  exec(success, error, "cordova-plugin-r6000", "register", []);
};

exports.setOutputPower = function (power, success, error) {
  exec(success, error, "cordova-plugin-r6000", "setOutputPower", [power]);
};

exports.setWorkArea = function (area, success, error) {
  exec(success, error, "cordova-plugin-r6000", "setWorkArea", [area]);
};

exports.setPlaySound = function (playSound, success, error) {
  exec(success, error, "cordova-plugin-r6000", "setPlaySound", [playSound]);
};