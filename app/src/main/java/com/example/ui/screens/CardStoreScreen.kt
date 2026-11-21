package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CardPackage
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPhysicalCardVisual
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardStoreScreen(
    state: MainDashboardState,
    onSelectPackage: (CardPackage) -> Unit
) {
    val extra = LocalHKPayColors.current
    var showOnlyAvailable by remember { mutableStateOf(false) }

    val packages = remember(state.cardPackages, showOnlyAvailable) {
        if (showOnlyAvailable) state.cardPackages.filter { it.available } else state.cardPackages
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
                    Text(
                        text = "Physical Card Store",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Live card packages synced from Firebase Realtime Database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !showOnlyAvailable,
                            onClick = { showOnlyAvailable = false },
                            label = { Text("All Packages (${state.cardPackages.size})") }
                        )
                        FilterChip(
                            selected = showOnlyAvailable,
                            onClick = { showOnlyAvailable = true },
                            label = { Text("Available Only") }
                        )
                    }
                }
            }

            if (packages.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Storefront,
                        title = "No Card Packages Published Yet",
                        subtitle = "Card packages appear here automatically when configured in Firebase Realtime Database at /card_packages."
                    )
                }
            } else {
                items(packages, key = { it.id }) { pkg ->
                    CardPackageItem(
                        pkg = pkg,
                        holderName = state.userProfile.fullName,
                        onOrderClick = { onSelectPackage(pkg) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardPackageItem(
    pkg: CardPackage,
    holderName: String,
    onOrderClick: () -> Unit
) {
    val extra = LocalHKPayColors.current
    GlassCard {
        if (pkg.imageUrl.isNotBlank()) {
            AsyncImage(
                model = pkg.imageUrl,
                contentDescription = pkg.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        } else {
            HKPayPhysicalCardVisual(
                cardName = pkg.name,
                cardholderName = holderName.ifBlank { "HK PAY HOLDER" },
                cardType = pkg.cardType,
                network = pkg.network,
                maskedOrFullNumber = "••••  ••••  ••••  ••••",
                expiryDate = "MM/YY",
                statusBadgeText = if (pkg.available) "Available" else "Unavailable"
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = pkg.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = buildString {
                        append(pkg.cardType)
                        if (pkg.network.isNotBlank()) append(" • ${pkg.network}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = extra.cyanGlow
                )
            }
            StatusBadge(status = if (pkg.available) "Available" else "Unavailable")
        }

        if (pkg.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = pkg.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pkg.usdPackage > 0.0) {
                PackageInfoPill("USD Package: ${FormatUtils.formatMoney(pkg.usdPackage, "USD")}")
            }
            PackageInfoPill(
                text = if (pkg.kycRequired) "KYC Required" else "No KYC Required",
                highlight = pkg.kycRequired
            )
            pkg.highlights.forEach { tag ->
                PackageInfoPill(tag)
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        PriceLine("Card Price", FormatUtils.formatMoney(pkg.cardPrice, pkg.currency))
        PriceLine("Issuing Fee", FormatUtils.formatMoney(pkg.issuingFee, pkg.currency))
        PriceLine("Delivery Fee", FormatUtils.formatMoney(pkg.deliveryFee, pkg.currency))
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Price",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = FormatUtils.formatMoney(pkg.computedTotal, pkg.currency),
                style = MaterialTheme.typography.titleLarge,
                color = extra.cyanGlow
            )
        }

        Spacer(Modifier.height(14.dp))
        HKPayPrimaryButton(
            text = if (pkg.available) "Buy Physical Card" else "Currently Unavailable",
            onClick = onOrderClick,
            enabled = pkg.available,
            icon = Icons.Rounded.AddCard,
            testTag = "store_order_button_${pkg.id}"
        )
    }
}

@Composable
private fun PackageInfoPill(text: String, highlight: Boolean = false) {
    val extra = LocalHKPayColors.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) extra.warning.copy(alpha = 0.14f) else extra.electricBlue.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (highlight) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = extra.warning,
                    modifier = Modifier.height(14.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlight) extra.warning else extra.cyanGlow
            )
        }
    }
}

@Composable
fun PriceLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
