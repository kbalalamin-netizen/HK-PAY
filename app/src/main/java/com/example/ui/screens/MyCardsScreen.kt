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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.data.model.IssuedCard
import com.example.data.model.ThemeMode
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPhysicalCardVisual
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.PaymentCardPreview
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.components.TransactionActivityCard
import com.example.ui.components.TwoFactorVerifyDialog
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.BiometricHelper
import com.example.util.FormatUtils
import com.example.util.HKPayStrings

@Composable
fun MyCardsScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    balanceVisible: Boolean,
    biometricEnabled: Boolean,
    isCardManagementVerified: Boolean,
    twoFactorEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onMarkCardManagementVerified: () -> Unit,
    onToggleCardFreeze: (IssuedCard, Boolean) -> Unit,
    onRevealToggle: (IssuedCard) -> Unit,
    verifyTwoFactorCode: (String) -> Boolean,
    onNavigateStore: () -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val activity = context as? FragmentActivity

    var pending2FactorAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var promptedAutoBio by remember { mutableStateOf(false) }

    fun runWithOptional2Factor(action: () -> Unit) {
        if (twoFactorEnabled) {
            pending2FactorAction = action
        } else {
            action()
        }
    }

    if (pending2FactorAction != null) {
        TwoFactorVerifyDialog(
            title = "Authorize Card Security Action",
            verifyCode = verifyTwoFactorCode,
            onVerified = {
                val action = pending2FactorAction
                pending2FactorAction = null
                action?.invoke()
            },
            onDismiss = { pending2FactorAction = null }
        )
    }

    // Prompt Android Biometric automatically if enabled and Card Management is not yet verified this unlock cycle
    LaunchedEffect(biometricEnabled, isCardManagementVerified) {
        if (biometricEnabled && !isCardManagementVerified && !promptedAutoBio && activity != null) {
            promptedAutoBio = true
            BiometricHelper.authenticate(
                activity = activity,
                title = "Card Management Security",
                subtitle = "Verify fingerprint or face to access your HK PAY physical cards",
                negativeButtonText = "Cancel",
                onSuccess = { onMarkCardManagementVerified() },
                onError = { msg -> onCopied(msg) }
            )
        }
    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = HKPayStrings.myPhysicalCards(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        ThemeCycleIconButton(
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Manage card freeze status, biometric reveal, and card transaction activity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SecurityNoticeBanner(text = stringResource(id = R.string.security_card_notice))
                }
            }

            if (biometricEnabled && !isCardManagementVerified) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = HKPayIcons.Fingerprint,
                                contentDescription = "Biometric card lock",
                                tint = extra.cyanGlow,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Biometric Card Shield",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Verify your fingerprint or face biometric to access card controls, freeze toggles, and sensitive card numbers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            HKPayPrimaryButton(
                                text = "Unlock Card Management",
                                icon = HKPayIcons.Fingerprint,
                                testTag = "cards_biometric_unlock_button",
                                onClick = {
                                    if (activity != null) {
                                        BiometricHelper.authenticate(
                                            activity = activity,
                                            title = "Unlock Card Management",
                                            subtitle = "Verify biometrics to access card controls",
                                            negativeButtonText = "Cancel",
                                            onSuccess = { onMarkCardManagementVerified() },
                                            onError = { msg -> onCopied(msg) }
                                        )
                                    } else {
                                        onMarkCardManagementVerified()
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (state.issuedCards.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentCardPreview(
                            cardholderName = state.userProfile.fullName.ifBlank { "CARDHOLDER NAME" },
                            cardNumberPlaceholder = "4532  ••••  ••••  9810",
                            expiryPlaceholder = "12/29",
                            network = "VISA",
                            cardTitle = "HK PAY Preview Card",
                            modifier = Modifier.testTag("cards_virtual_preview")
                        )
                        EmptyStateCard(
                            icon = HKPayIcons.CreditCard,
                            title = "Virtual Preview Active • No Issued Cards Yet",
                            subtitle = "Above is your interactive cardholder preview. Once your card order is verified, KYC approved, and issued, real freeze controls & balance sync appear here.",
                            actionLabel = "Order a Physical Card",
                            onAction = onNavigateStore
                        )
                    }
                }
            } else {
                items(state.issuedCards, key = { it.cardId }) { card ->
                    val revealed = state.revealedCardData[card.cardId]
                    val isRevealing = state.revealingCardId == card.cardId
                    val isFrozen = state.isCardFrozen(card)
                    val isUpdatingFreeze = state.freezingCardId == card.cardId
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
                            statusBadgeText = card.cardStatus,
                            isFrozen = isFrozen
                        )

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Issuing Provider", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(card.issuingProvider, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            StatusBadge(status = if (isFrozen) "Frozen" else card.activationStatus)
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
                                    Text("Card Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = if (balanceVisible) FormatUtils.formatMoney(card.balance, card.balanceCurrency) else "••••••",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = extra.cyanGlow
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(10.dp))

                        // Instant Freeze Card Toggle
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isFrozen) extra.warning.copy(alpha = 0.12f) else extra.glassSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isFrozen) extra.warning.copy(alpha = 0.55f) else extra.glassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = HKPayIcons.Freeze,
                                        contentDescription = null,
                                        tint = if (isFrozen) extra.warning else extra.cyanGlow
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = HKPayStrings.freezeCard(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isFrozen) "Card paused. New transactions blocked." else "Temporarily disable all card transactions.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isUpdatingFreeze) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Switch(
                                        checked = isFrozen,
                                        onCheckedChange = { targetFrozen ->
                                            runWithOptional2Factor {
                                                onToggleCardFreeze(card, targetFrozen)
                                            }
                                        },
                                        modifier = Modifier.testTag("card_freeze_switch_${card.cardId}")
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        if (isFrozen) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = extra.warning.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, extra.warning.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = "Card is currently frozen. All new transactions and card detail reveals are blocked until you unfreeze it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extra.warning,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        when {
                            card.providerSecureViewSupported && card.providerCardApiUrl.startsWith("https://") -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        enabled = !isFrozen,
                                        onClick = {
                                            val alreadyRevealed = revealed != null
                                            if (!alreadyRevealed && biometricEnabled && activity != null) {
                                                BiometricHelper.authenticate(
                                                    activity = activity,
                                                    title = "Authenticate to Reveal Card",
                                                    subtitle = "Verify fingerprint or face ID to view sensitive card details",
                                                    negativeButtonText = "Cancel",
                                                    onSuccess = {
                                                        runWithOptional2Factor { onRevealToggle(card) }
                                                    },
                                                    onError = { msg -> onCopied(msg) }
                                                )
                                            } else if (!alreadyRevealed) {
                                                runWithOptional2Factor { onRevealToggle(card) }
                                            } else {
                                                onRevealToggle(card)
                                            }
                                        },
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
                                                imageVector = if (revealed != null) HKPayIcons.VisibilityOff else HKPayIcons.Visibility,
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
                                            Icon(HKPayIcons.ContentCopy, contentDescription = "Copy card number")
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
                                    enabled = !isFrozen,
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(card.hostedCardViewUrl)))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(HKPayIcons.OpenInNew, contentDescription = null)
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
                    Text(
                        text = "Card Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(cardTxs.take(10), key = { it.txId }) { tx ->
                    TransactionActivityCard(tx = tx)
                }
            }
        }
    }
}
