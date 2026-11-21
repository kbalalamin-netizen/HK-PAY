package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.IssuedCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPhysicalCardVisual
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun MyCardsScreen(
    state: MainDashboardState,
    onRevealToggle: (IssuedCard) -> Unit,
    onNavigateStore: () -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(text = "My Physical Cards", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Issued cards synchronized from authorized card issuer backend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SecurityNoticeBanner(text = stringResource(id = R.string.security_card_notice))
                }
            }

            if (state.issuedCards.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.CreditCard,
                        title = "No Issued Physical Cards",
                        subtitle = "Once your card order is verified, KYC approved, and issued by the provider, your physical card will appear here.",
                        actionLabel = "Order a Physical Card",
                        onAction = onNavigateStore
                    )
                }
            } else {
                items(state.issuedCards, key = { it.cardId }) { card ->
                    val revealed = state.revealedCardData[card.cardId]
                    val isRevealing = state.revealingCardId == card.cardId
                    val maskedNumber = if (card.last4.isNotBlank()) {
                        "••••  ••••  ••••  ${card.last4}"
                    } else {
                        "••••  ••••  ••••  ••••"
                    }

                    GlassCard {
                        HKPayPhysicalCardVisual(
                            cardName = card.cardType,
                            cardholderName = card.cardholderName,
                            cardType = card.cardType,
                            network = card.network,
                            maskedOrFullNumber = revealed?.cardNumber ?: maskedNumber,
                            expiryDate = revealed?.expiryDate ?: "••/••",
                            statusBadgeText = card.cardStatus
                        )

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Issuing Provider", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(card.issuingProvider, style = MaterialTheme.typography.titleSmall)
                            }
                            StatusBadge(status = card.activationStatus)
                        }

                        if (card.providerBalanceSupported) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = extra.electricBlue.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Card Balance", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = FormatUtils.formatMoney(card.balance, card.balanceCurrency),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = extra.cyanGlow
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        when {
                            card.providerSecureViewSupported && card.providerCardApiUrl.startsWith("https://") -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onRevealToggle(card) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("card_reveal_button_${card.cardId}"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (isRevealing) {
                                            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(
                                                imageVector = if (revealed != null) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = null
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(if (revealed != null) "Hide Details" else "Reveal Card")
                                        }
                                    }
                                    if (revealed != null && revealed.cardNumber.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(revealed.cardNumber))
                                                onCopied("Card number copied")
                                            },
                                            modifier = Modifier.height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy card number")
                                            Spacer(Modifier.width(6.dp))
                                            Text("Copy")
                                        }
                                    }
                                }
                                if (revealed != null) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.35f)),
                                        color = extra.electricBlue.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("EXPIRY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(revealed.expiryDate, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("CVV", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(revealed.cvv, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            card.providerSecureViewSupported && card.hostedCardViewUrl.startsWith("https://") -> {
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(card.hostedCardViewUrl)))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Open Provider Hosted Card View")
                                }
                            }
                            else -> {
                                Text(
                                    text = "Full PAN & CVV reveal is only enabled when your issuing provider API or hosted PCI view is connected.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Card-related Transaction History
            val cardTxs = state.transactions.filter {
                it.cardId.isNotBlank() || it.txType.contains("Card", ignoreCase = true)
            }
            if (cardTxs.isNotEmpty()) {
                item {
                    Text(text = "Card Transactions", style = MaterialTheme.typography.titleMedium)
                }
                items(cardTxs.take(10), key = { it.txId }) { tx ->
                    GlassCard(contentPadding = PaddingValues(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.txType, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${tx.txId} • ${FormatUtils.formatDateTime(tx.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatMoney(tx.amount, tx.currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = extra.cyanGlow
                                )
                                StatusBadge(status = tx.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
