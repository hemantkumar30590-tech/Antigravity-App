package com.example.gemini.ui.desktop

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.gemini.AppViewMode
import com.example.gemini.theme.ClaudeTerracotta
import com.example.gemini.theme.GeminiBlue

/**
 * Desktop Split-Pane Workspace for Antigravity (Laptop / DeX / Tablet Mode).
 *
 * Provides a simultaneous multi-pane IDE layout:
 * - Left Pane: Code Editor / Terminal / Browser / Files / Notes / Run
 * - Draggable Center Divider: Adjust split ratio between 25% and 75%
 * - Right Pane: AI Agent Chat / Trajectory / Thought Stream / Artifacts
 */
@Composable
fun DesktopWorkspaceLayout(
    leftPaneMode: AppViewMode,
    onSelectLeftPaneMode: (AppViewMode) -> Unit,
    onToggleSplitMode: () -> Unit,
    leftContent: @Composable (AppViewMode) -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var splitRatio by rememberSaveable { mutableFloatStateOf(0.58f) }
    var isLeftMaximized by rememberSaveable { mutableStateOf(false) }
    var isRightMaximized by rememberSaveable { mutableStateOf(false) }
    var totalWidthPx by remember { mutableFloatStateOf(1f) }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { totalWidthPx = it.width.toFloat().coerceAtLeast(1f) }
    ) {
        if (isLeftMaximized) {
            // Left Pane Fullscreen
            Column(modifier = Modifier.fillMaxSize()) {
                DesktopPaneHeader(
                    activeMode = leftPaneMode,
                    onSelectMode = onSelectLeftPaneMode,
                    isMaximized = true,
                    onToggleMaximize = { isLeftMaximized = false },
                    onToggleSplitMode = onToggleSplitMode
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    leftContent(leftPaneMode)
                }
            }
        } else if (isRightMaximized) {
            // Right Pane (Chat) Fullscreen
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = null,
                                tint = ClaudeTerracotta,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Antigravity Agent (Maximized)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isRightMaximized = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Restore Split",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onToggleSplitMode,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VerticalSplit,
                                    contentDescription = "Exit Desktop Mode",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    rightContent()
                }
            }
        } else {
            // Multi-Pane Split View: Left Tool + Draggable Splitter + Right Agent
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Workspace (IDE / Terminal / Browser / Files / Notes / Run)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(splitRatio)
                ) {
                    DesktopPaneHeader(
                        activeMode = leftPaneMode,
                        onSelectMode = onSelectLeftPaneMode,
                        isMaximized = false,
                        onToggleMaximize = { isLeftMaximized = true },
                        onToggleSplitMode = onToggleSplitMode
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        leftContent(leftPaneMode)
                    }
                }

                // Draggable Split Divider
                DesktopSplitDivider(
                    onDrag = { delta ->
                        if (totalWidthPx > 0f) {
                            val newRatio = (splitRatio + (delta / totalWidthPx)).coerceIn(0.25f, 0.75f)
                            splitRatio = newRatio
                        }
                    },
                    onReset = { splitRatio = 0.58f }
                )

                // Right Workspace (Agent Chat)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f - splitRatio)
                ) {
                    // Slim Top bar for Right Workspace
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ClaudeTerracotta)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Agent Workspace",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isRightMaximized = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Maximize Agent",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        rightContent()
                    }
                }
            }
        }
    }
}

/**
 * Top Tab Switcher Header for the Left Workspace.
 */
@Composable
private fun DesktopPaneHeader(
    activeMode: AppViewMode,
    onSelectMode: (AppViewMode) -> Unit,
    isMaximized: Boolean,
    onToggleMaximize: () -> Unit,
    onToggleSplitMode: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Workspace Mode Tabs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                DesktopToolTab(
                    icon = Icons.Outlined.Code,
                    label = "Editor",
                    isSelected = activeMode == AppViewMode.IDE,
                    onClick = { onSelectMode(AppViewMode.IDE) }
                )
                DesktopToolTab(
                    icon = Icons.Outlined.Terminal,
                    label = "Terminal",
                    isSelected = activeMode == AppViewMode.TERMINAL,
                    onClick = { onSelectMode(AppViewMode.TERMINAL) }
                )
                DesktopToolTab(
                    icon = Icons.Outlined.Language,
                    label = "Browser",
                    isSelected = activeMode == AppViewMode.BROWSER,
                    onClick = { onSelectMode(AppViewMode.BROWSER) }
                )
                DesktopToolTab(
                    icon = Icons.Outlined.Folder,
                    label = "Files",
                    isSelected = activeMode == AppViewMode.FILES,
                    onClick = { onSelectMode(AppViewMode.FILES) }
                )
                DesktopToolTab(
                    icon = Icons.Outlined.PlayArrow,
                    label = "Run",
                    isSelected = activeMode == AppViewMode.RUN,
                    onClick = { onSelectMode(AppViewMode.RUN) }
                )
            }

            // Window Controls (Maximize / Fullscreen, Single/Split Mode toggle)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMaximize,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isMaximized) "Restore Split" else "Maximize Editor",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onToggleSplitMode,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerticalSplit,
                        contentDescription = "Toggle Desktop Split",
                        tint = ClaudeTerracotta,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopToolTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = ClaudeTerracotta
    val bg = if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

/**
 * Draggable Split Divider between Left and Right panes.
 */
@Composable
private fun DesktopSplitDivider(
    onDrag: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Double click can reset to default
            },
        contentAlignment = Alignment.Center
    ) {
        // Vertical Handle Pill
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
    }
}
