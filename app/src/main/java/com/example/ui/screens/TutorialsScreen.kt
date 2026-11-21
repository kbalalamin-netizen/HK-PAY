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
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.VerifiedUser
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
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
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
        title = "User Registration & Security",
        summary = "Create your HK PAY profile with your legal name, verify your email address, and receive your unique HKP Member ID and referral code.",
        icon = Icons.Rounded.PersonAdd,
        highlights = listOf(
            "Sign up with Full Name, Email, and optional Referral Code for $5.00 credit",
            "Verify your email link to activate account persistence",
            "Enable Fingerprint / Face Unlock in Profile for instant security"
        ),
        previewTitle = "HKP-94F2A1 • Biometric Ready",
        previewStatus = "Verified",
        previewDetails = "Email verified • 2FA Biometric Lock supported"
    ),
    TutorialGuidePage(
        stepNumber = 2,
        badge = "STEP 2 • CARD PACKAGES",
        title = "Browsing Card Packages",
        summary = "Explore live physical card packages synced directly from Firebase Realtime Database with transparent fees and USD package limits.",
        icon = Icons.Rounded.Storefront,
        highlights = listOf(
            "Compare Card Price, Issuing Fee, Delivery Fee, and Total Price upfront",
            "Swipe physical card finishes in the interactive Card Selection pager",
            "Check live Availability and whether KYC is required before ordering"
        ),
        previewTitle = "Physical Card Store",
        previewStatus = "Available",
        previewDetails = "Card + Issuing + Delivery = Clear Final Price"
    ),
    TutorialGuidePage(
        stepNumber = 3,
        badge = "STEP 3 • ORDER & PAYMENT",
        title = "Card Ordering Process",
        summary = "Select your card design, enter your shipping address, apply any referral fee credits, and pay via bKash, Nagad, Rocket, Bank, or Gateway.",
        icon = Icons.Rounded.AddCard,
        highlights = listOf(
            "Review final price and apply earned Referral Credits before confirming",
            "Upload your payment slip via Android Photo Picker for backend verification",
            "Track all 14 order milestones from Order Placed to Active delivery"
        ),
        previewTitle = "Order ORD-8421 • Courier Tracked",
        previewStatus = "Card Processing",
        previewDetails = "Live 14-step timeline & courier tracking updates"
    ),
    TutorialGuidePage(
        stepNumber = 4,
        badge = "STEP 4 • IDENTITY CHECK",
        title = "KYC Verification",
        summary = "Submit your NID or Passport details once from the KYC page to unlock physical card issuance and delivery.",
        icon = Icons.Rounded.VerifiedUser,
        highlights = listOf(
            "Enter legal name, Date of Birth, country, and NID / Passport number",
            "Upload front, back, and optional selfie securely using Photo Picker",
            "Documents are stored privately per-user in encrypted Firebase Storage"
        ),
        previewTitle = "Identity Verification",
        previewStatus = "Approved",
        previewDetails = "Strict per-user access • Zero-trust Storage rules"
    )
)

@Composable
fun TutorialsScreen(
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
                                        color = extra.cyanGlow
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
                                            tint = extra.cyanGlow,
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
                                                            1 -> Icons.Rounded.Fingerprint
                                                            2 -> Icons.Rounded.CreditCard
                                                            3 -> Icons.Rounded.LocalShipping
                                                            else -> Icons.Rounded.VerifiedUser
                                                        },
                                                        contentDescription = null,
                                                        tint = extra.cyanGlow,
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
                                fontWeight = FontWeight.Bold
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
                                        style = MaterialTheme.typography.bodySmall
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
