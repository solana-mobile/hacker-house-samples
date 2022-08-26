/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solanamobile.mwaworkshop.usecase

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

// NOTE: this is just a minimal implementation of this Solana RPC call, for testing purposes. It is
// NOT suitable for production use.
object WaitForTransactionCommittedUseCase {
    suspend operator fun invoke(
        rpcUri: Uri,
        signature: ByteArray,
        commitment: Commitment = Commitment.FINALIZED
    ) {
        for (i in 0 until MAX_RETRIES) {
            val confirmation = getSignatureStatuses(rpcUri, signature)
            confirmation?.let {
                if (it.ordinal >= commitment.ordinal) {
                    return
                }
            }
            delay(RETRY_DELAY_MS)
        }
        throw WaitForTransactionCommittedFailedException("Transaction not confirmed within ${MAX_RETRIES * RETRY_DELAY_MS}ms, signature=$signature")
    }

    @Suppress("BlockingMethodInNonBlockingContext") // running in Dispatchers.IO
    private suspend fun getSignatureStatuses(
        rpcUri: Uri,
        signature: ByteArray
    ): Commitment? {
        return withContext(Dispatchers.IO) {
            val conn = URL(rpcUri.toString()).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.readTimeout = TIMEOUT_MS
            conn.connectTimeout = TIMEOUT_MS
            conn.doOutput = true
            conn.outputStream.use { outputStream ->
                outputStream.write(
                    createGetSignatureStatusesRequest(signature).encodeToByteArray()
                )
            }
            conn.connect()
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw WaitForTransactionCommittedFailedException("Connection failed, response code=${conn.responseCode}")
            }
            val commitment = conn.inputStream.use { inputStream ->
                val response = inputStream.readBytes().toString(StandardCharsets.UTF_8)
                parseGetSignatureStatusesResponse(response)
            }
            Log.d(TAG, "getSignatureStatuses signature=${Base58EncodeUseCase(signature)}, commitment=$commitment")
            commitment
        }
    }

    private fun createGetSignatureStatusesRequest(signature: ByteArray): String {
        val jo = JSONObject()
        jo.put("jsonrpc", "2.0")
        jo.put("id", 1)
        jo.put("method", "getSignatureStatuses")

        val arr = JSONArray()

        // Parameter 0 - base58-encoded public key
        val signatureArr = JSONArray()
        signatureArr.put(Base58EncodeUseCase(signature))
        arr.put(signatureArr)

        jo.put("params", arr)

        return jo.toString()
    }

    private fun parseGetSignatureStatusesResponse(response: String): Commitment? {
        val jo = JSONObject(response)
        val result = jo.optJSONObject("result")
            ?: throw WaitForTransactionCommittedFailedException("getSignatureStatuses request was not successful, response=$response")
        val value = result.getJSONArray("value")
        return value.optJSONObject(0)?.let {
            val confirmationStatus = it.getString("confirmationStatus")
            Commitment.fromCommitmentString(confirmationStatus)
        }
    }

    class WaitForTransactionCommittedFailedException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

    private val TAG = WaitForTransactionCommittedUseCase::class.simpleName
    private const val TIMEOUT_MS = 20000
    private const val MAX_RETRIES = 20
    private const val RETRY_DELAY_MS = 500L
}