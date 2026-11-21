package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils

@Composable
fun ReferralScreen(
    state: MainDashboardState,
    onBack: () -> Unit,
    onClaimCode: (String) -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val myCode = state.userProfile.referralCode.ifBlank { "HKR000000" }
    var friendCodeInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Referral Program", onBack = onBack) }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Referral Banner & Code
                item {
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CardGiftcard,
                                contentDescription = null,
                                tint = extra.cyanGlow
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Invite Friends, Earn $5.00 Fee Credit",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Share your unique referral code. When a friend creates an HK PAY account using your code, you both receive a $5.00 credit toward physical card issuing & delivery fees.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extra.electricBlue.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "YOUR UNIQUE REFERRAL CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = myCode,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    letterSpacing = 2.sp,
                                    color = extra.cyanGlow
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(myCode))
                                    onCopied("Referral code $myCode copied")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("referral_copy_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Copy Code")
                            }
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join HK PAY to order your global physical payment card! Use my referral code $myCode during sign-up to get $5.00 off your card fees."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share HK PAY Referral"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("referral_share_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extra.electricBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Invite")
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
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = FormatUtils.formatMoney(state.availableReferralCredit, "USD"),
                                style = MaterialTheme.typography.headlineSmall,
                                color = extra.success
                            )
                            Text(
                                text = "Available Credit",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = state.referrals.size.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = extra.cyanGlow
                            )
                            Text(
                                text = "Friends Joined",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = FormatUtils.formatMoney(state.totalReferralEarned, "USD"),
                                style = MaterialTheme.typography.headlineSmall,
                                color = extra.electricBlue
                            )
                            Text(
                                text = "Total Earned",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // One-Time Redeem Friend's Code (if user didn't enter one at signup)
                if (state.userProfile.referredBy.isBlank()) {
                    item {
                        GlassCard {
                            Text("Have a Friend's Referral Code?", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Enter a valid referral code once to unlock your $5.00 welcome card fee credit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = friendCodeInput,
                                onValueChange = { friendCodeInput = it.uppercase() },
                                label = { Text("Friend's Referral Code") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("referral_redeem_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            HKPayPrimaryButton(
                                text = "Apply Referral Code",
                                onClick = { onClaimCode(friendCodeInput) },
                                enabled = friendCodeInput.isNotBlank(),
                                isLoading = state.isSubmitting,
                                testTag = "referral_redeem_button"
                            )
                        }
                    }
                } else {
                    item {
                        GlassCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Referred By Code", style = MaterialTheme.typography.titleSmall)
                                StatusBadge(status = state.userProfile.referredByCode.ifBlank { "Applied" })
                            }
                        }
                    }
                }

                // Reward Ledger
                item {
                    Text("Your Referral Rewards", style = MaterialTheme.typography.titleMedium)
                }

                if (state.referralRewards.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.GroupAdd,
                            title = "No Referral Rewards Yet",
                            subtitle = "Share your code $myCode with friends to start earning fee discount credits."
                        )
                    }
                } else {
                    items(state.referralRewards, key = { it.rewardId }) { reward ->
                        GlassCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reward.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${reward.type} • ${FormatUtils.formatDate(reward.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+${FormatUtils.formatMoney(reward.amount, reward.currency)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = extra.success,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    StatusBadge(status = reward.status)
                                }
                            }
                        }
                    }
                }

                if (state.referrals.isNotEmpty()) {
                    item {
                        Text("Referral Sign-Ups", style = MaterialTheme.typography.titleMedium)
                    }
                    items(state.referrals, key = { it.referredUserId }) { ref ->
                        GlassCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ref.referredName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Joined ${FormatUtils.formatDateTime(ref.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = ref.status)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
