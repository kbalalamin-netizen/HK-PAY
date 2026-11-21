package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors

private data class PermissionWizardStep(
    val id: String,
    val title: String,
    val subtitle: String,
    val benefit: String,
    val manifestPermission: String?,
    val icon: ImageVector
)

/**
 * Step-by-step modal popup permission flow (one popup per permission).
 * Complies with Google Play Policy: requests Camera, Coarse Location, Contacts,
 * and Notifications (API 33+), while using Android's zero-permission Photo Picker for Files/Media.
 */
@Composable
fun PermissionStepDialog(
    onBeforeSystemPrompt: () -> Unit,
    onCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val extra = LocalHKPayColors.current

    val steps = remember {
        listOf(
            PermissionWizardStep(
                id = "camera",
                title = "Camera Access",
                subtitle = "Step 1 of 4 • Live NID & Face Liveness",
                benefit = "Used strictly when you scan your NID/Passport or complete live face liveness verification.",
                manifestPermission = Manifest.permission.CAMERA,
                icon = HKPayIcons.CameraAlt
            ),
            PermissionWizardStep(
                id = "location",
                title = "Country & Region Location",
                subtitle = "Step 2 of 4 • Worldwide Auto-Region",
                benefit = "Auto-detects your country and regional card delivery availability without fine GPS tracking.",
                manifestPermission = Manifest.permission.ACCESS_COARSE_LOCATION,
                icon = HKPayIcons.Language
            ),
            PermissionWizardStep(
                id = "contacts",
                title = "Contacts Referral Helper",
                subtitle = "Step 3 of 4 • Optional Friend Invites",
                benefit = "Optional access so you can invite trusted contacts to earn HK PAY card fee referral credits.",
                manifestPermission = Manifest.permission.READ_CONTACTS,
                icon = HKPayIcons.GroupAdd
            ),
            PermissionWizardStep(
                id = "files_notifications",
                title = "Secure Files & Order Alerts",
                subtitle = "Step 4 of 4 • Photo Picker + Notifications",
                benefit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    "Files and payment proofs always use Android's zero-permission Photo Picker (your full gallery is never scanned). Enable notifications for real-time card delivery updates."
                } else {
                    "Files, KYC images, and payment proofs use Android's private zero-permission Photo Picker so your broader device storage stays completely private."
                },
                manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else null,
                icon = HKPayIcons.UploadFile
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentIndex.coerceIn(0, steps.lastIndex)]

    fun advanceOrFinish() {
        if (currentIndex < steps.lastIndex) {
            currentIndex += 1
        } else {
            onCompleted()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        advanceOrFinish()
    }

    val isAlreadyGranted = step.manifestPermission?.let { perm ->
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    } ?: true

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / steps.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = extra.cyanGlow,
                    trackColor = extra.electricBlue.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(extra.electricBlue.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = extra.cyanGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = extra.cyanGlow
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = step.benefit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isAlreadyGranted && step.manifestPermission != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = extra.success.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.success.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Permission already granted on this device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = extra.success,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            HKPayPrimaryButton(
                text = when {
                    step.manifestPermission == null -> "Got It & Finish"
                    isAlreadyGranted -> if (currentIndex == steps.lastIndex) "Finish Setup" else "Continue"
                    else -> "Allow & Continue"
                },
                testTag = "permission_wizard_allow_button",
                onClick = {
                    val perm = step.manifestPermission
                    if (perm != null && !isAlreadyGranted) {
                        onBeforeSystemPrompt()
                        permissionLauncher.launch(perm)
                    } else {
                        advanceOrFinish()
                    }
                }
            )
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { advanceOrFinish() },
                    modifier = Modifier.testTag("permission_wizard_skip_step")
                ) {
                    Text(if (currentIndex == steps.lastIndex) "Close" else "Skip Step")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("permission_wizard_later")
                ) {
                    Text("Later")
                }
            }
        }
    )
}
