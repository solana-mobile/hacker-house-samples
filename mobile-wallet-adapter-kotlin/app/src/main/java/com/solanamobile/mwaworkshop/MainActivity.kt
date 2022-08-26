/*
 * Copyright (c) 2022 Solana Mobile Inc.
 */

package com.solanamobile.mwaworkshop

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.GuardedBy
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.solanamobile.mwaworkshop.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var lastShownMessageSequenceNum: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    binding.accountIdentifierText.text = uiState.account.identifier
                    binding.accountBalanceLoading.visibility =
                        if (uiState.account.showBalanceRefreshing) View.VISIBLE else View.GONE
                    if (uiState.account.showBalance) {
                        binding.accountBalanceText.visibility = View.VISIBLE
                        binding.accountBalanceText.text =
                            getString(R.string.account_balance, uiState.account.balance)
                    } else {
                        binding.accountBalanceText.visibility = View.GONE
                    }

                    binding.destinationIdentifierText.text = uiState.destination.identifier
                    binding.destinationBalanceLoading.visibility =
                        if (uiState.destination.showBalanceRefreshing) View.VISIBLE else View.GONE
                    if (uiState.destination.showBalance) {
                        binding.destinationBalanceText.visibility = View.VISIBLE
                        binding.destinationBalanceText.text =
                            getString(R.string.account_balance, uiState.destination.balance)
                    } else {
                        binding.destinationBalanceText.visibility = View.GONE
                    }

                    binding.transferButton.isEnabled = uiState.enableTransfer

                    if (uiState.messages.isNotEmpty()) {
                        val message = uiState.messages.first()
                        if (message.sequenceNum != lastShownMessageSequenceNum) {
                            Snackbar.make(binding.root, message.message, Snackbar.LENGTH_SHORT)
                                .setAction(message.actionText) { startActivity(message.action) }
                                .addCallback(object :
                                    BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    override fun onDismissed(
                                        transientBottomBar: Snackbar?,
                                        event: Int
                                    ) {
                                        viewModel.messageShown()
                                    }
                                }).show()
                            lastShownMessageSequenceNum = message.sequenceNum
                        }
                    }
                }
            }
        }

        binding.selectAccountButton.setOnClickListener {
            viewModel.selectAccount(intentSender)
        }

        binding.selectDestinationButton.setOnClickListener {
            viewModel.selectDestination()
        }

        binding.transferButton.setOnClickListener {
            viewModel.doTransfer(intentSender)
        }
    }

    private val intentSender = object : MainViewModel.StartActivityForResultSender {
        @GuardedBy("this")
        private var callback: (() -> Unit)? = null

        override suspend fun startActivityForResult(
            intent: Intent,
            onActivityCompleteCallback: (() -> Unit)?
        ) {
            lifecycle.withResumed {
                synchronized(this) {
                    assert(callback == null) { "Received an activity start request while another is pending" }
                    callback = onActivityCompleteCallback
                }
                this@MainActivity.startActivityForResult(intent, WALLET_ACTIVITY_REQUEST_CODE)
            }
        }

        fun onActivityComplete() {
            synchronized(this) {
                callback?.let { it() }
                callback = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            WALLET_ACTIVITY_REQUEST_CODE -> intentSender.onActivityComplete()
        }
    }

    companion object {
        private const val WALLET_ACTIVITY_REQUEST_CODE = 10000
    }
}