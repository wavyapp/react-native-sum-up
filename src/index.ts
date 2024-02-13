import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-sum-up' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n';

const SumUp = NativeModules.ReactNativeSumUp
  ? NativeModules.ReactNativeSumUp
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

type AsyncActionResult = {
  success: boolean;
};

export type AuthenticationResult = AsyncActionResult & {
  userAdditionalInfo: {
    currencyCode: CurrencyCode,
    merchantCode: string;
  };
};

export type CheckoutResult = AsyncActionResult & {
  transactionCode: string;
  additionalInfo: {
    cardType: string;
    cardLast4Digits: string;
    installments: number;
  };
};

export enum CurrencyCode {
  BGN = SumUp.SMPCurrencyCodeBGN,
  BRL = SumUp.SMPCurrencyCodeBRL,
  CHF = SumUp.SMPCurrencyCodeCHF,
  CLP = SumUp.SMPCurrencyCodeCLP,
  CZK = SumUp.SMPCurrencyCodeCZK,
  DKK = SumUp.SMPCurrencyCodeDKK,
  EUR = SumUp.SMPCurrencyCodeEUR,
  GBP = SumUp.SMPCurrencyCodeGBP,
  HUF = SumUp.SMPCurrencyCodeHUF,
  NOK = SumUp.SMPCurrencyCodeNOK,
  PLN = SumUp.SMPCurrencyCodePLN,
  RON = SumUp.SMPCurrencyCodeRON,
  SEK = SumUp.SMPCurrencyCodeSEK,
  USD = SumUp.SMPCurrencyCodeUSD,
}

export enum ResultCodes {
  SUCCESSFUL = 1,
  ERROR_TRANSACTION_FAILED = 2,
  ERROR_GEOLOCATION_REQUIRED = 3,
  ERROR_INVALID_PARAM = 4,
  ERROR_INVALID_TOKEN = 5,
  ERROR_NO_CONNECTIVITY = 6,
  ERROR_PERMISSION_DENIED = 7,
  ERROR_NOT_LOGGED_IN = 8,
  ERROR_DUPLICATE_FOREIGN_TX_ID = 9,
  ERROR_INVALID_AFFILIATE_KEY = 10,
  ERROR_ALREADY_LOGGED_IN = 11,
  ERROR_INVALID_AMOUNT_DECIMALS = 12,
}

export enum PaymentOptions {
  ANY = SumUp.SMPPaymentOptionAny,
  CARD_READER = SumUp.SMPPaymentOptionCardReader,
  MOBILE = SumUp.SMPPaymentOptionMobilePayment,
}

export async function authenticate(affiliateKey: string): Promise<AuthenticationResult> {
  return SumUp.authenticate(affiliateKey);
}

export async function logout(): Promise<AsyncActionResult> {
  return SumUp.logout();
}

export async function prepareForCheckout(): Promise<AsyncActionResult> {
  return SumUp.prepareForCheckout();
}

export async function checkout(request: Record<string, string>): Promise<CheckoutResult> {
  return SumUp.checkout(request);
}

export async function preferences(): Promise<AsyncActionResult> {
  return SumUp.preferences();
}

export async function isLoggedIn(): Promise<Boolean> {
  return SumUp.isLoggedIn();
}
