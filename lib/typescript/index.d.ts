type AsyncActionResult = {
    success: boolean;
};
export type AuthenticationResult = AsyncActionResult & {
    userAdditionalInfo: {
        currencyCode: CurrencyCode;
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
export declare enum CurrencyCode {
    BGN,
    BRL,
    CHF,
    CLP,
    CZK,
    DKK,
    EUR,
    GBP,
    HUF,
    NOK,
    PLN,
    RON,
    SEK,
    USD
}
export declare enum ResultCodes {
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
    ERROR_INVALID_AMOUNT_DECIMALS = 12
}
export declare enum PaymentOptions {
    ANY,
    CARD_READER,
    MOBILE
}
export declare function authenticate(affiliateKey: string): Promise<AuthenticationResult>;
export declare function logout(): Promise<AsyncActionResult>;
export declare function prepareForCheckout(): Promise<AsyncActionResult>;
export declare function checkout(request: Record<string, string>): Promise<CheckoutResult>;
export declare function preferences(): Promise<AsyncActionResult>;
export declare function isLoggedIn(): Promise<Boolean>;
export {};
//# sourceMappingURL=index.d.ts.map