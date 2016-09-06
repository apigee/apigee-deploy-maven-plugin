//******************************************************************************
// Acquire a TOTP token
//*********************
// CLI alternative to Google Authenticator or similar mobile apps.
// Helpful for use with CI bots and automation tools.
//
// INSTALL
// $ npm install totp-generator
//
// USAGE
// $ node mfatoken.js
// 
// NOTES
//   Ensuring OTP is valid for next minOTPValidity millisecs
//   helps in subsequent usage of OTP
//******************************************************************************
var totp = require('totp-generator');
var TOTP_WINDOW = 30000; // TOTP CONSTANT

/////////////// Config/Tweak /////////////////
var secret = 'OKYMN32T74TEERMX';

// usable OTP
var minOTPValidity = 10000; // ms
//////////////////////////////////////////////

function myotp(){
  var token = totp(secret);
  console.log(token);
}

var now = new Date().getTime();
var delta = now%TOTP_WINDOW;

if (delta < (TOTP_WINDOW - minOTPValidity)) {
  myotp();
} else {
  var waitTime = TOTP_WINDOW - delta;
  //console.log('Waiting for ' + waitTime + ' ms');
  setTimeout(function(){ myotp(); }, waitTime);
}
