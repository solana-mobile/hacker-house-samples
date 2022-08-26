/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solanamobile.mwaworkshop.usecase

enum class Commitment(val commitment: String) {
    PROCESSED("processed"),
    CONFIRMED("confirmed"),
    FINALIZED("finalized");

    companion object {
        fun fromCommitmentString(commitmentStr: String): Commitment? {
            for (commitment in values()) {
                if (commitment.commitment == commitmentStr) {
                    return commitment
                }
            }
            return null
        }
    }
}