package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.data.model.CardDesignOption
import com.example.data.model.CardPackage
import com.example.data.model.ThemeMode
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPhysicalCardVisual
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils
import kotlin.math.absoluteValue

val HKPayCardDesigns = listOf(
    CardDesignOption(
        id = "obsidian_void",
        name = "Obsidian Navy",
        finishLabel = "Matte Stealth Finish",
        description = "Deep abyssal navy metal core with signature cyan edge glow.",
        gradientStartHex = 0xFF0A1B47,
        gradientEndHex = 0xFF020718,
        accentHex = 0xFF00E5FF
    ),
    CardDesignOption(
        id = "electric_cyan",
        name = "Electric Cyan",
        finishLabel = "Brushed Metallic Finish",
        description = "High-contrast electric blue anodized finish with luminous cyan wave.",
        gradientStartHex = 0xFF0B42B8,
        gradientEndHex = 0xFF031942,
        accentHex = 0xFF5CEBFF
    ),
    CardDesignOption(
        id = "midnight_graphite",
        name = "Midnight Graphite",
        finishLabel = "Carbon Black Finish",
        description = "Minimalist obsidian graphite slab engineered for global travel.",
        gradientStartHex = 0xFF1A2236,
        gradientEndHex = 0xFF060911,
        accentHex = 0xFF3E84FF
    ),
    CardDesignOption(
        id = "royal_aurora",
        name = "Royal Aurora",
        finishLabel = "Iridescent Edge Finish",
        description = "Indigo-to-teal prismatic gradient with gold EMV security chip.",
        gradientStartHex = 0xFF172D7A,
        gradientEndHex = 0xFF043B4D,
        accentHex = 0xFF00D68F
    )
)

@Composable
fun CardSelectionScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    initialPackageId: String = "",
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onProceedToBuy: (packageId: String, selectedDesignName: String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val availablePackages = remember(state.cardPackages) {
        state.cardPackages.filter { it.available }.ifEmpty { state.cardPackages }
    }
    var selectedPackage by remember(availablePackages, initialPackageId) {
        mutableStateOf(
            availablePackages.firstOrNull { it.id == initialPackageId }
                ?: availablePackages.firstOrNull()
        )
    }

    val pagerState = rememberPagerState(pageCount = { HKPayCardDesigns.size })
    val activeDesign = HKPayCardDesigns[pagerState.currentPage]
    val orderingEnabled = state.appSettings.cardOrderingEnabled

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Select Card Design",
                onBack = onBack,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(HKPayIcons.Palette, contentDescription = null, tint = extra.cyanGlow)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Swipe to Choose Physical Finish",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Preview how your HK PAY physical card looks before completing your order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Horizontal Pager Card Showcase
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 14.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_selection_pager")
                ) { page ->
                    val design = HKPayCardDesigns[page]
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val scale = lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    val alpha = lerp(0.65f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                    HKPayPhysicalCardVisual(
                        cardName = "${selectedPackage?.name ?: "HK PAY Card"} • ${design.name}",
                        cardholderName = state.userProfile.fullName.ifBlank { "HK PAY MEMBER" },
                        cardType = selectedPackage?.cardType ?: "Physical Card",
                        network = selectedPackage?.network.orEmpty(),
                        maskedOrFullNumber = "••••  ••••  ••••  ••••",
                        expiryDate = "MM/YY",
                        statusBadgeText = design.finishLabel,
                        gradientStart = Color(design.gradientStartHex),
                        gradientEnd = Color(design.gradientEndHex),
                        accentColor = Color(design.accentHex),
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                    )
                }

                // Pager Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    HKPayCardDesigns.forEachIndexed { index, design ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (selected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color(design.accentHex) else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                // Active Design Info
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            activeDesign.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeDesign.finishLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = extra.cyanGlow
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = activeDesign.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Card Package Selection
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Select Live Card Package",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))

                    if (availablePackages.isEmpty()) {
                        EmptyStateCard(
                            icon = HKPayIcons.Storefront,
                            title = "Awaiting Card Packages in Firebase",
                            subtitle = "Packages from Firebase RTDB (/card_packages) will appear here for ordering once published."
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availablePackages, key = { it.id }) { pkg ->
                                FilterChip(
                                    selected = selectedPackage?.id == pkg.id,
                                    onClick = { selectedPackage = pkg },
                                    label = {
                                        Text("${pkg.name} (${FormatUtils.formatMoney(pkg.computedTotal, pkg.currency)})")
                                    }
                                )
                            }
                        }

                        val pkg: CardPackage? = selectedPackage
                        if (pkg != null) {
                            Spacer(Modifier.height(12.dp))
                            GlassCard {
                                PriceLine("Selected Design", activeDesign.name)
                                PriceLine("Card Package", pkg.name)
                                PriceLine("Card Price", FormatUtils.formatMoney(pkg.cardPrice, pkg.currency))
                                PriceLine("Issuing + Delivery", FormatUtils.formatMoney(pkg.issuingFee + pkg.deliveryFee, pkg.currency))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Total Price",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = FormatUtils.formatMoney(pkg.computedTotal, pkg.currency),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = extra.cyanGlow
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                HKPayPrimaryButton(
                                    text = when {
                                        !orderingEnabled -> "Ordering Paused by Admin"
                                        pkg.available -> "Continue with ${activeDesign.name}"
                                        else -> "Package Unavailable"
                                    },
                                    enabled = pkg.available && orderingEnabled,
                                    icon = HKPayIcons.AddCard,
                                    onClick = { onProceedToBuy(pkg.id, activeDesign.name) },
                                    testTag = "card_selection_continue_button"
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
