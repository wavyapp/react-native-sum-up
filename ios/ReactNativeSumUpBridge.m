//
//  ReactNativeSumUpBridge.m
//  ReactNativeSumUpBridge
//
//  Created by Sébastien Vray on 23/05/2024.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ReactNativeSumUp, RCTEventEmitter)

RCT_EXTERN_METHOD(
                  isLoggedIn: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  authenticate: (NSString) affiliateKey
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  preferences: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  prepareForCheckout: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  checkout: (id) request
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  logout: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

@end
