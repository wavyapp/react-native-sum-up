//
//  ReactNativeSumUp.swift
//  ReactNativeSumUp
//
//  Created by Sébastien Vray on 23/05/2024.
//

import SumUpSDK
import Security

@objc(ReactNativeSumUp)
class ReactNativeSumUp: RCTEventEmitter {
  private var isSDKSetup = false

  override init() {
    super.init()
  }

  private func retrieveSumUpAccessToken() async -> String? {
    return await Task {
      var token: CFTypeRef?
      let retrieveQuery: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: "sum-up-access-token",
        kSecAttrService as String: "SumUp",
        kSecAttrDescription as String: "SumUp access token",
        kSecAttrIsInvisible as String: false,
        kSecReturnData as String: true,
      ]
      let status = SecItemCopyMatching(retrieveQuery as CFDictionary, &token)

      if (status == errSecSuccess) {
        if let data = token as? Data {
          return String(data: data, encoding: .utf8)
        }
      } else {
        print("Failed to retrieve SumUp access token because \(SecCopyErrorMessageString(status, nil)!)")
      }

      return nil
    }.value
  }

  private func deleteSumUpAccessToken() async -> Bool {
    return await Task {
      let retrieveQuery: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: "sum-up-access-token",
        kSecAttrService as String: "SumUp",
        kSecAttrDescription as String: "SumUp access token",
        kSecAttrIsInvisible as String: false,
      ]
      let status = SecItemDelete(retrieveQuery as CFDictionary)

      if (status == errSecSuccess) {
        return true
      }
      print("Failed to retrieve SumUp access token because \(SecCopyErrorMessageString(status, nil)!)")
      return false
    }.value
  }

  private func storeSumUpAccessToken(_ token: String) async -> Bool {
    return await Task(priority: .background) {
      let storeQuery: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: "sum-up-access-token",
        kSecAttrService as String: "SumUp",
        kSecAttrDescription as String: "SumUp access token",
        kSecAttrIsInvisible as String: false,
        kSecValueData as String: token.data(using: .utf8) as Any,
      ]
      let status = SecItemAdd(storeQuery as CFDictionary, nil)

      if (status == errSecDuplicateItem) {
        let updateStatus = SecItemUpdate([
          kSecClass as String: kSecClassGenericPassword,
          kSecAttrAccount as String: "sum-up-access-token",
          kSecAttrService as String: "SumUp",
          kSecAttrDescription as String: "SumUp access token",
          kSecAttrIsInvisible as String: false,
        ] as CFDictionary, [ kSecValueData as String: token.data(using: .utf8) ] as CFDictionary)

        if (updateStatus != errSecSuccess) {
          print("Failed to store SumUp access stoken because \(SecCopyErrorMessageString(status, nil)!)")
        }

        return updateStatus == errSecSuccess
      }
      if (status != errSecSuccess) {
        print("Failed to store SumUp access stoken because \(SecCopyErrorMessageString(status, nil)!)")
      }

      return status == errSecSuccess
    }.value
  }

  private func resolveWithMechantDetails(_ currentMerchant: Merchant, _ resolve: RCTPromiseResolveBlock) {
    resolve([
      "success": true,
      "userAdditionalInfo": [
        "currencyCode": currentMerchant.currencyCode,
        "merchantCode": currentMerchant.merchantCode,
      ]
    ])
  }

  private func presentLoginModal(
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      if let presentedVc = RCTPresentedViewController() {
        SumUpSDK.presentLogin(from: presentedVc, animated: true) { (success, error) in
          Task {
            if let currentMerchant = SumUpSDK.currentMerchant, success {
              let cashierWebService = ISHCashierWebService.default()
              if let accessToken = cashierWebService?.accessToken() {
                _ = await self.storeSumUpAccessToken(accessToken)
              }
              self.resolveWithMechantDetails(currentMerchant, resolve)
            } else {
              reject("Login failure", error?.localizedDescription, error)
            }
          }
        }
      } else {
        reject("Login failure", "Could not retrieve the presented view controller", nil)
      }
    }
  }

  @objc
  func authenticate(
    _ affiliateKey: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {

    DispatchQueue.main.async {
      if (!self.isSDKSetup) {
        if (!SumUpSDK.setup(withAPIKey: affiliateKey)) {
          reject("Login failure", "Can't setup SumUp SDK", nil)
          return
        }
      }
      self.isSDKSetup = true

      Task {
        if let currentMerchant = SumUpSDK.currentMerchant {
          self.resolveWithMechantDetails(currentMerchant, resolve)
        } else {
          if let accessToken = await self.retrieveSumUpAccessToken() {
            SumUpSDK.login(withToken: accessToken) { (success: Bool, error: Error?) in
              if let currentMerchant = SumUpSDK.currentMerchant, success {
                self.resolveWithMechantDetails(currentMerchant, resolve)
              } else {
                switch (error as NSError?)?.code {
                    // SumUpSDKError.invalidAccessToken.rawValue is 54 while actual error code for invalid access token is 20...
                  case 20:
                    self.presentLoginModal(resolver: resolve, rejecter: reject)
                  default:
                    reject("Login failure", error?.localizedDescription, error)
                }
              }
            }
          } else {
            self.presentLoginModal(resolver: resolve, rejecter: reject)
          }
        }
      }
    }
  }

  @objc
  func isLoggedIn(
    _ resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) {
    resolve(SumUpSDK.isLoggedIn)
  }

  @objc
  func preferences(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      if let presentedVc = RCTPresentedViewController() {
        SumUpSDK.presentCheckoutPreferences(from: presentedVc, animated: true)
        resolve(["success": true])
      } else {
        reject("Failed to open preferences view", "Could not retrieve the presented view controller", nil)
      }
    }
  }

  @objc
  func prepareForCheckout(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    if (SumUpSDK.isLoggedIn) {
      SumUpSDK.prepareForCheckout()
      resolve(["success": true])
    } else {
      reject("Failed to prepare terminal for checkout", "User is not logged in", nil)
    }
  }

  @objc
  func checkout(
    _ request: [String:Any],
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      guard let currentMerchant = SumUpSDK.currentMerchant else {
        return reject("Failed to checkout", "User does not seem logged in", nil)
      }
      guard let total = request["totalAmount"] as? String else {
        return reject("Failed to checkout", "", nil)
      }
      guard let presentedVc = RCTPresentedViewController() else {
        return reject("Failed to checkout", "Could not retrieve the presented view controller", nil)
      }
      let checkoutRequest = CheckoutRequest(
        total: NSDecimalNumber(string: total),
        title: request["title"] as? String,
        currencyCode: request["currencyCode"] as? String ?? currentMerchant.currencyCode ?? CurrencyCodeEUR
      )
      checkoutRequest.foreignTransactionID = request["foreignTransactionId"] as? String ?? UUID().uuidString
      checkoutRequest.skipScreenOptions = .success

      SumUpSDK.checkout(with: checkoutRequest, from: presentedVc) { (result: CheckoutResult?, error: Error?) in
        guard error == nil else {
          return reject("Checkout failure", error?.localizedDescription ?? "Unknown error", nil)
        }
        guard result != nil else {
          return reject("Checkout failure", "No error, but no transaction result available either", nil)
        }

        if (result != nil && result!.success) {
          return resolve([
            "success": result?.success ?? false as Any,
            "transactionCode": result?.transactionCode as Any,
            "additionalInfo": result?.additionalInfo as Any,
          ])
        }
        if let additionalInfo = result?.additionalInfo {
          let jsonEncoder = JSONEncoder()
          do {
            let jsonAdditionalInfo = try jsonEncoder.encode([
              "status": additionalInfo["status"] as! String,
              "transactionCode": additionalInfo["transaction_code"] as! String,
              "entryMode": additionalInfo["entry_mode"] as! String,
            ])
            reject("Checkout failure", String(data: jsonAdditionalInfo, encoding: .utf8), nil)
          } catch {
            // Nothing to do, shouldn't happen
          }
        }
      }

    }
  }

  @objc
  func logout(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    Task {
      do {
        let sumUpLoggedOut = try await SumUpSDK.logout()
        let accessTokenDeleted = await self.deleteSumUpAccessToken()
        resolve(["success": sumUpLoggedOut && accessTokenDeleted])
      } catch {
        reject("Failed to log out", error.localizedDescription, error)
      }
    }
  }
}
