/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solanamobile.mwaworkshop.usecase

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

// NOTE: this is just a minimal implementation of this Solana instruction, for testing purposes. It
// is NOT suitable for production use.
object TransferLamportsUseCase {
    operator fun invoke(
        accountPublicKey: ByteArray,
        destinationPublicKey: ByteArray,
        latestBlockhash: ByteArray,
        lamports: Long
    ): ByteArray {
        assert(accountPublicKey.size == ACCOUNT_PUBLIC_KEY_LEN) { "Invalid account public key length for a Solana transaction" }
        assert(destinationPublicKey.size == DESTINATION_PUBLIC_KEY_LEN) { "Invalid destination public key length for a Solana transaction" }
        assert(latestBlockhash.size == BLOCKHASH_LEN) { "Invalid blockhash length for a Solana transaction" }

        val transaction = TRANSFER_SOL_USE_CASE.clone()
        System.arraycopy(accountPublicKey, 0, transaction, ACCOUNT_PUBLIC_KEY_OFFSET, ACCOUNT_PUBLIC_KEY_LEN)
        System.arraycopy(destinationPublicKey, 0, transaction, DESTINATION_PUBLIC_KEY_OFFSET, DESTINATION_PUBLIC_KEY_LEN)
        System.arraycopy(latestBlockhash, 0, transaction, BLOCKHASH_OFFSET, BLOCKHASH_LEN)
        val lamportsBuffer = ByteBuffer.wrap(transaction, TRANSFER_LAMPORTS_OFFSET, TRANSFER_LAMPORTS_LEN)
        lamportsBuffer.order(ByteOrder.LITTLE_ENDIAN)
        lamportsBuffer.putLong(lamports)
        Log.d(TAG, "Created transfer transaction for accountPublicKey(base58)=${Base58EncodeUseCase(accountPublicKey)}, " +
                "destinationPublicKey(base58)=${Base58EncodeUseCase(destinationPublicKey)}, " +
                "latestBlockhash(base58)=${Base58EncodeUseCase(latestBlockhash)}, " +
                "lamports=${lamports}")
        return transaction
    }

    private val TRANSFER_SOL_USE_CASE = byteArrayOf(
        0x01.toByte(), // 1 signature required (fee payer)
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // First signature (fee payer account)
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x01.toByte(), // 1 signature required (fee payer)
        0x00.toByte(), // 0 read-only account signatures
        0x01.toByte(), // 1 read-only account not requiring a signature
        0x03.toByte(), // 3 accounts
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // Fee payer account public key (placeholder)
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // Destination account (placeholder)
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // System program
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // Recent blockhash (placeholder)
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x01.toByte(),
        0x02.toByte(), // program ID (index into list of accounts)
        0x02.toByte(), // 2 accounts
        0x00.toByte(), // account index 0
        0x01.toByte(), // account index 1
        0x0C.toByte(), // 12 byte payload
        0x02.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // Transfer function
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // Lamports (placeholder)
    )

    private val TAG = TransferLamportsUseCase::class.simpleName
    private const val SIGNATURE_OFFSET = 1
    private const val SIGNATURE_LEN = 64
    private const val HEADER_OFFSET = 65
    private const val ACCOUNT_PUBLIC_KEY_OFFSET = 69
    private const val ACCOUNT_PUBLIC_KEY_LEN = 32
    private const val DESTINATION_PUBLIC_KEY_OFFSET = 101
    private const val DESTINATION_PUBLIC_KEY_LEN = 32
    private const val BLOCKHASH_OFFSET = 165
    private const val BLOCKHASH_LEN = 32
    private const val TRANSFER_LAMPORTS_OFFSET = 207
    private const val TRANSFER_LAMPORTS_LEN = 8
}