package fr.wavyapp.sumup

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

import com.sumup.merchant.reader.api.SumUpAPI
import com.sumup.merchant.reader.api.SumUpLogin
import com.sumup.merchant.reader.api.SumUpPayment
import com.sumup.merchant.reader.api.SumUpState
import com.sumup.merchant.reader.di.dagger.DaggerHandler
import com.sumup.merchant.reader.models.TransactionInfo

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class ReactNativeSumUpModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var sumUpLogin: SumUpLogin? = null
  override fun getName(): String {
    return "ReactNativeSumUp"
  }

  override fun initialize() {
    val handler = Handler(reactContext.mainLooper)
    handler.post {
      SumUpState.init(reactContext)
    }
    println("Module init, current activity $currentActivity")
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["SMPCurrencyCodeBGN"] = SMPCurrencyCodeBGN
    constants["SMPCurrencyCodeBRL"] = SMPCurrencyCodeBRL
    constants["SMPCurrencyCodeCHF"] = SMPCurrencyCodeCHF
    constants["SMPCurrencyCodeCLP"] = SMPCurrencyCodeCLP
    constants["SMPCurrencyCodeCZK"] = SMPCurrencyCodeCZK
    constants["SMPCurrencyCodeDKK"] = SMPCurrencyCodeDKK
    constants["SMPCurrencyCodeEUR"] = SMPCurrencyCodeEUR
    constants["SMPCurrencyCodeGBP"] = SMPCurrencyCodeGBP
    constants["SMPCurrencyCodeHUF"] = SMPCurrencyCodeHUF
    constants["SMPCurrencyCodeNOK"] = SMPCurrencyCodeNOK
    constants["SMPCurrencyCodePLN"] = SMPCurrencyCodePLN
    constants["SMPCurrencyCodeRON"] = SMPCurrencyCodeRON
    constants["SMPCurrencyCodeSEK"] = SMPCurrencyCodeSEK
    constants["SMPCurrencyCodeUSD"] = SMPCurrencyCodeUSD
    return constants
  }

  companion object {
    private const val REQUEST_CODE_LOGIN = 1
    private const val REQUEST_CODE_PAYMENT = 2
    private const val REQUEST_CODE_PAYMENT_SETTINGS = 3
    private const val TRANSACTION_SUCCESSFUL = 1
    private const val SMPCurrencyCodeBGN = "SMPCurrencyCodeBGN"
    private const val SMPCurrencyCodeBRL = "SMPCurrencyCodeBRL"
    private const val SMPCurrencyCodeCHF = "SMPCurrencyCodeCHF"
    private const val SMPCurrencyCodeCLP = "SMPCurrencyCodeCLP"
    private const val SMPCurrencyCodeCZK = "SMPCurrencyCodeCZK"
    private const val SMPCurrencyCodeDKK = "SMPCurrencyCodeDKK"
    private const val SMPCurrencyCodeEUR = "SMPCurrencyCodeEUR"
    private const val SMPCurrencyCodeGBP = "SMPCurrencyCodeGBP"
    private const val SMPCurrencyCodeHUF = "SMPCurrencyCodeHUF"
    private const val SMPCurrencyCodeNOK = "SMPCurrencyCodeNOK"
    private const val SMPCurrencyCodePLN = "SMPCurrencyCodePLN"
    private const val SMPCurrencyCodeRON = "SMPCurrencyCodeRON"
    private const val SMPCurrencyCodeSEK = "SMPCurrencyCodeSEK"
    private const val SMPCurrencyCodeUSD = "SMPCurrencyCodeUSD"
  }

  private fun settlePromiseOnActivityEvent(filterOnRequestCode: Int, promise: Promise?) {
    val activityEventListener = object: BaseActivityEventListener() {
      override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
      ) {
        println("Activity event listener, request code $requestCode, result code $resultCode");
        when (requestCode) {
          REQUEST_CODE_LOGIN and filterOnRequestCode -> data?.let {
            val extra = data.extras
            println("sum up login result ${extra!!.getInt(SumUpAPI.Response.RESULT_CODE)}")
            
            if (resultCode == SumUpAPI.Response.ResultCode.SUCCESSFUL) {
              // Deprecated but used anyway in the isLoggedIn method of SumUpAPI class
              val accessToken = DaggerHandler.sumUpHelperComponent.getIdentityModel().accessToken
              println("Access token from identity model $accessToken")
              accessToken?.let {
                securelyStoreAccessToken(it)
              }
              val map = Arguments.createMap()
              map.putBoolean("success", true)
              val currentMerchant = SumUpAPI.getCurrentMerchant()
              val userAdditionalInfo = Arguments.createMap()
              userAdditionalInfo.putString(
                "merchantCode",
                currentMerchant!!.merchantCode
              )
              userAdditionalInfo.putString(
                "currencyCode",
                currentMerchant.currency.isoCode
              )
              map.putMap("userAdditionalInfo", userAdditionalInfo)
              promise?.resolve(map)
            } else {
              deleteSumUpAccessToken()
              val errorDetails = Arguments.createMap()
              errorDetails.putInt("code", resultCode)
              errorDetails.putString("message", extra.getString(SumUpAPI.Response.MESSAGE))
              promise?.reject(Error("Login failure"), errorDetails)
            }
          }

          REQUEST_CODE_PAYMENT and filterOnRequestCode -> data?.let {
            val extra = data.extras
            if (resultCode == TRANSACTION_SUCCESSFUL) {
              val map = Arguments.createMap()
              map.putBoolean("success", true)
              map.putString(
                "transactionCode",
                extra?.getString(SumUpAPI.Response.TX_CODE)
              )

              val transactionInfo = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extra?.getParcelable(SumUpAPI.Response.TX_INFO, TransactionInfo::class.java)
              } else {
                @Suppress("DEPRECATION")
                extra?.getParcelable(SumUpAPI.Response.TX_INFO)
              }
              val additionalInfo = Arguments.createMap()
              additionalInfo.putString("cardType", transactionInfo!!.card.type)
              additionalInfo.putString(
                "cardLast4Digits",
                transactionInfo.card.last4Digits
              )
              transactionInfo.installments?.let {
                additionalInfo.putInt("installments", transactionInfo.installments)
              }
              map.putMap("additionalInfo", additionalInfo)
              promise?.resolve(map)
            } else {
              println("Transaction failure $resultCode ${extra?.getString(SumUpAPI.Response.MESSAGE)}")
              val errorDetails = Arguments.createMap()
              errorDetails.putInt("code", resultCode)
              errorDetails.putString("message", extra?.getString(SumUpAPI.Response.MESSAGE))
              promise?.reject(Error("Checkout failure"), errorDetails)
            }
          }

          REQUEST_CODE_PAYMENT_SETTINGS and filterOnRequestCode -> {
            val map = Arguments.createMap()
            map.putBoolean("success", true)
            promise?.resolve(map)
          }

          else -> {}
        }
        reactContext.removeActivityEventListener(this)
      }
    }
    reactContext.addActivityEventListener(activityEventListener)
  }

  private fun getEncryptedSharedPreferences(): SharedPreferences {
    val keyGenParamaterSpecs = KeyGenParameterSpec.Builder(
      "react-native-sum-up-master-key",
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build()
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParamaterSpecs)

    return EncryptedSharedPreferences.create(
      "react-native-sum-up-prefs",
      masterKeyAlias,
      reactContext,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  }

  private fun securelyStoreAccessToken(accessToken: String) {
    getEncryptedSharedPreferences().edit().putString("sum-up-access-token", accessToken).apply()
  }

  private fun retrieveSumUpAccessToken(): String? {
    return getEncryptedSharedPreferences().getString("sum-up-access-token", null)
  }

  private fun deleteSumUpAccessToken() {
    getEncryptedSharedPreferences().edit().remove("sum-up-access-token").apply()
  }

  @ReactMethod
  fun authenticate(affiliateKey: String, promise: Promise?) {
    settlePromiseOnActivityEvent(REQUEST_CODE_LOGIN, promise)
    val accessToken = retrieveSumUpAccessToken()
    val sumUpLoginBuilder = SumUpLogin.builder(affiliateKey)
    accessToken?.let {
      println("Previous token from encrypted shared preferences $accessToken")
      sumUpLoginBuilder.accessToken(accessToken)
    }
    sumUpLogin = sumUpLoginBuilder.build()
    SumUpAPI.openLoginActivity(currentActivity, sumUpLogin, REQUEST_CODE_LOGIN)
  }

  @ReactMethod
  fun prepareForCheckout(promise: Promise?) {
    if (!SumUpAPI.isLoggedIn()) {
      val errorDetails = Arguments.createMap()
      errorDetails.putInt("code", SumUpAPI.Response.ResultCode.ERROR_NOT_LOGGED_IN)
      promise?.reject(Error("Must login to SumUp before preparing checking out"), errorDetails)
      return
    }
    Log.d("react-native-sumup", "Prepare for checkout")

    reactContext.currentActivity?.runOnUiThread {
      SumUpAPI.prepareForCheckout()
      val map = Arguments.createMap()
      map.putBoolean("success", true)
      promise?.resolve(map)
    }
  }

  @ReactMethod
  fun logout(promise: Promise?) {
    Log.d("react-native-sumup", "Logout")
    SumUpAPI.logout()
    deleteSumUpAccessToken()
    val map = Arguments.createMap()
    map.putBoolean("success", true)
    promise?.resolve(map)
  }

  private fun getCurrency(currency: String?): SumUpPayment.Currency {
    return when (currency) {
      SMPCurrencyCodeBGN -> SumUpPayment.Currency.BGN
      SMPCurrencyCodeBRL -> SumUpPayment.Currency.BRL
      SMPCurrencyCodeCHF -> SumUpPayment.Currency.CHF
      SMPCurrencyCodeCLP -> SumUpPayment.Currency.CLP
      SMPCurrencyCodeCZK -> SumUpPayment.Currency.CZK
      SMPCurrencyCodeDKK -> SumUpPayment.Currency.DKK
      SMPCurrencyCodeEUR -> SumUpPayment.Currency.EUR
      SMPCurrencyCodeGBP -> SumUpPayment.Currency.GBP
      SMPCurrencyCodeHUF -> SumUpPayment.Currency.HUF
      SMPCurrencyCodeNOK -> SumUpPayment.Currency.NOK
      SMPCurrencyCodePLN -> SumUpPayment.Currency.PLN
      SMPCurrencyCodeRON -> SumUpPayment.Currency.RON
      SMPCurrencyCodeSEK -> SumUpPayment.Currency.SEK
      SMPCurrencyCodeUSD -> SumUpPayment.Currency.USD
      else -> SumUpPayment.Currency.USD
    }
  }

  @ReactMethod
  fun checkout(request: ReadableMap, promise: Promise?) {
    Log.d("react-native-sumup", "Checkout")

    try {
      val paymentBuilder = SumUpPayment.builder()
      paymentBuilder.total(
        BigDecimal(request.getString("totalAmount")).setScale(
          2,
          RoundingMode.HALF_EVEN
        )
      ).currency(
        getCurrency(request.getString("currencyCode"))
      ).foreignTransactionId(
        request.getString("foreignTransactionId").let {
          if (it === null || it.isEmpty()) {
            UUID.randomUUID().toString()
          } else {
            it
          }
        }
      )
      request.getString("title")?.let {
        paymentBuilder.title(it)
      }
      val payment = paymentBuilder
        .skipSuccessScreen()
        .build()
      println("Payment request of ${payment.total} ${payment.currency} (${payment.title}) foreign transaction id ${payment.foreignTransactionId}")
      settlePromiseOnActivityEvent(REQUEST_CODE_PAYMENT, promise)
      SumUpAPI.checkout(currentActivity, payment, REQUEST_CODE_PAYMENT)
    } catch (ex: Exception) {
      promise?.reject(ex)
    }
  }

  @ReactMethod
  fun preferences(promise: Promise?) {
    settlePromiseOnActivityEvent(REQUEST_CODE_PAYMENT_SETTINGS, promise)
    SumUpAPI.openCardReaderPage(currentActivity, REQUEST_CODE_PAYMENT_SETTINGS)
  }

  @ReactMethod
  fun isLoggedIn(promise: Promise) {
    promise.resolve(SumUpAPI.isLoggedIn())
  }

}
