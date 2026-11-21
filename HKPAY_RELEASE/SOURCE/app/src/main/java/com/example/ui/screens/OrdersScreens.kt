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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CardOrder
import com.example.data.model.HKPayConstants
import com.example.ui.components.CopyableChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun OrdersScreen(
    state: MainDashboardState,
    onSelectOrder: (String) -> Unit,
    onNavigateStore: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "In Progress", "Unpaid", "Active", "Cancelled")

    val filteredOrders = remember(state.orders, filter) {
        when (filter) {
            "Unpaid" -> state.orders.filter { it.paymentStatus.equals("Unpaid", ignoreCase = true) }
            "Active" -> state.orders.filter { it.orderStatus.equals("Active", ignoreCase = true) || it.orderStatus.equals("Delivered", ignoreCase = true) }
            "Cancelled" -> state.orders.filter { it.orderStatus in listOf("Cancelled", "Refunded") }
            "In Progress" -> state.orders.filter { it.orderStatus !in listOf("Active", "Delivered", "Cancelled", "Refunded") }
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
                    Text(text = "My Orders & Tracking", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Real-time order progress synced from Firebase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { label ->
                            FilterChip(
                                selected = filter == label,
                                onClick = { filter = label },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.LocalShipping,
                        title = "No Orders Found",
                        subtitle = "Orders you place in the Physical Card Store will track live here.",
                        actionLabel = "Browse Card Store",
                        onAction = onNavigateStore
                    )
                }
            } else {
                items(filteredOrders, key = { it.orderId }) { order ->
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = order.orderId,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow
                                )
                            }
                            Text(
                                text = FormatUtils.formatMoney(order.totalPrice, order.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = order.orderStatus)
                            StatusBadge(status = "Pay: ${order.paymentStatus}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Ordered ${FormatUtils.formatDateTime(order.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(
    order: CardOrder?,
    onBack: () -> Unit,
    onPayNow: (String) -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Order Tracking", onBack = onBack) }
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
                        icon = Icons.Rounded.LocalShipping,
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
                                    Text(order.packageName, style = MaterialTheme.typography.titleLarge)
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
                                    icon = Icons.Rounded.Payment,
                                    testTag = "order_detail_pay_button"
                                )
                            }
                        }
                    }

                    item {
                        GlassCard {
                            Text("Delivery & Courier Information", style = MaterialTheme.typography.titleMedium)
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
                            Text("Order Status Timeline", style = MaterialTheme.typography.titleMedium)
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
                                            imageVector = if (reached) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
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
