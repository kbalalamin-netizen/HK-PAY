package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SupportTicket
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun TransactionHistoryScreen(
    state: MainDashboardState,
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var selectedType by remember { mutableStateOf("All") }
    val types = listOf("All", "Card Order", "Card Issuing", "Payment", "Refund", "Card Funding")

    val filtered = remember(state.transactions, selectedType) {
        if (selectedType == "All") state.transactions
        else state.transactions.filter { it.txType.equals(selectedType, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Transaction History", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(types) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            title = "No Transactions Yet",
                            subtitle = "Your card orders, payments, issuing fees, and refunds appear here."
                        )
                    }
                } else {
                    items(filtered, key = { it.txId }) { tx ->
                        GlassCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.txType, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = tx.txId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extra.cyanGlow
                                    )
                                    if (tx.orderId.isNotBlank()) {
                                        Text(
                                            text = "Order: ${tx.orderId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = FormatUtils.formatDateTime(tx.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatMoney(tx.amount, tx.currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
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
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: MainDashboardState,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onOpenOrder: (String) -> Unit
) {
    val extra = LocalHKPayColors.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Notifications", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.notifications.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Notifications,
                            title = "No Notifications",
                            subtitle = "Order updates, KYC reviews, payment confirmations, and security alerts appear here."
                        )
                    }
                } else {
                    items(state.notifications, key = { it.id }) { notif ->
                        GlassCard(
                            onClick = {
                                if (!notif.isRead) onMarkRead(notif.id)
                                if (notif.referenceId.startsWith("ORD-")) {
                                    onOpenOrder(notif.referenceId)
                                }
                            },
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (!notif.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp, end = 10.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(extra.cyanGlow)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = extra.cyanGlow
                                        )
                                        Text(
                                            text = FormatUtils.formatDateTime(notif.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(notif.title, style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportScreen(
    state: MainDashboardState,
    onBack: () -> Unit,
    onCreateTicket: (String, String, String) -> Unit,
    onSelectTicket: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { subject, category, message ->
                showCreateDialog = false
                onCreateTicket(subject, category, message)
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Customer Support", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = extra.electricBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("support_new_ticket_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create support ticket")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.appSettings.supportEmail.isNotBlank()) {
                    item {
                        GlassCard(contentPadding = PaddingValues(14.dp)) {
                            Text("Support Desk", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${state.appSettings.supportEmail}${if (state.appSettings.supportHours.isNotBlank()) " • ${state.appSettings.supportHours}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = extra.cyanGlow
                            )
                        }
                    }
                }

                if (state.supportTickets.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.SupportAgent,
                            title = "No Support Tickets",
                            subtitle = "Need help with an order, payment, or KYC verification? Tap below to open a ticket.",
                            actionLabel = "Create Support Ticket",
                            onAction = { showCreateDialog = true }
                        )
                    }
                } else {
                    items(state.supportTickets, key = { it.ticketId }) { ticket ->
                        GlassCard(
                            onClick = { onSelectTicket(ticket.ticketId) },
                            modifier = Modifier.testTag("support_ticket_${ticket.ticketId}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ticket.ticketId,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow
                                )
                                StatusBadge(status = ticket.status)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(ticket.subject, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${ticket.category} • Updated ${FormatUtils.formatDateTime(ticket.updatedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Card Order") }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Card Order", "Payment", "KYC", "Card Issue", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Support Ticket") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!error.isNullOrBlank()) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("ticket_message_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        subject.isBlank() -> error = "Subject is required."
                        message.isBlank() -> error = "Please describe your issue."
                        else -> onSubmit(subject, category, message)
                    }
                },
                modifier = Modifier.testTag("ticket_submit_button")
            ) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SupportDetailScreen(
    ticket: SupportTicket?,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onSendReply: (String, String) -> Unit
) {
    val extra = LocalHKPayColors.current
    var replyText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = ticket?.ticketId ?: "Support Ticket", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (ticket == null) {
                Box(Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.SupportAgent,
                        title = "Ticket Not Found",
                        subtitle = "Unable to locate this support ticket.",
                        actionLabel = "Back to Support",
                        onAction = onBack
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                ) {
                    GlassCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ticket.subject, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = ticket.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extra.cyanGlow
                                )
                            }
                            StatusBadge(status = ticket.status)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ticket.replies, key = { it.replyId }) { reply ->
                            val isUser = reply.senderRole.equals("user", ignoreCase = true)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isUser) extra.electricBlue.copy(alpha = 0.22f) else extra.glassSurfaceElevated,
                                    border = BorderStroke(1.dp, if (isUser) extra.cyanGlow.copy(alpha = 0.45f) else extra.glassBorder),
                                    modifier = Modifier.widthIn(max = 310.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isUser) "You" else reply.senderName.ifBlank { "HK PAY Support" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = extra.cyanGlow
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(reply.message, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = FormatUtils.formatDateTime(reply.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        color = extra.glassSurfaceElevated,
                        border = BorderStroke(1.dp, extra.glassBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Write a reply...") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.weight(1f).testTag("ticket_reply_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (replyText.isNotBlank() && !isSubmitting) {
                                        val msg = replyText
                                        replyText = ""
                                        onSendReply(ticket.ticketId, msg)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(extra.electricBlue)
                                    .testTag("ticket_send_reply_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Send reply",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
