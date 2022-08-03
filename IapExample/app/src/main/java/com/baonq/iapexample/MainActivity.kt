package com.baonq.iapexample

import android.icu.text.AlphabeticIndex
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val purchaseUpdatedListener =
            PurchasesUpdatedListener { billingResult, mutableList ->
                // To be implemented in a later section
            }

    private var billingClient = BillingClient.newBuilder(this)
        .setListener(purchaseUpdatedListener)
        .enablePendingPurchases()
        .build()

    val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("product_id_example")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setup()
    }

    private fun setup() {
        connectToGooglePLay()
        queryProducts()
    }

    private fun connectToGooglePLay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // GooglePlay by calling the startConnection() method.
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. you can query purchase here.
                }
            }
        })
    }

    private fun queryProducts() {
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
            billingResult,
            productDetailsList ->
            // check billingResult
            // process returned productDetailsList
        }
    }

    suspend fun processPurchases() {
        val productList = ArrayList<String>()
        productList.add("product_id_example")

        val params =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("product_id_example")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        )
                    ).setType(BillingClient.ProductType.SUBS)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        // Process the result.
    }
}
