package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CardOrder
import com.example.data.model.HKPayConstants
import com.example.data.model.OrderTrackStage
import com.example.data.model.ThemeMode
import com.example.data.model.resolvedTrackStage
import com.example.data.model.trackingProgressFraction
import com.example.ui.components.CopyableChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun OrdersScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSelectOrder: (String) -> Unit,
    onNavigateStore: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Processing", "Shipped", "Delivered", "Unpaid", "Cancelled")

    val processingCount = remember(state.orders) {
        state.orders.count { it.resolvedTrackStage() == OrderTrackStage.PROCESSING }
    }
    val shippedCount = remember(state.orders) {
        state.orders.count { it.resolvedTrackStage() == OrderTrackStage.SHIPPED }
    }
    val deliveredCount = remember(state.orders) {
        state.orders.count { it.resolvedTrackStage() == OrderTrackStage.DELIVERED }
    }

    val filteredOrders = remember(state.orders, filter) {
        when (filter) {
            "Processing" -> state.orders.filter {
                val stage = it.resolvedTrackStage()
                stage == OrderTrackStage.PROCESSING || stage == OrderTrackStage.PLACED
            }
            "Shipped" -> state.orders.filter { it.resolvedTrackStage() == OrderTrackStage.SHIPPED }
            "Delivered" -> state.orders.filter { it.resolvedTrackStage() == OrderTrackStage.DELIVERED }
            "Unpaid" -> state.orders.filter { it.paymentStatus.equals("Unpaid", ignoreCase = true) }
            "Cancelled" -> state.orders.filter { it.resolvedTrackStage() == OrderTrackStage.CANCELLED }
            else -> state.orders
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order Tracking Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Live Firebase Realtime Database tracking for physical cards.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ThemeCycleIconButton(
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Live Order Tracking Summary Dashboard
                    GlassCard(modifier = Modifier.testTag("orders_tracking_dashboard")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Shipment Pipeline",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = extra.cyanGlow.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${state.orders.size} Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OrderStageSummaryTile(
                                label = "Processing",
                                count = processingCount,
                                tint = extra.warning,
                                selected = filter == "Processing",
                                onClick = { filter = if (filter == "Processing") "All" else "Processing" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("orders_kpi_processing")
                            )
                            OrderStageSummaryTile(
                                label = "Shipped",
                                count = shippedCount,
                                tint = extra.cyanGlow,
                                selected = filter == "Shipped",
                                onClick = { filter = if (filter == "Shipped") "All" else "Shipped" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("orders_kpi_shipped")
                            )
                            OrderStageSummaryTile(
                                label = "Delivered",
                                count = deliveredCount,
                                tint = extra.success,
                                selected = filter == "Delivered",
                                onClick = { filter = if (filter == "Delivered") "All" else "Delivered" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("orders_kpi_delivered")
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { label ->
                            FilterChip(
                                selected = filter == label,
                                onClick = { filter = label },
                                label = { Text(label) },
                                modifier = Modifier.testTag("orders_filter_${label.lowercase()}")
                            )
                        }
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = HKPayIcons.LocalShipping,
                        title = if (filter == "All") "No Orders Found" else "No $filter Orders",
                        subtitle = if (filter == "All") {
                            "Orders you place in the Physical Card Store will track live here."
                        } else {
                            "No orders currently match the $filter stage."
                        },
                        actionLabel = if (filter == "All") "Browse Card Store" else "Show All Orders",
                        onAction = {
                            if (filter == "All") onNavigateStore() else filter = "All"
                        }
                    )
                }
            } else {
                items(filteredOrders, key = { it.orderId }) { order ->
                    val stage = order.resolvedTrackStage()
                    GlassCard(
                        onClick = { onSelectOrder(order.orderId) },
                        modifier = Modifier.testTag("order_card_${order.orderId}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.packageName.ifBlank { "Physical Card" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append(order.orderId)
                                        if (order.selectedDesign.isNotBlank()) append(" • ${order.selectedDesign}")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = FormatUtils.formatMoney(order.totalPrice, order.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        OrderStageProgressStrip(
                            stage = stage,
                            progressFraction = order.trackingProgressFraction()
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = order.orderStatus)
                            StatusBadge(status = "Pay: ${order.paymentStatus}")
                        }

                        if (order.trackingNumber.isNotBlank() || order.courierName.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = extra.electricBlue.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = HKPayIcons.LocalShipping,
                                            contentDescription = null,
                                            tint = extra.cyanGlow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = order.courierName.ifBlank { "Courier Assigned" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (order.trackingNumber.isNotBlank()) {
                                        Text(
                                            text = order.trackingNumber,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = extra.cyanGlow,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ordered ${FormatUtils.formatDateTime(order.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "View Timeline →",
                                style = MaterialTheme.typography.labelSmall,
                                color = extra.cyanGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStageSummaryTile(
    label: String,
    count: Int,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) tint.copy(alpha = 0.18f) else extra.glassSurfaceElevated,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) tint else extra.glassBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun OrderStageProgressStrip(
    stage: OrderTrackStage,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val extra = LocalHKPayColors.current
    val stages = listOf(
        OrderTrackStage.PLACED,
        OrderTrackStage.PROCESSING,
        OrderTrackStage.SHIPPED,
        OrderTrackStage.DELIVERED
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "orderStageProgress"
    )
    val barColor = when (stage) {
        OrderTrackStage.DELIVERED -> extra.success
        OrderTrackStage.SHIPPED -> extra.cyanGlow
        OrderTrackStage.PROCESSING -> extra.warning
        OrderTrackStage.CANCELLED -> extra.danger
        OrderTrackStage.PLACED -> extra.electricBlue
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            strokeCap = StrokeCap.Round
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEach { milestone ->
                val isReached = stage != OrderTrackStage.CANCELLED && stage.stepIndex >= milestone.stepIndex
                val isCurrent = stage == milestone
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> barColor
                                    isReached -> extra.cyanGlow
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                }
                            )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = milestone.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> barColor
                            isReached -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(
    order: CardOrder?,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onPayNow: (String) -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Order Tracking",
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
            if (order == null) {
                Box(Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = HKPayIcons.LocalShipping,
                        title = "Order Not Found",
                        subtitle = "This order may have been removed or is still syncing.",
                        actionLabel = "Back to Orders",
                        onAction = onBack
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        order.packageName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${order.cardType}${if (order.network.isNotBlank()) " • ${order.network}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extra.cyanGlow
                                    )
                                }
                                StatusBadge(status = order.orderStatus)
                            }
                            Spacer(Modifier.height(10.dp))
                            CopyableChip(
                                label = "Order ID",
                                value = order.orderId,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(order.orderId))
                                    onCopied("Order ID copied")
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(10.dp))
                            PriceLine("Total Amount", FormatUtils.formatMoney(order.totalPrice, order.currency))
                            if (order.selectedDesign.isNotBlank()) {
                                PriceLine("Card Design", order.selectedDesign)
                            }
                            if (order.referralDiscountApplied > 0.0) {
                                PriceLine("Referral Credit Applied", "-${FormatUtils.formatMoney(order.referralDiscountApplied, order.currency)}")
                            }
                            PriceLine("Payment Status", order.paymentStatus)
                            if (order.paymentMethod.isNotBlank()) {
                                PriceLine("Payment Method", order.paymentMethod)
                            }
                            if (order.paymentReference.isNotBlank()) {
                                PriceLine("Payment Reference", order.paymentReference)
                            }
                            PriceLine("Order Date", FormatUtils.formatDateTime(order.createdAt))
                            if (order.estimatedDelivery.isNotBlank()) {
                                PriceLine("Estimated Delivery", order.estimatedDelivery)
                            }

                            if (order.paymentStatus.equals("Unpaid", ignoreCase = true) ||
                                order.paymentStatus.equals("Rejected", ignoreCase = true)
                            ) {
                                Spacer(Modifier.height(14.dp))
                                HKPayPrimaryButton(
                                    text = "Complete Payment Now",
                                    onClick = { onPayNow(order.orderId) },
                                    icon = HKPayIcons.Payment,
                                    testTag = "order_detail_pay_button"
                                )
                            }
                        }
                    }

                    item {
                        GlassCard {
                            Text(
                                "Delivery & Courier Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            PriceLine("Recipient", order.customerName)
                            PriceLine("Phone", order.phoneNumber)
                            PriceLine(
                                "Address",
                                listOf(order.address, order.cityUpazila, order.district, order.country, order.postalCode)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                            )
                            PriceLine("Courier Name", order.courierName.ifBlank { "Assigned after card issue" })
                            if (order.trackingNumber.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                CopyableChip(
                                    label = "Tracking",
                                    value = order.trackingNumber,
                                    onCopy = {
                                        clipboard.setText(AnnotatedString(order.trackingNumber))
                                        onCopied("Tracking number copied")
                                    }
                                )
                            }
                        }
                    }

                    item {
                        GlassCard {
                            Text(
                                "Order Status Timeline",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            OrderStageProgressStrip(
                                stage = order.resolvedTrackStage(),
                                progressFraction = order.trackingProgressFraction()
                            )
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(12.dp))

                            val isTerminalAlt = order.orderStatus in listOf("Cancelled", "Refunded")
                            val currentIdx = HKPayConstants.ORDER_STATUS_STEPS.indexOfFirst {
                                it.equals(order.orderStatus, ignoreCase = true)
                            }

                            HKPayConstants.ORDER_STATUS_STEPS.forEachIndexed { index, stepName ->
                                val reached = !isTerminalAlt && currentIdx >= index
                                val eventMatch = order.timeline.lastOrNull { it.status.equals(stepName, ignoreCase = true) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (reached) Icons.Rounded.CheckCircle else HKPayIcons.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (reached) extra.cyanGlow else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        if (index < HKPayConstants.ORDER_STATUS_STEPS.lastIndex) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(22.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (currentIdx > index && !isTerminalAlt) extra.cyanGlow.copy(alpha = 0.6f)
                                                        else MaterialTheme.colorScheme.outlineVariant
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stepName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (eventMatch != null) {
                                            if (eventMatch.note.isNotBlank()) {
                                                Text(
                                                    text = eventMatch.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (eventMatch.timestamp > 0L) {
                                                Text(
                                                    text = FormatUtils.formatDateTime(eventMatch.timestamp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = extra.cyanGlow
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (isTerminalAlt) {
                                Spacer(Modifier.height(8.dp))
                                StatusBadge(status = order.orderStatus)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
