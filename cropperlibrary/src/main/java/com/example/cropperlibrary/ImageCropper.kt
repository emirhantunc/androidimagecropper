package com.example.cropperlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ImageCropper(
    imageUri: Uri,
    cropWidth: Float = 300f,
    cropHeight: Float = 300f,
    onCropComplete: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var cropRect by remember { mutableStateOf(Rect(0f, 0f, cropWidth, cropHeight)) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(imageUri) {
        imageBitmap = loadBitmapFromUri(context, imageUri)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        imageBitmap?.let { bitmap ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offset += pan
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newRect = cropRect.translate(dragAmount.x, dragAmount.y)
                            if (newRect.left >= 0 && newRect.right <= canvasSize.width &&
                                newRect.top >= 0 && newRect.bottom <= canvasSize.height
                            ) {
                                cropRect = newRect
                            }
                        }
                    }
            ) {
                canvasSize = size

                val imgWidth = bitmap.width * scale
                val imgHeight = bitmap.height * scale
                val imgOffset = Offset(
                    (size.width - imgWidth) / 2 + offset.x,
                    (size.height - imgHeight) / 2 + offset.y
                )
                drawImage(
                    image = bitmap,
                    dstOffset = imgOffset.round(),
                    dstSize = androidx.compose.ui.unit.IntSize(
                        imgWidth.toInt(),
                        imgHeight.toInt()
                    )
                )

                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = size
                )

                drawImage(
                    image = bitmap,
                    srcOffset = androidx.compose.ui.unit.IntOffset(
                        ((cropRect.left - imgOffset.x) / scale).toInt().coerceIn(0, bitmap.width),
                        ((cropRect.top - imgOffset.y) / scale).toInt().coerceIn(0, bitmap.height)
                    ),
                    srcSize = androidx.compose.ui.unit.IntSize(
                        (cropRect.width / scale).toInt().coerceIn(1, bitmap.width),
                        (cropRect.height / scale).toInt().coerceIn(1, bitmap.height)
                    ),
                    dstOffset = cropRect.topLeft.round(),
                    dstSize = androidx.compose.ui.unit.IntSize(
                        cropRect.width.toInt(),
                        cropRect.height.toInt()
                    )
                )

                drawRect(
                    color = Color.White,
                    topLeft = cropRect.topLeft,
                    size = cropRect.size,
                    style = Stroke(width = 3f)
                )

                val corners = listOf(
                    cropRect.topLeft,
                    Offset(cropRect.right, cropRect.top),
                    Offset(cropRect.left, cropRect.bottom),
                    cropRect.bottomRight
                )

                corners.forEach { corner ->
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = corner
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "cancel",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    imageBitmap?.let { bitmap ->
                        val croppedBitmap = cropBitmap(
                            bitmap.asAndroidBitmap(),
                            cropRect,
                            scale,
                            offset,
                            canvasSize
                        )
                        onCropComplete(croppedBitmap)
                    }
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "confirm",
                    tint = Color.White
                )
            }
        }
    }
}

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

private fun cropBitmap(
    bitmap: Bitmap,
    cropRect: Rect,
    scale: Float,
    offset: Offset,
    canvasSize: Size
): Bitmap {
    val imgWidth = bitmap.width * scale
    val imgHeight = bitmap.height * scale
    val imgOffset = Offset(
        (canvasSize.width - imgWidth) / 2 + offset.x,
        (canvasSize.height - imgHeight) / 2 + offset.y
    )

    val x = ((cropRect.left - imgOffset.x) / scale).toInt().coerceIn(0, bitmap.width)
    val y = ((cropRect.top - imgOffset.y) / scale).toInt().coerceIn(0, bitmap.height)
    val width = (cropRect.width / scale).toInt().coerceIn(1, bitmap.width - x)
    val height = (cropRect.height / scale).toInt().coerceIn(1, bitmap.height - y)

    return Bitmap.createBitmap(bitmap, x, y, width, height)
}