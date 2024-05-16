"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.ResultCodes = exports.PaymentOptions = exports.CurrencyCode = void 0;
exports.authenticate = authenticate;
exports.checkout = checkout;
exports.isLoggedIn = isLoggedIn;
exports.logout = logout;
exports.preferences = preferences;
exports.prepareForCheckout = prepareForCheckout;
var _reactNative = require("react-native");
const LINKING_ERROR = `The package 'react-native-sum-up' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n';
const SumUp = _reactNative.NativeModules.ReactNativeSumUp ? _reactNative.NativeModules.ReactNativeSumUp : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
let CurrencyCode = exports.CurrencyCode = function (CurrencyCode) {
  CurrencyCode[CurrencyCode["BGN"] = SumUp.SMPCurrencyCodeBGN] = "BGN";
  CurrencyCode[CurrencyCode["BRL"] = SumUp.SMPCurrencyCodeBRL] = "BRL";
  CurrencyCode[CurrencyCode["CHF"] = SumUp.SMPCurrencyCodeCHF] = "CHF";
  CurrencyCode[CurrencyCode["CLP"] = SumUp.SMPCurrencyCodeCLP] = "CLP";
  CurrencyCode[CurrencyCode["CZK"] = SumUp.SMPCurrencyCodeCZK] = "CZK";
  CurrencyCode[CurrencyCode["DKK"] = SumUp.SMPCurrencyCodeDKK] = "DKK";
  CurrencyCode[CurrencyCode["EUR"] = SumUp.SMPCurrencyCodeEUR] = "EUR";
  CurrencyCode[CurrencyCode["GBP"] = SumUp.SMPCurrencyCodeGBP] = "GBP";
  CurrencyCode[CurrencyCode["HUF"] = SumUp.SMPCurrencyCodeHUF] = "HUF";
  CurrencyCode[CurrencyCode["NOK"] = SumUp.SMPCurrencyCodeNOK] = "NOK";
  CurrencyCode[CurrencyCode["PLN"] = SumUp.SMPCurrencyCodePLN] = "PLN";
  CurrencyCode[CurrencyCode["RON"] = SumUp.SMPCurrencyCodeRON] = "RON";
  CurrencyCode[CurrencyCode["SEK"] = SumUp.SMPCurrencyCodeSEK] = "SEK";
  CurrencyCode[CurrencyCode["USD"] = SumUp.SMPCurrencyCodeUSD] = "USD";
  return CurrencyCode;
}({});
let ResultCodes = exports.ResultCodes = /*#__PURE__*/function (ResultCodes) {
  ResultCodes[ResultCodes["SUCCESSFUL"] = 1] = "SUCCESSFUL";
  ResultCodes[ResultCodes["ERROR_TRANSACTION_FAILED"] = 2] = "ERROR_TRANSACTION_FAILED";
  ResultCodes[ResultCodes["ERROR_GEOLOCATION_REQUIRED"] = 3] = "ERROR_GEOLOCATION_REQUIRED";
  ResultCodes[ResultCodes["ERROR_INVALID_PARAM"] = 4] = "ERROR_INVALID_PARAM";
  ResultCodes[ResultCodes["ERROR_INVALID_TOKEN"] = 5] = "ERROR_INVALID_TOKEN";
  ResultCodes[ResultCodes["ERROR_NO_CONNECTIVITY"] = 6] = "ERROR_NO_CONNECTIVITY";
  ResultCodes[ResultCodes["ERROR_PERMISSION_DENIED"] = 7] = "ERROR_PERMISSION_DENIED";
  ResultCodes[ResultCodes["ERROR_NOT_LOGGED_IN"] = 8] = "ERROR_NOT_LOGGED_IN";
  ResultCodes[ResultCodes["ERROR_DUPLICATE_FOREIGN_TX_ID"] = 9] = "ERROR_DUPLICATE_FOREIGN_TX_ID";
  ResultCodes[ResultCodes["ERROR_INVALID_AFFILIATE_KEY"] = 10] = "ERROR_INVALID_AFFILIATE_KEY";
  ResultCodes[ResultCodes["ERROR_ALREADY_LOGGED_IN"] = 11] = "ERROR_ALREADY_LOGGED_IN";
  ResultCodes[ResultCodes["ERROR_INVALID_AMOUNT_DECIMALS"] = 12] = "ERROR_INVALID_AMOUNT_DECIMALS";
  return ResultCodes;
}({});
let PaymentOptions = exports.PaymentOptions = function (PaymentOptions) {
  PaymentOptions[PaymentOptions["ANY"] = SumUp.SMPPaymentOptionAny] = "ANY";
  PaymentOptions[PaymentOptions["CARD_READER"] = SumUp.SMPPaymentOptionCardReader] = "CARD_READER";
  PaymentOptions[PaymentOptions["MOBILE"] = SumUp.SMPPaymentOptionMobilePayment] = "MOBILE";
  return PaymentOptions;
}({});
async function authenticate(affiliateKey) {
  return SumUp.authenticate(affiliateKey);
}
async function logout() {
  return SumUp.logout();
}
async function prepareForCheckout() {
  return SumUp.prepareForCheckout();
}
async function checkout(request) {
  return SumUp.checkout(request);
}
async function preferences() {
  return SumUp.preferences();
}
async function isLoggedIn() {
  return SumUp.isLoggedIn();
}
//# sourceMappingURL=index.js.map