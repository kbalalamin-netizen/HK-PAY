package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import kotlinx.coroutines.launch

private data class TutorialGuidePage(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val highlights: List<String>,
    val previewTitle: String,
    val previewStatus: String,
    val previewDetails: String
)

private val TutorialPages = listOf(
    TutorialGuidePage(
        stepNumber = 1,
        badge = "STEP 1 • ACCOUNT SETUP",
        title = "Registration, NID & Liveness",
        summary = "Create your HK PAY account with email verification link, phone number (no OTP needed), NID photos, and a quick live selfie liveness check.",
        icon = HKPayIcons.PersonAdd,
        highlights = listOf(
            "Sign up with Full Name, Email link verification, Phone (No OTP) & optional Referral Code",
            "Upload NID Front & Back photos and capture a live Liveness selfie",
            "Enable Fingerprint / Face Unlock in Profile for instant security"
        ),
        previewTitle = "HKP-94F2A1 • Biometric Ready",
        previewStatus = "Verified",
        previewDetails = "Email verified • NID + Live Selfie captured"
    ),
    TutorialGuidePage(
        stepNumber = 2,
        badge = "STEP 2 • CARD PACKAGES",
        title = "Browsing Card Packages",
        summary = "Explore live physical card packages synced directly from Firebase Realtime Database with transparent fees and USD package limits.",
        icon = HKPayIcons.Storefront,
        highlights = listOf(
            "Compare Card Price, Issuing Fee, Delivery Fee, and Total Price upfront",
            "Swipe physical card finishes in the interactive Card Selection pager",
            "Check live card balances directly on the Dashboard with Show/Hide toggle"
        ),
        previewTitle = "Physical Card Store",
        previewStatus = "Available",
        previewDetails = "Card + Issuing + Delivery = Clear Final Price"
    ),
    TutorialGuidePage(
        stepNumber = 3,
        badge = "STEP 3 • ORDER & PAYMENT",
        title = "Card Ordering Process",
        summary = "Select your card design, enter your shipping address, apply referral credits, and track real-time push notifications as status advances.",
        icon = HKPayIcons.AddCard,
        highlights = listOf(
            "Review final price and apply earned Referral Credits before confirming",
            "Upload your payment slip via Android Photo Picker for verification",
            "Receive personalized FCM alerts as your order advances to delivery"
        ),
        previewTitle = "Order ORD-8421 • Courier Tracked",
        previewStatus = "Card Processing",
        previewDetails = "Live 14-step timeline & courier tracking updates"
    ),
    TutorialGuidePage(
        stepNumber = 4,
        badge = "STEP 4 • IDENTITY CHECK",
        title = "KYC & Activity Tracking",
        summary = "Manage your identity documents and inspect every card transaction and balance movement in one clean dashboard.",
        icon = HKPayIcons.VerifiedUser,
        highlights = listOf(
            "Complete your 13-field Premium Profile for faster physical card dispatch",
            "Switch cleanly between Dark, Light, and Navy themes from any header",
            "Inspect live card activity & history from Firebase Realtime Database"
        ),
        previewTitle = "Identity Verification",
        previewStatus = "Approved",
        previewDetails = "Strict per-user access • Zero-trust Storage rules"
    )
)

@Composable
fun TutorialsScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current
    val pagerState = rememberPagerState(pageCount = { TutorialPages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "HK PAY Tutorials",
                onBack = onBack,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                actions = {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("tutorial_skip_button")
                    ) {
                        Text("Skip", color = extra.cyanGlow)
                    }
                }
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
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                // Step Progress Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TutorialPages.forEachIndexed { index, _ ->
                        val active = pagerState.currentPage == index
                        val dotColor by animateColorAsState(
                            targetValue = if (active) extra.cyanGlow else MaterialTheme.colorScheme.outline,
                            label = "tutorial_dot"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (active) 26.dp else 8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("tutorial_pager")
                ) { pageIndex ->
                    val page = TutorialPages[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Visual Aid Hero Illustration Card
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                extra.cardGradientStart,
                                                extra.cardGradientEnd
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = page.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7BE8FF)
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(extra.electricBlue.copy(alpha = 0.28f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = page.icon,
                                            contentDescription = page.title,
                                            tint = Color(0xFF5CEBFF),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(14.dp))

                                    // Mini UI Diagram Preview inside the Visual Aid
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = when (page.stepNumber) {
                                                            1 -> HKPayIcons.Fingerprint
                                                            2 -> HKPayIcons.CreditCard
                                                            3 -> HKPayIcons.LocalShipping
                                                            else -> HKPayIcons.VerifiedUser
                                                        },
                                                        contentDescription = null,
                                                        tint = Color(0xFF5CEBFF),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = page.previewTitle,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelLarge
                                                    )
                                                }
                                                StatusBadge(status = page.previewStatus)
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = page.previewDetails,
                                                color = Color.White.copy(alpha = 0.78f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Step Explanation Card
                        GlassCard(contentPadding = PaddingValues(18.dp)) {
                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = page.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            page.highlights.forEach { bullet ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = extra.cyanGlow,
                                        modifier = Modifier.padding(top = 2.dp).size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = bullet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Pager Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("tutorial_prev_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("tutorial_bottom_skip_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Skip Guide", textAlign = TextAlign.Center)
                        }
                    }

                    val isLast = pagerState.currentPage == TutorialPages.lastIndex
                    Button(
                        onClick = {
                            if (isLast) {
                                onComplete()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag(if (isLast) "tutorial_complete_button" else "tutorial_next_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extra.electricBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isLast) "Complete Guide" else "Next Step")
                        if (!isLast) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
