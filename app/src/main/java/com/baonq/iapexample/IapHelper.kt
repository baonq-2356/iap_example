package com.baonq.iapexample

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

class IapHelper(context: Context) : PurchasesUpdatedListener, ProductDetailsResponseListener {

    private val billingClient =
            BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build()

    private val purchase = mutableListOf<Purchase>()
    private var productWithProductDetails = mapOf<String, ProductDetails>()

    /**
     * Establish connection to GooglePLay
     */
    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Billing service disconnected.
                Log.d(TAG, "Billing connection disconnected")

                // Restart the connection with startConnection() so future requests don't fail.
            }

            override fun onBillingSetupFinished(billingresult: BillingResult) {
                if (billingresult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing service connected.
                    Log.d(TAG, "Billing response OK")

                    // Query for existing user purchases
                    queryPurchases()

                    // Query for products for sale
                    queryProductDetails()
                } else {
                    Log.d(TAG, billingresult.debugMessage)
                }
            }
        })
    }

    /**
     * New purchases will be provided to PurchasesUpdatedListener
     * Can be handle response in onProductDetailsResponse() callback.
     */
    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in LIST_OF_PRODUCTS) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            params.setProductList(productList).let { productDetailsParams ->
                Log.i(TAG, "queryProductDetailsAsync")
                billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     */
    private fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.d(TAG, "queryPurchases: BillingClient is not ready")
        }
        // Query for existing subscription products that have been purchased.
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchase.apply {
                    clear()
                    addAll(purchaseList.ifEmpty { emptyList() })
                }
            } else {
                Log.d(TAG, billingResult.debugMessage)
            }
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (!billingClient.isReady) {
            Log.d(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        TODO("Not yet implemented")
    }

    override fun onProductDetailsResponse(
        billingresult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val responseCode = billingresult.responseCode
        val debugMessage = billingresult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val productDetailsListTemp = if (productDetailsList.isEmpty()) {
                    Log.d(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly " +
                                "published in the Google Play Console."
                    )
                    emptyMap()
                } else {
                    productDetailsList.associateBy { it.productId }
                }
                productWithProductDetails = productDetailsListTemp
            }
            else -> {
                Log.d(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    companion object {
        private const val TAG = "NQB"

        // List of subscription product offerings
        private const val BASIC_SUB = "up_basic_sub"
        private const val PREMIUM_SUB = "up_premium_sub"

        private val LIST_OF_PRODUCTS = listOf(BASIC_SUB, PREMIUM_SUB)
    }
}
