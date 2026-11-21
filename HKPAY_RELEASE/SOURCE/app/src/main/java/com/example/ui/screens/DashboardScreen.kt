package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayLesson
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.CopyableChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    state: MainDashboardState,
    tutorialCompleted: Boolean,
    onNavigateBuyCard: () -> Unit,
    onNavigateCardSelection: () -> Unit,
    onNavigateMyCards: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateOrderDetail: (String) -> Unit,
    onNavigateTransactions: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSupport: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateCurrencyRates: () -> Unit,
    onNavigateKyc: () -> Unit,
    onNavigateTutorials: () -> Unit,
    onNavigateReferrals: () -> Unit,
    onCopiedMessage: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val clipboard = LocalClipboardManager.current
    val profile = state.userProfile

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Brand & Notification Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.hk_pay_logo),
                            contentDescription = "HK PAY Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "HK PAY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Welcome, ${profile.fullName.ifBlank { "Member" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = extra.cyanGlow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateNotifications,
                            modifier = Modifier.testTag("dashboard_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (state.unreadNotificationsCount > 0) {
                                        Badge(containerColor = extra.danger) {
                                            Text(
                                                text = state.unreadNotificationsCount.coerceAtMost(99).toString(),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }

                        Surface(
                            onClick = onNavigateProfile,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("dashboard_avatar_button"),
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, extra.cyanGlow.copy(alpha = 0.7f)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Profile",
                                        tint = extra.cyanGlow
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // System Notice Banner if configured in Firebase
            if (state.appSettings.systemNotice.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extra.electricBlue.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = state.appSettings.systemNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }

            // Member Identity & Status Card
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName.ifBlank { "HK PAY Member" },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            CopyableChip(
                                label = "ID",
                                value = profile.publicUserId.ifBlank { "HKP-MEMBER" },
                                onCopy = {
                                    clipboard.setText(AnnotatedString(profile.publicUserId))
                                    onCopiedMessage("User ID copied")
                                }
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadge(status = "Account: ${profile.accountStatus}")
                            Surface(
                                onClick = onNavigateKyc,
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent
                            ) {
                                StatusBadge(status = "KYC: ${state.effectiveKycStatus}")
                            }
                        }
                    }

                    if (state.effectiveKycStatus == "Not Submitted" || state.effectiveKycStatus == "Rejected") {
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            onClick = onNavigateKyc,
                            shape = RoundedCornerShape(14.dp),
                            color = extra.warning.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, extra.warning.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_kyc_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = null,
                                    tint = extra.warning
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (state.effectiveKycStatus == "Rejected") {
                                            "KYC Verification Rejected"
                                        } else {
                                            "Complete KYC Verification"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = extra.warning
                                    )
                                    Text(
                                        text = "Tap to submit NID or Passport for physical card eligibility.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // KPI Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatTile(
                        title = "Card Orders",
                        value = state.orders.size.toString(),
                        accent = extra.cyanGlow,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateOrders
                    )
                    DashboardStatTile(
                        title = "Active Cards",
                        value = state.activeCardsCount.toString(),
                        accent = extra.success,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateMyCards
                    )
                    DashboardStatTile(
                        title = "Pending",
                        value = state.pendingOrdersCount.toString(),
                        accent = extra.warning,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateOrders
                    )
                }
            }

            // Quick Actions Grid
            item {
                Column {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    val actions = listOf(
                        Triple("Buy Physical Card", Icons.Rounded.AddCard, onNavigateBuyCard),
                        Triple("Card Designs", Icons.Rounded.Palette, onNavigateCardSelection),
                        Triple("My Cards", Icons.Rounded.CreditCard, onNavigateMyCards),
                        Triple("Track Order", Icons.Rounded.LocalShipping, onNavigateOrders),
                        Triple("Transactions", Icons.AutoMirrored.Rounded.ReceiptLong, onNavigateTransactions),
                        Triple("Refer & Earn", Icons.Rounded.CardGiftcard, onNavigateReferrals),
                        Triple("App Guide", Icons.Rounded.PlayLesson, onNavigateTutorials),
                        Triple("Profile", Icons.Rounded.Person, onNavigateProfile),
                        Triple("Support", Icons.Rounded.SupportAgent, onNavigateSupport)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        actions.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { (label, icon, onClick) ->
                                    QuickActionTile(
                                        label = label,
                                        icon = icon,
                                        onClick = onClick,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Referral & Tutorial Highlight Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        onClick = onNavigateReferrals,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_referral_card"),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = extra.cyanGlow)
                            Spacer(Modifier.width(8.dp))
                            Text("Referral Bonus", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (state.availableReferralCredit > 0.0) {
                                "${FormatUtils.formatMoney(state.availableReferralCredit, "USD")} credit available"
                            } else {
                                "Code: ${state.userProfile.referralCode.ifBlank { "HKR" }}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.success
                        )
                    }
                    GlassCard(
                        onClick = onNavigateTutorials,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_tutorial_card"),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PlayLesson, contentDescription = null, tint = extra.cyanGlow)
                            Spacer(Modifier.width(8.dp))
                            Text("HK PAY Guide", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (!tutorialCompleted) "4-step walkthrough" else "Replay app tutorials",
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.cyanGlow
                        )
                    }
                }
            }

            // Currency Rates Preview
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Currency Rates",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            onClick = onNavigateCurrencyRates,
                            modifier = Modifier.testTag("dashboard_all_rates_button")
                        ) {
                            Text("View All")
                        }
                    }
                    if (state.currencyRates.isEmpty()) {
                        GlassCard(onClick = onNavigateCurrencyRates) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CurrencyExchange,
                                    contentDescription = null,
                                    tint = extra.cyanGlow
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Live currency rates sync from Firebase RTDB (/currency_rates). Tap to view converter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.currencyRates, key = { it.id }) { rate ->
                                GlassCard(
                                    onClick = onNavigateCurrencyRates,
                                    modifier = Modifier.width(178.dp),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Text(
                                        text = "${rate.baseCurrency} → ${rate.targetCurrency}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = extra.cyanGlow
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = FormatUtils.formatMoney(rate.cardPurchaseRate, rate.targetCurrency),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "Market: ${FormatUtils.formatMoney(rate.currentRate, rate.targetCurrency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Orders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Orders",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (state.orders.isNotEmpty()) {
                        TextButton(onClick = onNavigateOrders) {
                            Text("See All")
                        }
                    }
                }
            }

            if (state.orders.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.CreditCard,
                        title = "No Card Orders Yet",
                        subtitle = "Explore available physical card packages and place your first order.",
                        actionLabel = "Browse Card Store",
                        onAction = onNavigateBuyCard
                    )
                }
            } else {
                items(state.orders.take(3), key = { it.orderId }) { order ->
                    GlassCard(
                        onClick = { onNavigateOrderDetail(order.orderId) },
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.packageName.ifBlank { "Physical Card" },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${order.orderId} • ${FormatUtils.formatDate(order.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatMoney(order.totalPrice, order.currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = extra.cyanGlow
                                )
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(status = order.orderStatus)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DashboardStatTile(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accent
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    GlassCard(
        onClick = onClick,
        modifier = modifier.testTag("quick_action_${label.lowercase().replace(" ", "_")}"),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                extra.electricBlue.copy(alpha = 0.24f),
                                extra.cyanGlow.copy(alpha = 0.14f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = extra.cyanGlow,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
