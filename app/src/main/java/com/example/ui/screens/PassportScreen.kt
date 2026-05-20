package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.CreditSafetyCheck
import com.example.ImageItem
import com.example.MainViewModel
import com.example.PassportPreset
import com.example.Presets
import kotlinx.coroutines.delay
import java.io.File
import java.net.URLEncoder
import kotlin.ranges.coerceIn
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun PassportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    systemDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Determine current color theme
    val isDark = when (uiState.appTheme) {
        "light" -> false
        "dark" -> true
        else -> systemDarkTheme
    }

    // Dynamic Color Palette for high fidelity visual feedback
    val primaryColor = if (isDark) Color(0xFF9E84FF) else Color(0xFF5E35B1)
    val secondaryColor = if (isDark) Color(0xFF4DB6AC) else Color(0xFF00796B)
    val accentColor = if (isDark) Color(0xFFFF4081) else Color(0xFFC2185B)
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF12101F), Color(0xFF1A182E))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF3F1FB), Color(0xFFE8E5F7))
        )
    }
    
    val surfaceColor = if (isDark) Color(0xFF211E39) else Color(0xFFFFFFFF)
    val onSurfaceColor = if (isDark) Color(0xFFE5E2FA) else Color(0xFF262147)
    val textMutedColor = if (isDark) Color(0xFFAFA9D1) else Color(0xFF6F6894)
    val borderColor = if (isDark) Color(0xFF3B365C) else Color(0xFFDBD6F2)

    // Clear Toast handler
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(3500)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // 👤 Header with clickable title and GitHub icons
            HeaderSection(
                isDark = isDark,
                appTheme = uiState.appTheme,
                onThemeSelected = { viewModel.setTheme(it) },
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor
            )

            // Main body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Sparkle Alert Box
                    GradientCard(
                        gradientColors = listOf(primaryColor, secondaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sparkle",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Choose a preset, customize sizes, border lines, layout alignments, and generate print sheets effortlessly! Built by DevixOP 🚀",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upload & Photos Dashboard
                    PhotosSection(
                        images = uiState.images,
                        onAddPhotos = { uris -> viewModel.addPhotos(context, uris) },
                        onRemovePhoto = { uri -> viewModel.removePhoto(uri) },
                        onEditCopies = { uri, count -> viewModel.updateCopies(uri, count) },
                        onStartCrop = { uri -> viewModel.startCropping(uri) },
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        textMutedColor = textMutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Countries Selector
                    PresetsSection(
                        selectedPreset = uiState.chosenPreset,
                        onPresetSelected = { viewModel.selectPreset(it) },
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        primaryColor = primaryColor,
                        textMutedColor = textMutedColor,
                        borderColor = borderColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Advanced Control Settings (Sizes, spaces, border)
                    AdvancedSettingsSection(
                        widthMm = uiState.widthMm,
                        heightMm = uiState.heightMm,
                        spacingMm = uiState.spacingMm,
                        borderMm = uiState.borderMm,
                        layoutMode = uiState.layoutMode,
                        gridAlignment = uiState.gridAlignment,
                        onWidthChange = { viewModel.setWidthMm(it) },
                        onHeightChange = { viewModel.setHeightMm(it) },
                        onSpacingChange = { viewModel.setSpacingMm(it) },
                        onBorderChange = { viewModel.setBorderMm(it) },
                        onLayoutModeChange = { viewModel.setLayoutMode(it) },
                        onGridAlignmentChange = { viewModel.setGridAlignment(it) },
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        textMutedColor = textMutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic Live Canvas Sheet Preview
                    PreviewSheetSection(
                        images = uiState.images,
                        widthMm = uiState.widthMm,
                        heightMm = uiState.heightMm,
                        spacingMm = uiState.spacingMm,
                        borderMm = uiState.borderMm,
                        layoutMode = uiState.layoutMode,
                        gridAlignment = uiState.gridAlignment,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        borderColor = borderColor,
                        textMutedColor = textMutedColor,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Action Buttons
                    ActionButtonsSection(
                        isGenerating = uiState.isGeneratingPdf,
                        pdfFile = uiState.generatedPdfFile,
                        onGenerate = { viewModel.generatePrintPdf(context) },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer / DevixOP clickable credits
                    FooterSection(
                        onOpenFeedback = { viewModel.setFeedbackModalOpen(true) },
                        textMutedColor = textMutedColor,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        // Animated Glass Toast Message popups
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            uiState.toastMessage?.let { msg ->
                ToastMessage(
                    text = msg,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }
    }

    // Modal view: Zoom / Pan Aspect Cropper
    if (uiState.activeCroppingUri != null) {
        val uri = uiState.activeCroppingUri!!
        val imageItem = uiState.images.find { it.uri == uri }
        if (imageItem != null) {
            CropDialog(
                imageItem = imageItem,
                targetAspect = uiState.widthMm / uiState.heightMm,
                onCancel = { viewModel.cancelCropping() },
                onSave = { cropped -> viewModel.saveCrop(uri, cropped) },
                isDark = isDark,
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor
            )
        }
    }

    // Modal view: Feedback and Bugs to @WTF_Phantom on Telegram
    if (uiState.isFeedbackModalOpen) {
        FeedbackDialog(
            onDismiss = { viewModel.setFeedbackModalOpen(false) },
            isDark = isDark,
            primaryColor = primaryColor,
            surfaceColor = surfaceColor,
            onSurfaceColor = onSurfaceColor,
            textMutedColor = textMutedColor
        )
    }
}

// ==================== COMPONENTS ====================

@Composable
fun HeaderSection(
    isDark: Boolean,
    appTheme: String,
    onThemeSelected: (String) -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    val context = LocalContext.current
    
    // Safety Credits verification on click or draw
    LaunchedEffect(Unit) {
        CreditSafetyCheck.performIntegrityCheck()
    }

    Surface(
        color = surfaceColor.copy(alpha = 0.85f),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Premium subtle bottom line gradient
                val borderHeight = 2.dp.toPx()
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, Color(0xFF00E676))
                    ),
                    topLeft = Offset(0f, size.height - borderHeight),
                    size = Size(size.width, borderHeight)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logotype
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor, Color(0xFF00E676))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Passport Photo Pro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "By ${CreditSafetyCheck.AUTHOR}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme toggle
                IconButton(
                    onClick = {
                        val next = when (appTheme) {
                            "system" -> "light"
                            "light" -> "dark"
                            else -> "system"
                        }
                        onThemeSelected(next)
                    }
                ) {
                    val themeIcon = when (appTheme) {
                        "light" -> Icons.Default.LightMode
                        "dark" -> Icons.Default.DarkMode
                        else -> Icons.Default.SettingsSuggest
                    }
                    Icon(
                        imageVector = themeIcon,
                        contentDescription = "Theme selection",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // GitHub icon - clickable to link DevixOP
                IconButton(
                    modifier = Modifier.testTag("github_link"),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CreditSafetyCheck.GITHUB_LINK))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore fallback
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, // Represents link to code
                        contentDescription = "GitHub DevixOP Profile",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PhotosSection(
    images: List<ImageItem>,
    onAddPhotos: (List<Uri>) -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onEditCopies: (Uri, Int) -> Unit,
    onStartCrop: (Uri) -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    textMutedColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    val context = LocalContext.current
    
    // Multiple Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onAddPhotos(uris)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📸 Photo Upload Pool (${images.size}/10)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Upload photos, adjust individual print copies, and crop to exact templates.",
                fontSize = 11.sp,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Drag/Click Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.06f))
                    .clickable { galleryLauncher.launch("image/*") }
                    .drawBehind {
                        // Dash Border Paint
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.5f),
                            style = stroke,
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload trigger icon",
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap to choose images from gallery (max 10)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = "Supports JPEG, PNG, WEBP formats",
                        fontSize = 9.sp,
                        color = textMutedColor
                    )
                }
            }

            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Horizontal list of uploaded images
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    images.forEach { item ->
                        ImageCard(
                            imageItem = item,
                            onRemove = { onRemovePhoto(item.uri) },
                            onEditCopies = { onEditCopies(item.uri, it) },
                            onStartCrop = { onStartCrop(item.uri) },
                            surfaceColor = surfaceColor,
                            onSurfaceColor = onSurfaceColor,
                            borderColor = borderColor,
                            primaryColor = primaryColor,
                            textMutedColor = textMutedColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCard(
    imageItem: ImageItem,
    onRemove: () -> Unit,
    onEditCopies: (Int) -> Unit,
    onStartCrop: () -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    borderColor: Color,
    primaryColor: Color,
    textMutedColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cropped version view
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Image(
                    bitmap = imageItem.croppedBitmap.asImageBitmap(),
                    contentDescription = "Active view preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Crop overlay indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = "Cropped state indicator",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Copies count: ${imageItem.copies}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = onSurfaceColor
                    )

                    // Manual simple buttons to adjust copies
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onEditCopies(imageItem.copies - 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement copies",
                                tint = primaryColor
                            )
                        }

                        Text(
                            text = imageItem.copies.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(18.dp)
                        )

                        IconButton(
                            onClick = { onEditCopies(imageItem.copies + 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment copies",
                                tint = primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                
                // Copies Slider (1-20)
                Slider(
                    value = imageItem.copies.toFloat(),
                    onValueChange = { onEditCopies(it.roundToInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        activeTrackColor = primaryColor,
                        thumbColor = primaryColor
                    ),
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Crop Button
                    OutlinedButton(
                        onClick = onStartCrop,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        border = BorderStroke(1.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crop Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Remove Button
                    OutlinedButton(
                        onClick = onRemove,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetsSection(
    selectedPreset: PassportPreset,
    onPresetSelected: (PassportPreset) -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    primaryColor: Color,
    textMutedColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📏 Preset Dimensions (Includes Indian Sizes 🇮🇳)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select a regional layout index. India presets cover mini, small, card, and OCI square sizes.",
                fontSize = 11.sp,
                color = textMutedColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally scrolled presets categorized
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(Presets.list) { item ->
                    val isSelected = selectedPreset.name == item.name && selectedPreset.country == item.country
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPresetSelected(item) },
                        color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.08f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) primaryColor else borderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.flag,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${item.country.split(" ").firstOrNull() ?: ""} ${item.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else onSurfaceColor
                                )
                                Text(
                                    text = "${item.widthMm.toInt()}x${item.heightMm.toInt()} mm",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else textMutedColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedSettingsSection(
    widthMm: Float,
    heightMm: Float,
    spacingMm: Float,
    borderMm: Float,
    layoutMode: String,
    gridAlignment: String,
    onWidthChange: (Float) -> Unit,
    onHeightChange: (Float) -> Unit,
    onSpacingChange: (Float) -> Unit,
    onBorderChange: (Float) -> Unit,
    onLayoutModeChange: (String) -> Unit,
    onGridAlignmentChange: (String) -> Unit,
    surfaceColor: Color,
    onSurfaceColor: Color,
    textMutedColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚙️ Layout & Calibration Panel",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dynamically fine-tune photo sizing parameters and margins before compiling.",
                fontSize = 11.sp,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Width & Height mm Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Width
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Photo Width", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Text("${widthMm.toInt()} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    Slider(
                        value = widthMm,
                        onValueChange = onWidthChange,
                        valueRange = 20f..80f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor, thumbColor = primaryColor),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Height
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Photo Height", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Text("${heightMm.toInt()} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    Slider(
                        value = heightMm,
                        onValueChange = onHeightChange,
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor, thumbColor = primaryColor),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Spacing & Border thin outline Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Spacing
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gap Spacing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Text("${spacingMm.roundToInt()} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    Slider(
                        value = spacingMm,
                        onValueChange = onSpacingChange,
                        valueRange = 1f..15f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor, thumbColor = primaryColor),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Border Size
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gray Border Line", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Text("${"%.1f".format(borderMm)} mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    Slider(
                        value = borderMm,
                        onValueChange = onBorderChange,
                        valueRange = 0f..3f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor, thumbColor = primaryColor),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = borderColor)

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Grid vs Line arrangement selection
            Text(
                text = "Arrangement Mode",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Grid Trigger Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLayoutModeChange("grid") },
                    colors = CardDefaults.cardColors(
                        containerColor = if (layoutMode == "grid") primaryColor.copy(alpha = 0.12f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (layoutMode == "grid") primaryColor else borderColor
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = if (layoutMode == "grid") primaryColor else textMutedColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("3xN Grid", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            Text("3 cols, left-aligned", fontSize = 9.sp, color = textMutedColor)
                        }
                    }
                }

                // Line Wrap Trigger Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLayoutModeChange("line") },
                    colors = CardDefaults.cardColors(
                        containerColor = if (layoutMode == "line") primaryColor.copy(alpha = 0.12f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (layoutMode == "line") primaryColor else borderColor
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignLeft,
                            contentDescription = null,
                            tint = if (layoutMode == "line") primaryColor else textMutedColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Line-By-Line", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            Text("Fills row completely", fontSize = 9.sp, color = textMutedColor)
                        }
                    }
                }
            }

            // Grid Alignment customizable blank sections options
            if (layoutMode == "grid") {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "📐 Grid Column Blank Alignments",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = "Force specific columns or cells to stay blank to fit physical stickers.",
                    fontSize = 10.sp,
                    color = textMutedColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Alignments flowchips
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val alignmentList = listOf(
                        "normal" to "Standard Fill Full Grid",
                        "right_blank" to "Keep Right Column Blank (Col 3)",
                        "left_blank" to "Keep Left Column Blank (Col 1)",
                        "lower_right_blank" to "Keep Lower Right Blank (Bottom right)",
                        "lower_left_blank" to "Keep Lower Left Blank (Bottom left)"
                    )

                    alignmentList.forEach { (key, title) ->
                        val isSel = gridAlignment == key
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onGridAlignmentChange(key) },
                            color = if (isSel) primaryColor.copy(alpha = 0.08f) else Color.Transparent,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSel) primaryColor else borderColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { onGridAlignmentChange(key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewSheetSection(
    images: List<ImageItem>,
    widthMm: Float,
    heightMm: Float,
    spacingMm: Float,
    borderMm: Float,
    layoutMode: String,
    gridAlignment: String,
    surfaceColor: Color,
    onSurfaceColor: Color,
    borderColor: Color,
    textMutedColor: Color,
    primaryColor: Color
) {
    // Generate total individual bitmaps sequence
    val previewCopies = mutableListOf<Bitmap>()
    for (img in images) {
        repeat(img.copies) {
            previewCopies.add(img.croppedBitmap)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📋 Live A4 Page Layout Simulator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "Demonstrates how items align onto paper sheets.",
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                }

                Box(
                    modifier = Modifier
                        .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "A4 PORTRAIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // High Fidelity simulated interactive render of Page 1
            if (previewCopies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = textMutedColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add photos to see layout sheet preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMutedColor
                        )
                    }
                }
            } else {
                // Calculate columns and rows fitting Page 1 to model visually
                Box(
                    modifier = Modifier
                        .width(230.dp) // Maintain A4 Aspect Ratio 1:1.414 (230dp wide -> 325dp high)
                        .height(325.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Safe bounds calculations
                        val cvWidth = size.width
                        val cvHeight = size.height

                        // Scaling ratio from real A4 (210mm x 297mm) to Canvas dimensions
                        val scale = cvWidth / 210f

                        val drawWidth = widthMm * scale
                        val drawHeight = heightMm * scale
                        val drawSpacing = spacingMm * scale
                        val drawBorder = borderMm * scale
                        val drawMargin = 15f * scale

                        val printableW = cvWidth - 2 * drawMargin
                        val printableH = cvHeight - 2 * drawMargin

                        var globalIdx = 0

                        if (layoutMode == "grid") {
                            val colCount = 3
                            val gridTotalW = colCount * drawWidth + (colCount - 1) * drawSpacing
                            val startX = drawMargin + (printableW - gridTotalW) / 2

                            val rowHeight = drawHeight + drawSpacing
                            val maxVisualRows = floor((printableH - 12f + drawSpacing) / rowHeight).toInt().coerceAtLeast(1)

                            for (r in 0 until maxVisualRows) {
                                for (c in 0 until colCount) {
                                    if (globalIdx >= previewCopies.size) break

                                    // Skip masked cells based on alignment
                                    var isMasked = false
                                    when (gridAlignment) {
                                        "right_blank" -> if (c == 2) isMasked = true
                                        "left_blank" -> if (c == 0) isMasked = true
                                        "lower_left_blank" -> if (r == maxVisualRows - 1 && c == 0) isMasked = true
                                        "lower_right_blank" -> if (r == maxVisualRows - 1 && c == 2) isMasked = true
                                    }

                                    if (isMasked) {
                                        // Draw a transparent slot box to visually show alignment blank helper
                                        drawRoundRect(
                                            color = Color.LightGray.copy(alpha = 0.4f),
                                            topLeft = Offset(startX + c * (drawWidth + drawSpacing), drawMargin + r * (drawHeight + drawSpacing)),
                                            size = Size(drawWidth, drawHeight),
                                            cornerRadius = CornerRadius(4f, 4f),
                                            style = Stroke(
                                                width = 1f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                            )
                                        )
                                        continue
                                    }

                                    val cellX = startX + c * (drawWidth + drawSpacing)
                                    val cellY = drawMargin + r * (drawHeight + drawSpacing)

                                    // Draw simulated filled picture box
                                    drawRoundRect(
                                        color = primaryColor.copy(alpha = 0.25f),
                                        topLeft = Offset(cellX, cellY),
                                        size = Size(drawWidth, drawHeight),
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )

                                    // Custom user borders (RGB: 180,180,180) thin border indicator
                                    if (drawBorder > 0f) {
                                        drawRect(
                                            color = Color(180, 180, 180),
                                            topLeft = Offset(cellX, cellY),
                                            size = Size(drawWidth, drawHeight),
                                            style = Stroke(width = drawBorder.coerceAtLeast(1f))
                                        )
                                    }

                                    globalIdx++
                                }
                            }
                        } else {
                            // Line layout: auto fill rows completely
                            val colsPerRow = floor((printableW + drawSpacing) / (drawWidth + drawSpacing)).toInt().coerceAtLeast(1)
                            val rowHeight = drawHeight + drawSpacing
                            val maxVisualRows = floor((printableH - 12f + drawSpacing) / rowHeight).toInt().coerceAtLeast(1)

                            val lineTotalW = colsPerRow * drawWidth + (colsPerRow - 1) * drawSpacing
                            val startX = drawMargin + (printableW - lineTotalW) / 2

                            for (r in 0 until maxVisualRows) {
                                for (c in 0 until colsPerRow) {
                                    if (globalIdx >= previewCopies.size) break

                                    val cellX = startX + c * (drawWidth + drawSpacing)
                                    val cellY = drawMargin + r * (drawHeight + drawSpacing)

                                    // Draw simulated picture
                                    drawRoundRect(
                                        color = primaryColor.copy(alpha = 0.25f),
                                        topLeft = Offset(cellX, cellY),
                                        size = Size(drawWidth, drawHeight),
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )

                                    if (drawBorder > 0f) {
                                        drawRect(
                                            color = Color(180, 180, 180),
                                            topLeft = Offset(cellX, cellY),
                                            size = Size(drawWidth, drawHeight),
                                            style = Stroke(width = drawBorder.coerceAtLeast(1f))
                                        )
                                    }

                                    globalIdx++
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                
                // Pages allocation status
                val calculatedTotalSlotsOnOnePage = if (layoutMode == "grid") {
                    val colCount = 3
                    val scale = 230f / 210f
                    val drawHeight = heightMm * scale
                    val drawSpacing = spacingMm * scale
                    val drawMargin = 15f * scale
                    val printableH = 325f - 2 * drawMargin
                    val maxRows = floor((printableH - 12f + drawSpacing) / (drawHeight + drawSpacing)).toInt().coerceAtLeast(1)
                    var count = maxRows * colCount
                    when (gridAlignment) {
                        "right_blank" -> count -= maxRows
                        "left_blank" -> count -= maxRows
                        "lower_right_blank" -> count -= 1
                        "lower_left_blank" -> count -= 1
                    }
                    count.coerceAtLeast(1)
                } else {
                    val scale = 230f / 210f
                    val drawWidth = widthMm * scale
                    val drawHeight = heightMm * scale
                    val drawSpacing = spacingMm * scale
                    val drawMargin = 15f * scale
                    val printableW = 230f - 2 * drawMargin
                    val printableH = 325f - 2 * drawMargin
                    val colsPerRow = floor((printableW + drawSpacing) / (drawWidth + drawSpacing)).toInt().coerceAtLeast(1)
                    val maxRows = floor((printableH - 12f + drawSpacing) / (drawHeight + drawSpacing)).toInt().coerceAtLeast(1)
                    (maxRows * colsPerRow).coerceAtLeast(1)
                }

                val neededPages = (previewCopies.size.toFloat() / calculatedTotalSlotsOnOnePage).let { kotlin.math.ceil(it).toInt() }.coerceAtLeast(1)

                Text(
                    text = "Page 1 of $neededPages (${previewCopies.size} Total Photo Slots)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = primaryColor
                )
                Text(
                    text = "If layout overflows Page 1 boundaries, extra PDF pages compile automatically.",
                    fontSize = 10.sp,
                    color = textMutedColor
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    isGenerating: Boolean,
    pdfFile: File?,
    onGenerate: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gradient Build / Compile Button
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_button")
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(primaryColor, Color(0xFF00E676))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Print-Ready PDF Sheet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Expanded PDF triggers: View and Share
        if (pdfFile != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Open File
                Button(
                    onClick = {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "com.aistudio.passportphotopro.pwtrqz.fileprovider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handler simple toast
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Sheet PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Share Intent
                Button(
                    onClick = {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "com.aistudio.passportphotopro.pwtrqz.fileprovider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Passport Photo PDF"))
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Print PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FooterSection(
    onOpenFeedback: () -> Unit,
    textMutedColor: Color,
    primaryColor: Color
) {
    val context = LocalContext.current
    
    // Safety verification check on draw
    LaunchedEffect(Unit) {
        CreditSafetyCheck.performIntegrityCheck()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Author signature trademark - REQUIRED BY CREDIT PROTECTION SYSTEM
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CreditSafetyCheck.GITHUB_LINK))
                context.startActivity(intent)
            }
        ) {
            Text(
                text = "Passport Photo Pro © 2026",
                fontSize = 11.sp,
                color = textMutedColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "• ${CreditSafetyCheck.FOOTER_CREDIT}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Telegram Feedback link and support redirection button
        OutlinedButton(
            onClick = onOpenFeedback,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Submit Feedback / Bug Report",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

// Dialog: Cropping with interactive Zoom / Pan parameters
@Composable
fun CropDialog(
    imageItem: ImageItem,
    targetAspect: Float, // width / height
    onCancel: () -> Unit,
    onSave: (Bitmap) -> Unit,
    isDark: Boolean,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    var zoom by remember { mutableStateOf(1.0f) }
    var panXPct by remember { mutableStateOf(0.0f) } // -50% to +50% of offset
    var panYPct by remember { mutableStateOf(0.0f) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = surfaceColor,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crop Passport Shape",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = onSurfaceColor
                    )
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close modal", tint = onSurfaceColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cropping Box simulation container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Raw original bitmap drawn
                    Image(
                        bitmap = imageItem.sourceBitmap.asImageBitmap(),
                        contentDescription = "Original photo source",
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                // Draw a beautiful semi-transparent cropping frame guidelines reflecting targeted aspect ration
                                val boxW: Float
                                val boxH: Float
                                val viewportRatio = size.width / size.height

                                if (viewportRatio > targetAspect) {
                                    boxH = size.height * 0.85f
                                    boxW = boxH * targetAspect
                                } else {
                                    boxW = size.width * 0.85f
                                    boxH = boxW / targetAspect
                                }

                                val left = (size.width - boxW) / 2
                                val top = (size.height - boxH) / 2

                                // Darken background exterior of crop box
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.45f)
                                )
                                // Clear crop box region
                                drawRect(
                                    color = Color.Transparent,
                                    topLeft = Offset(left, top),
                                    size = Size(boxW, boxH),
                                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                )
                                // Draw golden outline boundary
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(left, top),
                                    size = Size(boxW, boxH),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calibration Sliders: Zoom
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scale Zoom", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                        Text("${zoom}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    Slider(
                        value = zoom,
                        onValueChange = { zoom = it },
                        valueRange = 1.0f..3.0f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor)
                    )
                }

                // Horizontal Offset
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pan X Position", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                    }
                    Slider(
                        value = panXPct,
                        onValueChange = { panXPct = it },
                        valueRange = -0.5f..0.5f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor)
                    )
                }

                // Vertical Offset
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pan Y Position", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                    }
                    Slider(
                        value = panYPct,
                        onValueChange = { panYPct = it },
                        valueRange = -0.5f..0.5f,
                        colors = SliderDefaults.colors(activeTrackColor = primaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val cropped = cropBitmapProgrammatically(
                                imageItem.sourceBitmap,
                                targetAspect,
                                zoom,
                                panXPct,
                                panYPct
                            )
                            onSave(cropped)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Apply Crop")
                    }
                }
            }
        }
    }
}

// Programmatic bitmap crop executor
fun cropBitmapProgrammatically(
    src: Bitmap,
    targetRatio: Float,
    zoom: Float,
    panX: Float,
    panY: Float
): Bitmap {
    val srcW = src.width.toFloat()
    val srcH = src.height.toFloat()

    // Base layout coordinates
    val baseCropW: Float
    val baseCropH: Float
    
    if (srcW / srcH > targetRatio) {
        baseCropH = srcH
        baseCropW = srcH * targetRatio
    } else {
        baseCropW = srcW
        baseCropH = srcW / targetRatio
    }

    // Apply Zoom factor downscaling dimensions
    val finalCropW = baseCropW / zoom
    val finalCropH = baseCropH / zoom

    // Shift centers based on pan percentages
    val centerShiftX = (srcW - finalCropW) / 2f
    val centerShiftY = (srcH - finalCropH) / 2f

    val finalX = (centerShiftX + panX * (srcW - finalCropW)).coerceIn(0f, srcW - finalCropW).toInt()
    val finalY = (centerShiftY + panY * (srcH - finalCropH)).coerceIn(0f, srcH - finalCropH).toInt()

    return try {
        Bitmap.createBitmap(src, finalX, finalY, finalCropW.toInt().coerceAtLeast(1), finalCropH.toInt().coerceAtLeast(1))
    } catch (e: Exception) {
        src
    }
}

// Dialog: Feedback form triggering redirect open Telegram with URL params
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    isDark: Boolean,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    textMutedColor: Color
) {
    val context = LocalContext.current
    var textMessage by remember { mutableStateOf("") }

    // Critical assertion
    LaunchedEffect(Unit) {
        CreditSafetyCheck.performIntegrityCheck()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = surfaceColor,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐞 Feedback & Bug Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = onSurfaceColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = onSurfaceColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Submit a bug report or suggested enhancement directly to @${CreditSafetyCheck.TELEGRAM_USER} on Telegram.",
                    fontSize = 11.sp,
                    color = textMutedColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = { Text("Describe the bug or feature request here...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = primaryColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = primaryColor.copy(alpha = 0.05f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (textMessage.isNotBlank()) {
                                try {
                                    val formattedPayload = URLEncoder.encode(
                                        "Passport Photo Pro Feedback:\n\n$textMessage",
                                        "UTF-8"
                                    )
                                    val telegramIntentUrl = "https://t.me/${CreditSafetyCheck.TELEGRAM_USER}?text=$formattedPayload"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramIntentUrl))
                                    context.startActivity(intent)
                                    onDismiss()
                                } catch (e: Exception) {
                                    // ignore 
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = textMessage.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ToastMessage(
    text: String,
    primaryColor: Color,
    secondaryColor: Color
) {
    Surface(
        color = Color(0xEB1C172E),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(colors = listOf(primaryColor, secondaryColor))
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GradientCard(
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradientColors))
    ) {
        content()
    }
}
