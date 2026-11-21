package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionRecord
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.util.FormatUtils

@Composable
fun HKPayBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val extra = LocalHKPayColors.current
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bgColor, surfaceColor, bgColor)
                )
            )
            if (extra.isDark) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            extra.cyanGlow.copy(alpha = 0.14f),
                            extra.electricBlue.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = size.width * 0.85f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            extra.electricBlue.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.88f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.88f),
                    radius = size.width * 0.7f
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            extra.electricBlue.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.08f),
                        radius = size.width * 0.75f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.08f),
                    radius = size.width * 0.75f
                )
            }
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val extra = LocalHKPayColors.current
    val shape = RoundedCornerShape(20.dp)
    val border = BorderStroke(1.dp, extra.glassBorder)
    val onSurface = MaterialTheme.colorScheme.onSurface

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = extra.glassSurface,
            contentColor = onSurface,
            border = border,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = extra.glassSurface,
            contentColor = onSurface,
            border = border,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun ThemeCycleIconButton(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    val icon = when (themeMode) {
        ThemeMode.DARK -> HKPayIcons.DarkMode
        ThemeMode.LIGHT -> HKPayIcons.LightMode
        ThemeMode.NAVY -> HKPayIcons.NavyTheme
    }
    Surface(
        onClick = onToggleTheme,
        modifier = modifier
            .size(40.dp)
            .testTag("header_theme_toggle_button"),
        shape = CircleShape,
        color = extra.electricBlue.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = "Theme: ${themeMode.label}. Tap to switch theme",
                tint = extra.cyanGlow,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun HKPayPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val extra = LocalHKPayColors.current
    val tagModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = tagModifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = extra.electricBlue,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.2.dp,
                color = Color.White
            )
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    val normalized = status.lowercase()
    val tint = when {
        normalized in listOf(
            "active", "approved", "paid", "payment confirmed", "delivered", "resolved", "available", "completed", "credited"
        ) -> extra.success
        normalized in listOf(
            "pending", "payment pending", "kyc pending", "card processing", "shipped",
            "in transit", "open", "in progress", "card activation pending", "card issued"
        ) -> extra.warning
        normalized in listOf("rejected", "cancelled", "failed", "unavailable", "suspended") -> extra.danger
        else -> extra.info
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
    ) {
        Text(
            text = status.ifBlank { "Unknown" },
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HKPayPhysicalCardVisual(
    cardName: String,
    cardholderName: String,
    cardType: String,
    network: String,
    maskedOrFullNumber: String,
    expiryDate: String,
    statusBadgeText: String,
    modifier: Modifier = Modifier,
    isFrozen: Boolean = false,
    gradientStart: Color? = null,
    gradientEnd: Color? = null,
    accentColor: Color? = null
) {
    val extra = LocalHKPayColors.current
    val shape = RoundedCornerShape(22.dp)
    val startCol = if (isFrozen) Color(0xFF1A2B42) else (gradientStart ?: extra.cardGradientStart)
    val endCol = if (isFrozen) Color(0xFF0E1826) else (gradientEnd ?: extra.cardGradientEnd)
    val accentCol = if (isFrozen) Color(0xFF7DD3FC) else (accentColor ?: Color(0xFF00E5FF))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        startCol,
                        endCol,
                        startCol.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentCol.copy(alpha = 0.75f),
                        extra.electricBlue.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.18f)
                    )
                ),
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HK PAY",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cardName,
                            color = Color(0xFF5CEBFF),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StatusBadge(status = if (isFrozen) "Frozen" else statusBadgeText)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 32.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF6D365), Color(0xFFD39D38))
                            )
                        )
                        .border(1.dp, Color(0xFFFFEEAA), RoundedCornerShape(7.dp))
                )
                Icon(
                    imageVector = HKPayIcons.Contactless,
                    contentDescription = "Contactless NFC",
                    tint = Color(0xFF5CEBFF),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.weight(1f))
                if (network.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = network.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = maskedOrFullNumber,
                color = Color.White,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CARDHOLDER",
                        color = Color.White.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = cardholderName.ifBlank { "HK PAY MEMBER" }.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = cardType.ifBlank { "PHYSICAL" }.uppercase(),
                        color = Color(0xFF5CEBFF),
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (expiryDate.isNotBlank()) {
                        Text(
                            text = "EXP $expiryDate",
                            color = Color.White,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        if (isFrozen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A192F).copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF0F2942).copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = HKPayIcons.Freeze,
                            contentDescription = "Card frozen",
                            tint = Color(0xFF7DD3FC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "CARD FROZEN",
                            color = Color(0xFFE0F2FE),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KycVerifiedSealBadge(
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val extra = LocalHKPayColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = extra.electricBlue.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (showLabel) 8.dp else 4.dp,
                vertical = 3.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = HKPayIcons.VerifiedSeal,
                contentDescription = "KYC Verified Badge",
                tint = extra.cyanGlow,
                modifier = Modifier.size(16.dp)
            )
            if (showLabel) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = extra.cyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HKPayTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    themeMode: ThemeMode? = null,
    onToggleTheme: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            actions()
            if (themeMode != null && onToggleTheme != null) {
                Spacer(Modifier.width(4.dp))
                ThemeCycleIconButton(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    GlassCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(extra.electricBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = extra.cyanGlow,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                HKPayPrimaryButton(
                    text = actionLabel,
                    onClick = onAction
                )
            }
        }
    }
}

@Composable
fun CopyableChip(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    Surface(
        onClick = onCopy,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = extra.electricBlue.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall,
                color = extra.cyanGlow
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = HKPayIcons.ContentCopy,
                contentDescription = "Copy $label",
                tint = extra.cyanGlow,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SecurityNoticeBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = extra.info.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, extra.info.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = HKPayIcons.Shield,
                contentDescription = "Security notice",
                tint = extra.cyanGlow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TransactionActivityCard(
    tx: TransactionRecord,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    val txIcon = when {
        tx.txType.contains("Refund", ignoreCase = true) -> HKPayIcons.CurrencyExchange
        tx.txType.contains("Payment", ignoreCase = true) -> HKPayIcons.AccountBalance
        tx.txType.contains("Card", ignoreCase = true) -> HKPayIcons.CreditCard
        else -> HKPayIcons.ReceiptLong
    }

    GlassCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extra.electricBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = txIcon,
                    contentDescription = tx.txType,
                    tint = extra.cyanGlow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.txType.ifBlank { "Card Activity" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tx.description.isNotBlank()) {
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = buildString {
                        append(tx.txId)
                        if (tx.orderId.isNotBlank()) append(" • ${tx.orderId}")
                        if (tx.cardId.isNotBlank()) append(" • Card ${tx.cardId.takeLast(4)}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = extra.cyanGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = FormatUtils.formatDateTime(tx.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                val isCredit = tx.txType.contains("Refund", ignoreCase = true) ||
                    tx.txType.contains("Funding", ignoreCase = true) ||
                    tx.txType.contains("Bonus", ignoreCase = true)
                Text(
                    text = (if (isCredit) "+" else "") + FormatUtils.formatMoney(tx.amount, tx.currency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) extra.success else MaterialTheme.colorScheme.onSurface
                )
                if (tx.fee > 0.0) {
                    Text(
                        text = "Fee: ${FormatUtils.formatMoney(tx.fee, tx.currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                StatusBadge(status = tx.status)
            }
        }
    }
}

@Composable
fun RecentCardActivitySection(
    transactions: List<TransactionRecord>,
    onSeeAll: () -> Unit,
    onSelectOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Recent Card Activity",
    maxItems: Int = 4
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (transactions.isNotEmpty()) {
                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier.testTag("recent_activity_see_all_button")
                ) {
                    Text("See All")
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        if (transactions.isEmpty()) {
            EmptyStateCard(
                icon = HKPayIcons.ReceiptLong,
                title = "No Recent Card Activity",
                subtitle = "Card purchases, issuing fees, loads, and settlements from Firebase appear here in real time."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                transactions.take(maxItems).forEach { tx ->
                    TransactionActivityCard(
                        tx = tx,
                        onClick = if (tx.orderId.isNotBlank()) {
                            { onSelectOrder(tx.orderId) }
                        } else null
                    )
                }
            }
        }
    }
}
