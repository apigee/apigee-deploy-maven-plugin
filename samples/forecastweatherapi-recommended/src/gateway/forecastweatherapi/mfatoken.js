//******************************************************************************
// Acquire a TOTP token
//*********************
// CLI alternative to Google Authenticator or similar mobile apps.
// Helpful for use with CI bots and automation tools.
// IMPORTANT: Waits for a maximum of 25s to generate a usable OTP
//
// INSTALL
// $ npm install totp-generator
//
// USAGE
// $ node mfatoken.js
// 
// NOTES
// - TOTP is valid for 30 sec
// - Ensuring OTP is valid for next 25 secs helps in subsequent usage of OTP
//******************************************************************************

var totp = require('totp-generator');

function myotp(){
  var token = totp('OKYMN32T74TEERMX');
  console.log(token);
}

var now = new Date().getTime();
var delta = now%30000;
//console.log(delta);

// 25s is a good window to use otp
if (delta < 5000) {
  myotp();
} else {
  var waitTime = 30000-delta;
  //console.log('Waiting for ' + waitTime + ' ms');
  setTimeout(function(){ myotp(); }, waitTime);
}
