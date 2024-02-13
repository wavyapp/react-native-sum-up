package fr.wavyapp.sumup

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.ActivityEventListener
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
import com.sumup.merchant.reader.models.TransactionInfo
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class ReactNativeSumUpModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var mSumUpPromise: Promise? = null
  override fun getName(): String {
    return "ReactNativeSumUp"
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

  @ReactMethod
  fun authenticate(affiliateKey: String?, promise: Promise?) {
    mSumUpPromise = promise
    val sumupLogin = SumUpLogin.builder(affiliateKey!!).build()
    SumUpAPI.openLoginActivity(currentActivity, sumupLogin, REQUEST_CODE_LOGIN)
  }

  @ReactMethod
  fun authenticateWithToken(affiliateKey: String?, token: String?, promise: Promise?) {
    mSumUpPromise = promise
    val sumupLogin = SumUpLogin.builder(affiliateKey!!).accessToken(token).build()
    SumUpAPI.openLoginActivity(currentActivity, sumupLogin, REQUEST_CODE_LOGIN)
  }

  @ReactMethod
  fun prepareForCheckout(promise: Promise?) {
    mSumUpPromise = promise
    SumUpAPI.prepareForCheckout()
  }

  @ReactMethod
  fun logout(promise: Promise?) {
    mSumUpPromise = promise
    SumUpAPI.logout()
    mSumUpPromise!!.resolve(true)
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
    mSumUpPromise = promise
    try {
      var foreignTransactionId: String? = UUID.randomUUID().toString()
      if (request.getString("foreignTransactionId") != null) {
        foreignTransactionId = request.getString("foreignTransactionId")
      }
      val currencyCode = getCurrency(request.getString("currencyCode"))
      val payment = SumUpPayment.builder()
        .total(
          BigDecimal(request.getString("totalAmount")).setScale(
            2,
            RoundingMode.HALF_EVEN
          )
        )
        .currency(currencyCode)
        .title(request.getString("title")) // Wait for ios imp
        // .addAdditionalInfo("AccountId", "taxi0334")
        // .receiptEmail("customer@mail.com")
        // .receiptSMS("+3531234567890")
        .foreignTransactionId(foreignTransactionId)
        .skipSuccessScreen()
        .build()
      SumUpAPI.checkout(currentActivity, payment, REQUEST_CODE_PAYMENT)
    } catch (ex: Exception) {
      mSumUpPromise!!.reject(ex)
      mSumUpPromise = null
    }
  }

  @ReactMethod
  fun preferences(promise: Promise?) {
    mSumUpPromise = promise
    SumUpAPI.openCardReaderPage(currentActivity, REQUEST_CODE_PAYMENT_SETTINGS)
  }

  @ReactMethod
  fun isLoggedIn(promise: Promise) {
    val map = Arguments.createMap()
    map.putBoolean("isLoggedIn", SumUpAPI.isLoggedIn())
    promise.resolve(map)
  }

  private val mActivityEventListener: ActivityEventListener =
    object : BaseActivityEventListener() {
      override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
      ) {
        when (requestCode) {
          REQUEST_CODE_LOGIN -> if (data != null) {
            val extra = data.extras
            if (extra!!.getInt(SumUpAPI.Response.RESULT_CODE) == REQUEST_CODE_LOGIN) {
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
              mSumUpPromise!!.resolve(map)
            } else {
              mSumUpPromise!!.reject(
                extra.getString(SumUpAPI.Response.RESULT_CODE),
                extra.getString(SumUpAPI.Response.MESSAGE)
              )
            }
          }

          REQUEST_CODE_PAYMENT -> if (data != null) {
            val extra = data.extras
            if (mSumUpPromise != null) {
              if (extra!!.getInt(SumUpAPI.Response.RESULT_CODE) == TRANSACTION_SUCCESSFUL) {
                val map = Arguments.createMap()
                map.putBoolean("success", true)
                map.putString(
                  "transactionCode",
                  extra.getString(SumUpAPI.Response.TX_CODE)
                )
                val transactionInfo =
                  extra.getParcelable<TransactionInfo>(SumUpAPI.Response.TX_INFO)
                val additionalInfo = Arguments.createMap()
                additionalInfo.putString("cardType", transactionInfo!!.card.type)
                additionalInfo.putString(
                  "cardLast4Digits",
                  transactionInfo.card.last4Digits
                )
                additionalInfo.putInt("installments", transactionInfo.installments)
                map.putMap("additionalInfo", additionalInfo)
                mSumUpPromise!!.resolve(map)
              } else {
                mSumUpPromise!!.reject(
                  extra.getString(SumUpAPI.Response.RESULT_CODE),
                  extra.getString(SumUpAPI.Response.RESULT_CODE)
                )
              }
            }
          }

          REQUEST_CODE_PAYMENT_SETTINGS -> {
            val map = Arguments.createMap()
            map.putBoolean("success", true)
            mSumUpPromise!!.resolve(map)
          }

          else -> {}
        }
      }
    }

  init {
    reactContext.addActivityEventListener(mActivityEventListener)
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
}
