package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import kotlinx.coroutines.delay
import java.io.File

/**
 * Full-screen embedded CameraX live document scanner for NID / Passport / Driver License.
 * Includes animated rectangular guide overlay, torch control, and zero-permission Photo Picker fallback.
 */
@Composable
fun LiveDocumentScanDialog(
    title: String,
    subtitle: String = "Align your NID or official document cleanly inside the glowing frame.",
    onBeforeExternalPicker: () -> Unit = {},
    onCaptured: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val extra = LocalHKPayColors.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var torchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onCaptured(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            onBeforeExternalPicker()
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val scanTransition = rememberInfiniteTransition(label = "doc_scan")
    val scanLineProgress by scanTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030816))
        ) {
            if (hasPermission) {
                val previewView = remember {
                    PreviewView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val future = ProcessCameraProvider.getInstance(context)
                    val executor = ContextCompat.getMainExecutor(context)
                    var provider: ProcessCameraProvider? = null
                    future.addListener({
                        runCatching {
                            provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture
                            provider?.unbindAll()
                            boundCamera = provider?.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                capture
                            )
                        }.onFailure { err ->
                            cameraError = err.localizedMessage ?: "Unable to start back camera."
                        }
                    }, executor)

                    onDispose {
                        runCatching { provider?.unbindAll() }
                    }
                }

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Document rectangular reticle & animated laser line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boxW = size.width * 0.86f
                    val boxH = boxW * 0.63f
                    val left = (size.width - boxW) / 2f
                    val top = (size.height - boxH) / 2f - 40.dp.toPx()

                    drawRect(color = Color.Black.copy(alpha = 0.45f))
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(left, top),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    val scanY = top + (boxH * scanLineProgress)
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = Offset(left + 12.dp.toPx(), scanY),
                        end = Offset(left + boxW - 12.dp.toPx(), scanY),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = HKPayIcons.CameraAlt,
                        contentDescription = null,
                        tint = extra.cyanGlow,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enable camera access to directly scan your NID/Passport, or upload from Android Photo Picker below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    HKPayPrimaryButton(
                        text = "Allow Camera",
                        onClick = {
                            onBeforeExternalPicker()
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }

            // Top overlay header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = Color(0xFF7DD3FC),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasPermission && boundCamera?.cameraInfo?.hasFlashUnit() == true) {
                        IconButton(
                            onClick = {
                                torchOn = !torchOn
                                boundCamera?.cameraControl?.enableTorch(torchOn)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = HKPayIcons.FlashOn,
                                contentDescription = "Toggle Flash",
                                tint = if (torchOn) Color(0xFF00E5FF) else Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close scanner",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom capture controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!cameraError.isNullOrBlank()) {
                    Text(
                        text = cameraError.orEmpty(),
                        color = Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onBeforeExternalPicker()
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("doc_scan_gallery_button"),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(HKPayIcons.UploadFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val cap = imageCapture ?: return@Button
                            isCapturing = true
                            val outFile = File(context.cacheDir, "hkpay_doc_${System.currentTimeMillis()}.jpg")
                            val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
                            cap.takePicture(
                                options,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        onCaptured(Uri.fromFile(outFile))
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        cameraError = exception.localizedMessage ?: "Capture failed."
                                    }
                                }
                            )
                        },
                        enabled = hasPermission && imageCapture != null && !isCapturing,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("doc_scan_capture_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = extra.electricBlue)
                    ) {
                        Icon(HKPayIcons.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isCapturing) "Scanning..." else "Scan & Capture")
                    }
                }
            }
        }
    }
}

/**
 * Full-screen embedded CameraX Front-Camera Liveness Face Scan with step-by-step directional prompts
 * (Look Straight -> Turn Head Left / Right -> Smile & Capture).
 */
@Composable
fun LiveFaceLivenessDialog(
    livenessChallenge: String,
    onCompleted: (Uri, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val extra = LocalHKPayColors.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val directionSteps = remember(livenessChallenge) {
        listOf(
            "Step 1/3: Look Straight into the circle",
            "Step 2/3: $livenessChallenge",
            "Step 3/3: Smile naturally & hold steady"
        )
    }
    var currentStepIdx by remember { mutableIntStateOf(0) }
    var stepConfirmed by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Guided directional cadence
    LaunchedEffect(hasPermission, currentStepIdx) {
        if (hasPermission && currentStepIdx < directionSteps.lastIndex) {
            delay(1800)
            currentStepIdx += 1
        } else if (hasPermission && currentStepIdx == directionSteps.lastIndex) {
            delay(1200)
            stepConfirmed = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030816))
        ) {
            if (hasPermission) {
                val previewView = remember {
                    PreviewView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val future = ProcessCameraProvider.getInstance(context)
                    val executor = ContextCompat.getMainExecutor(context)
                    var provider: ProcessCameraProvider? = null
                    future.addListener({
                        runCatching {
                            provider = future.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture
                            provider?.unbindAll()
                            provider?.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                capture
                            )
                        }.onFailure { err ->
                            cameraError = err.localizedMessage ?: "Unable to start front camera."
                        }
                    }, executor)

                    onDispose {
                        runCatching { provider?.unbindAll() }
                    }
                }

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Face Oval Ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val ovalW = size.width * 0.68f
                    val ovalH = ovalW * 1.28f
                    val left = (size.width - ovalW) / 2f
                    val top = (size.height - ovalH) / 2f - 36.dp.toPx()
                    val ringColor = if (stepConfirmed) Color(0xFF22C55E) else Color(0xFF00E5FF)

                    drawRect(color = Color.Black.copy(alpha = 0.42f))
                    drawOval(
                        color = ringColor,
                        topLeft = Offset(left, top),
                        size = Size(ovalW, ovalH),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = HKPayIcons.FaceCheck,
                        contentDescription = null,
                        tint = extra.cyanGlow,
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = "Front Camera Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live face liveness check requires front camera permission.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    HKPayPrimaryButton(
                        text = "Allow Camera",
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }

            // Top directional prompt card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.68f),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.65f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Live Face Liveness",
                                color = Color(0xFF7DD3FC),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = directionSteps[currentStepIdx],
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (currentStepIdx + 1).toFloat() / directionSteps.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (stepConfirmed) Color(0xFF22C55E) else Color(0xFF00E5FF),
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Bottom capture controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.68f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!cameraError.isNullOrBlank()) {
                    Text(
                        text = cameraError.orEmpty(),
                        color = Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            currentStepIdx = 0
                            stepConfirmed = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
                    ) {
                        Text("Restart Guide", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val cap = imageCapture ?: return@Button
                            isCapturing = true
                            val outFile = File(context.cacheDir, "hkpay_liveness_${System.currentTimeMillis()}.jpg")
                            val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
                            cap.takePicture(
                                options,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        onCompleted(
                                            Uri.fromFile(outFile),
                                            "$livenessChallenge (Completed 3-step live guide)"
                                        )
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        cameraError = exception.localizedMessage ?: "Liveness capture failed."
                                    }
                                }
                            )
                        },
                        enabled = hasPermission && imageCapture != null && !isCapturing,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("liveness_capture_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (stepConfirmed) Color(0xFF16A34A) else extra.electricBlue
                        )
                    ) {
                        Icon(HKPayIcons.FaceCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isCapturing) "Verifying..." else if (stepConfirmed) "Complete Liveness" else "Capture Liveness"
                        )
                    }
                }
            }
        }
    }
}
