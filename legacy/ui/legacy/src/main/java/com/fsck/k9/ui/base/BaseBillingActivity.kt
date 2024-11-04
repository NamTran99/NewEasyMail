package com.fsck.k9.ui.base

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import android.os.Bundle
import android.util.Log
import app.k9mail.core.android.common.Utils
import com.android.billingclient.api.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class BaseBillingActivity : K9Activity() {

    private lateinit var billingClient: BillingClient
    private val subscriptionId = "remove_ads_sub_monthly"

    abstract fun onPurchaseStateChange(isPurchased: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBillingClient()
    }


    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    purchases.forEach { purchase ->
                        launchRepeatOnResume {
                            handlePurchase(purchase)
                        }
                    }
                } else {
                    Log.e("BillingClient", "Error in purchase flow: ${billingResult.debugMessage}")
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        launchRepeatOnResume {
                            checkSubscriptionStatus()
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.e("BillingClient", "Service disconnected")
                }
            },
        )
    }

    private suspend fun checkSubscriptionStatus() {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        // Use a Pair to hold BillingResult and List<Purchase>
        val resultPair = suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { continuation ->
            billingClient.queryPurchasesAsync(queryPurchasesParams) { result: BillingResult, purchases: List<Purchase> ->
                continuation.resume(Pair(result, purchases))
            }
        }

        val (billingResult, purchases) = resultPair // Destructure the Pair

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Check if the user is subscribed
            val isSubscribed = purchases.any { it.products.contains(subscriptionId) && it.isAutoRenewing }

            if (isSubscribed) {
                Utils.setIsAppPurchased(true)
                onPurchaseStateChange(true)
                Log.i("SubscriptionStatus", "User is already subscribed to $subscriptionId")
            } else {
                Utils.setIsAppPurchased(false)
                onPurchaseStateChange(false)
                Log.i("SubscriptionStatus", "User is not subscribed to $subscriptionId")
            }
        } else {
            Log.e("SubscriptionStatus", "Error checking subscription: ${billingResult.debugMessage}")
        }
    }

    fun subscribeToMonthlyPlan() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subscriptionId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
        ) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()

                val billingResultLaunch = billingClient.launchBillingFlow(this, billingFlowParams)
                if (billingResultLaunch.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e("BillingFlow", "Error launching billing flow: ${billingResultLaunch.debugMessage}")
                }
            } else {
                Log.e("ProductDetails", "Error fetching product details: ${billingResult.debugMessage}")
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(subscriptionId)) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                Utils.setIsAppPurchased(true)
                onPurchaseStateChange(true)
                Log.i("Purchase", "Subscription purchased")
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.i("AcknowledgePurchase", "Purchase acknowledged")
                        } else {
                            Log.e("AcknowledgePurchase", "Failed to acknowledge purchase: ${result.debugMessage}")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }
}
