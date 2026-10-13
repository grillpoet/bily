package com.grillpoet.bily.ui.widget

import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.grillpoet.bily.core.DurationUtils
import kotlinx.coroutines.delay
import kotlin.math.abs

private val SPEED_OPTIONS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

@UnstableApi
@Composable
fun GesturePlayer(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(false) }
    var dragDelta by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }

    var currentPos by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var buffering by remember { mutableStateOf(false) }

    var aspectRatio by remember { mutableFloatStateOf(16f / 9f) }

    var menuVisible by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1f) }

    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }

    val progressProvider: () -> Float = {
        if (duration > 0L) {
            val pos = when {
                isSeeking -> (seekProgress * duration).toLong()
                dragging -> (currentPos + (dragDelta / 3f).toLong() * 1000L)
                    .coerceIn(0L, duration)
                else -> currentPos
            }
            pos.toFloat() / duration.toFloat()
        } else 0f
    }

    LaunchedEffect(player, controlsVisible, dragging, isSeeking) {
        if (!controlsVisible && !dragging && !isSeeking) {
            runCatching {
                currentPos = player.currentPosition
                duration = player.duration.coerceAtLeast(0L)
                isPlaying = player.isPlaying
            }
            return@LaunchedEffect
        }
        while (true) {
            runCatching {
                if (!isSeeking) {
                    currentPos = player.currentPosition
                }
                duration = player.duration.coerceAtLeast(0L)
                isPlaying = player.isPlaying
            }
            delay(500)
        }
    }

    LaunchedEffect(
        player, controlsVisible, isPlaying, dragging, menuVisible, isSeeking
    ) {
        if (controlsVisible && isPlaying && !dragging && !menuVisible && !isSeeking) {
            delay(3000)
            controlsVisible = false
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width == 0 || videoSize.height == 0) return
                aspectRatio = (videoSize.width * videoSize.pixelWidthHeightRatio) /
                        videoSize.height
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                buffering = playbackState == Player.STATE_BUFFERING
                runCatching { duration = player.duration.coerceAtLeast(0L) }
            }
        }
        player.addListener(listener)
        onDispose { runCatching { player.removeListener(listener) } }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // 视频区域：按实际宽高比居中，黑边由外层 Box 填充
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val videoRatio = aspectRatio
            val w = maxWidth
            val h = maxHeight
            val containerRatio = w.value / h.value

            val (videoW, videoH) = if (videoRatio > containerRatio) {
                w to (w / videoRatio)
            } else {
                (h * videoRatio) to h
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.size(videoW, videoH),
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            runCatching { player.setVideoTextureView(this) }
                        }
                    }
                )
            }
        }

        if (buffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }

        // 整屏手势层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(player) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = {
                            runCatching {
                                if (player.isPlaying) player.pause() else player.play()
                            }
                            controlsVisible = true
                        }
                    )
                }
                .pointerInput(player) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragging = true
                            dragDelta = 0f
                        },
                        onDragEnd = {
                            val deltaMs = (dragDelta / 3f).toLong() * 1000
                            if (abs(deltaMs) > 500) {
                                runCatching {
                                    val target = (player.currentPosition + deltaMs)
                                        .coerceIn(0L, player.duration.coerceAtLeast(0L))
                                    player.seekTo(target)
                                }
                            }
                            dragging = false
                            dragDelta = 0f
                        },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            dragDelta += delta
                        }
                    )
                }
        )

        // 拖动画面时的中央提示
        if (dragging && abs(dragDelta) > 20f) {
            val deltaSec = (dragDelta / 3f).toLong()
            val target = (currentPos + deltaSec * 1000).coerceIn(0L, duration)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${if (deltaSec >= 0) "+" else ""}${deltaSec}s  " +
                            DurationUtils.format(target / 1000),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // 底部控制条
        if (controlsVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .pointerInput(player) {
                            detectTapGestures { offset ->
                                val dur = runCatching {
                                    player.duration.coerceAtLeast(0L)
                                }.getOrDefault(0L)
                                if (dur > 0L) {
                                    val p = (offset.x / size.width)
                                        .coerceIn(0f, 1f)
                                    val target = (p * dur).toLong()
                                    runCatching { player.seekTo(target) }
                                    currentPos = target
                                }
                            }
                        }
                        .pointerInput(player) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isSeeking = true
                                    controlsVisible = true
                                    seekProgress = (offset.x / size.width)
                                        .coerceIn(0f, 1f)
                                },
                                onDragEnd = {
                                    val dur = runCatching {
                                        player.duration.coerceAtLeast(0L)
                                    }.getOrDefault(0L)
                                    if (dur > 0L) {
                                        val target =
                                            (seekProgress * dur).toLong()
                                        runCatching { player.seekTo(target) }
                                        currentPos = target
                                    }
                                    isSeeking = false
                                },
                                onDragCancel = { isSeeking = false },
                                onHorizontalDrag = { change, delta ->
                                    change.consume()
                                    seekProgress = (seekProgress +
                                            delta / size.width)
                                        .coerceIn(0f, 1f)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barH = 20.dp.toPx()
                        val cy = size.height / 2f
                        val top = cy - barH / 2f
                        val p = if (isSeeking) seekProgress.coerceIn(0f, 1f)
                        else progressProvider().coerceIn(0f, 1f)

                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            topLeft = Offset(0f, top),
                            size = Size(size.width, barH),
                            cornerRadius = CornerRadius(barH / 2f)
                        )
                        if (p > 0f) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(0f, top),
                                size = Size(size.width * p, barH),
                                cornerRadius = CornerRadius(barH / 2f)
                            )
                        }
                        if (isSeeking) {
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = Offset(size.width * p, cy)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "倍速",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { menuVisible = true }
                        )

                        DropdownMenu(
                            expanded = menuVisible,
                            onDismissRequest = { menuVisible = false }
                        ) {
                            SPEED_OPTIONS.forEach { v ->
                                val label = if (v == 1f) "1.0x" else "${v}x"
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (v == speed)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onBackground
                                        )
                                    },
                                    onClick = {
                                        runCatching { player.setPlaybackSpeed(v) }
                                        speed = v
                                        menuVisible = false
                                    }
                                )
                            }
                        }
                    }

                    val shownPos = if (isSeeking) {
                        (seekProgress * duration).toLong()
                    } else currentPos

                    Text(
                        text = "${DurationUtils.format(shownPos / 1000)} / " +
                                DurationUtils.format(duration / 1000),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
