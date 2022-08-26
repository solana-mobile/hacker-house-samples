/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solanamobile.mwaworkshop.usecase

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

// NOTE: this is just a minimal implementation of this Solana RPC call, for testing purposes. It is
// NOT suitable for production use.
object GetBalanceUseCase {
    @Suppress("BlockingMethodInNonBlockingContext") // running in Dispatchers.IO
    suspend operator fun invoke(
        rpcUri: Uri,
        publicKey: ByteArray,
        commitment: Commitment = Commitment.FINALIZED
    ): Long {
        return withContext(Dispatchers.IO) {
            val conn = URL(rpcUri.toString()).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.readTimeout = TIMEOUT_MS
            conn.connectTimeout = TIMEOUT_MS
            conn.doOutput = true
            conn.outputStream.use { outputStream ->
                outputStream.write(
                    createGetBalanceRequest(
                        publicKey,
                        commitment
                    ).encodeToByteArray()
                )
            }
            conn.connect()
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw GetBalanceFailedException("Connection failed, response code=${conn.responseCode}")
            }
            val balance = conn.inputStream.use { inputStream ->
                val response = inputStream.readBytes().toString(StandardCharsets.UTF_8)
                parseGetBalanceResponse(response)
            }
            Log.d(TAG, "getBalance pubKey=${Base58EncodeUseCase(publicKey)}, balance=$balance")
            balance
        }
    }

    private fun createGetBalanceRequest(publicKey: ByteArray, commitment: Commitment): String {
        val jo = JSONObject()
        jo.put("jsonrpc", "2.0")
        jo.put("id", 1)
        jo.put("method", "getBalance")

        val arr = JSONArray()

        // Parameter 0 - base58-encoded public key
        arr.put(Base58EncodeUseCase(publicKey))

        // Parameter 1 - configuration object
        val config = JSONObject()
        config.put("commitment", commitment.commitment)
        arr.put(config)

        jo.put("params", arr)

        return jo.toString()
    }

    private fun parseGetBalanceResponse(response: String): Long {
        val jo = JSONObject(response)
        val result = jo.optJSONObject("result")
            ?: throw GetBalanceFailedException("getBalance request was not successful, response=$response")
        return result.getLong("value")
    }

    class GetBalanceFailedException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

    private val TAG = GetBalanceUseCase::class.simpleName
    private const val TIMEOUT_MS = 20000
}