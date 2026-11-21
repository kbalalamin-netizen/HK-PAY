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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.data.model.AppLanguage
import com.example.data.model.DashboardSlider
import com.example.data.model.IssuedCard
import com.example.data.model.ThemeMode
import com.example.ui.components.CopyableChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.KycVerifiedSealBadge
import com.example.ui.components.RecentCardActivitySection
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils
import com.example.util.HKPayStrings
import com.example.util.LocationCatalog

@Composable
fun DashboardScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    balanceVisible: Boolean,
    tutorialCompleted: Boolean,
    onToggleTheme: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onToggleBalanceVisible: () -> Unit,
    onNavigateRoute: (String) -> Unit,
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
    val countryInfo = LocationCatalog.countryFor(profile.country)
    val sliders = state.activeSliders

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
            // Top Brand, Country Flag, Language Chip, Theme Switcher, Notification & Avatar Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hk_pay_logo),
                            contentDescription = "HK PAY Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "HK PAY",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = countryInfo.flagEmoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (state.isKycVerified) {
                                    KycVerifiedSealBadge(showLabel = false)
                                }
                            }
                            Text(
                                text = HKPayStrings.welcomeMember(profile.fullName.ifBlank { "Member" }),
                                style = MaterialTheme.typography.bodySmall,
                                color = extra.cyanGlow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = onOpenLanguagePicker,
                            shape = RoundedCornerShape(50),
                            color = extra.electricBlue.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("header_language_button")
                        ) {
                            Text(
                                text = appLanguage.shortTag,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                            )
                        }

                        ThemeCycleIconButton(
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme
                        )

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
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Surface(
                            onClick = onNavigateProfile,
                            modifier = Modifier
                                .size(40.dp)
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

            // Admin System Notice / Banner
            if (state.appSettings.systemNotice.isNotBlank() || state.appSettings.bannerTitle.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extra.electricBlue.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (state.appSettings.bannerTitle.isNotBlank()) {
                                Text(
                                    text = state.appSettings.bannerTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = extra.cyanGlow
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            if (state.appSettings.systemNotice.isNotBlank()) {
                                Text(
                                    text = state.appSettings.systemNotice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Admin-Controllable + Authentic Default Dashboard Slider (HK PAY Logo on Every Slide)
            if (sliders.isNotEmpty()) {
                item {
                    DashboardPromoSlider(
                        sliders = sliders,
                        onActionRoute = onNavigateRoute
                    )
                }
            }

            // Card-Linked Balance Hero Card (Show / Hide)
            item {
                val linkedCards = state.issuedCards
                val primaryCurrency = linkedCards.firstOrNull { it.providerBalanceSupported }
                    ?.balanceCurrency
                    ?.ifBlank { profile.preferredCurrency }
                    ?: profile.preferredCurrency.ifBlank { "USD" }
                val totalCardBalance = state.totalLinkedCardBalance

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.2.dp, extra.cyanGlow.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_balance_hero_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(extra.cardGradientStart, extra.cardGradientEnd)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = HKPayStrings.cardLinkedBalance(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7BE8FF)
                                    )
                                    Text(
                                        text = if (linkedCards.isNotEmpty()) {
                                            "Synced with ${linkedCards.size} issued physical card(s)"
                                        } else {
                                            "Link a physical card to track live balance"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                }
                                Surface(
                                    onClick = onToggleBalanceVisible,
                                    modifier = Modifier.testTag("dashboard_balance_toggle_button"),
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, Color(0xFF5CEBFF).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (balanceVisible) HKPayIcons.VisibilityOff else HKPayIcons.Visibility,
                                            contentDescription = if (balanceVisible) "Hide card balance" else "Show card balance",
                                            tint = Color(0xFF5CEBFF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (balanceVisible) "Hide" else "Show",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = if (balanceVisible) {
                                    FormatUtils.formatMoney(totalCardBalance, primaryCurrency)
                                } else {
                                    "••••••••"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("dashboard_balance_value")
                            )

                            if (linkedCards.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(linkedCards, key = { it.cardId }) { card ->
                                        val frozen = state.isCardFrozen(card)
                                        Surface(
                                            onClick = onNavigateMyCards,
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.10f),
                                            border = BorderStroke(
                                                1.dp,
                                                if (frozen) Color(0xFF7DD3FC).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (frozen) HKPayIcons.Freeze else HKPayIcons.CreditCard,
                                                    contentDescription = null,
                                                    tint = if (frozen) Color(0xFF7DD3FC) else Color(0xFF5CEBFF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "${card.cardType.ifBlank { "Card" }} ••${card.last4.ifBlank { "0000" }}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = when {
                                                            frozen -> "FROZEN"
                                                            !balanceVisible -> "••••••"
                                                            card.providerBalanceSupported -> FormatUtils.formatMoney(card.balance, card.balanceCurrency)
                                                            else -> card.cardStatus
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (frozen) Color(0xFF7DD3FC) else Color(0xFF5CEBFF)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = if (linkedCards.isNotEmpty()) onNavigateMyCards else onNavigateBuyCard,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF5CEBFF).copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = if (linkedCards.isNotEmpty()) HKPayIcons.CreditCard else HKPayIcons.AddCard,
                                        contentDescription = null,
                                        tint = Color(0xFF5CEBFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (linkedCards.isNotEmpty()) "View Cards" else "Order Card",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                OutlinedButton(
                                    onClick = onNavigateTransactions,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Icon(
                                        imageVector = HKPayIcons.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Activity",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Member Identity & Status Card (ONLY UID is copyable; verified seal + country flag)
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile.fullName.ifBlank { "HK PAY Member" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (state.isKycVerified) {
                                    KycVerifiedSealBadge(showLabel = true)
                                }
                            }
                            Text(
                                text = "${countryInfo.flagEmoji} ${countryInfo.name} • ${profile.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            CopyableChip(
                                label = "UID",
                                value = profile.publicUserId.ifBlank { "HKP-MEMBER" },
                                onCopy = {
                                    clipboard.setText(AnnotatedString(profile.publicUserId))
                                    onCopiedMessage("User ID copied")
                                },
                                modifier = Modifier.testTag("dashboard_copy_uid_chip")
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
                                    imageVector = HKPayIcons.VerifiedUser,
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
                                        text = "Tap to submit NID/Passport & Live Face Liveness check for Verified badge.",
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
                        text = HKPayStrings.quickActions(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    val actions = buildList {
                        add(Triple("Buy Physical Card", HKPayIcons.AddCard, onNavigateBuyCard))
                        add(Triple("Card Designs", HKPayIcons.Palette, onNavigateCardSelection))
                        add(Triple("My Cards", HKPayIcons.CreditCard, onNavigateMyCards))
                        add(Triple("Track Order", HKPayIcons.LocalShipping, onNavigateOrders))
                        add(Triple("Transactions", HKPayIcons.ReceiptLong, onNavigateTransactions))
                        if (state.appSettings.referralProgramEnabled) {
                            add(Triple("Refer & Earn", HKPayIcons.CardGiftcard, onNavigateReferrals))
                        } else {
                            add(Triple("Exchange Rates", HKPayIcons.CurrencyExchange, onNavigateCurrencyRates))
                        }
                        add(Triple("App Guide", HKPayIcons.PlayLesson, onNavigateTutorials))
                        add(Triple("Profile", Icons.Rounded.Person, onNavigateProfile))
                        add(Triple("Support", HKPayIcons.SupportAgent, onNavigateSupport))
                    }
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
                    if (state.appSettings.referralProgramEnabled) {
                        GlassCard(
                            onClick = onNavigateReferrals,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_referral_card"),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(HKPayIcons.CardGiftcard, contentDescription = null, tint = extra.cyanGlow)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Referral Bonus",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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
                    }
                    GlassCard(
                        onClick = onNavigateTutorials,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_tutorial_card"),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(HKPayIcons.PlayLesson, contentDescription = null, tint = extra.cyanGlow)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "HK PAY Guide",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                            text = HKPayStrings.currencyRates(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
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
                                    imageVector = HKPayIcons.CurrencyExchange,
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
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
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

            // Reusable Recent Card Activity / Transaction History Component
            item {
                RecentCardActivitySection(
                    transactions = state.transactions,
                    onSeeAll = onNavigateTransactions,
                    onSelectOrder = onNavigateOrderDetail
                )
            }

            // Recent Orders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = HKPayStrings.recentOrders(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
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
                        icon = HKPayIcons.CreditCard,
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
private fun DashboardPromoSlider(
    sliders: List<DashboardSlider>,
    onActionRoute: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val pagerState = rememberPagerState(pageCount = { sliders.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_slider_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val slide = sliders[page]
            val startColor = Color(slide.gradientStartHex)
            val endColor = Color(slide.gradientEndHex)

            Surface(
                onClick = { onActionRoute(slide.actionRoute) },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f)),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .testTag("dashboard_slider_page_$page")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(startColor, endColor)))
                ) {
                    if (slide.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = slide.imageUrl,
                            contentDescription = slide.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.24f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = slide.badge.ifBlank { "HK PAY" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7BE8FF),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = slide.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.84f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${slide.ctaLabel.ifBlank { "Open" }} →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5CEBFF)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        // Prominent HK PAY official logo on every slide
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.2.dp, Color(0xFF5CEBFF).copy(alpha = 0.55f)),
                            modifier = Modifier.size(84.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hk_pay_logo),
                                contentDescription = "HK PAY Official Brand",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(sliders.size) { idx ->
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (active) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) extra.cyanGlow else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
