package com.beehive.tracker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun HivePinWidget(
    name: String,
    posX: Float,
    posY: Float,
    colorHex: String?,
    isEditMode: Boolean,
    snapFn: (Offset) -> Offset,
    onClick: () -> Unit,
    onDragEnd: (posX: Float, posY: Float) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pinColor = remember(colorHex) {
        colorHex?.let { parseColor(it) } ?: Color(0xFFBDBDBD)
    }

    var rawOffset     by remember(posX, posY) { mutableStateOf(Offset(posX, posY)) }
    var displayOffset by remember(posX, posY) { mutableStateOf(Offset(posX, posY)) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .offset { IntOffset(displayOffset.x.roundToInt(), displayOffset.y.roundToInt()) }
            .size(72.dp)
            .background(pinColor.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .then(
                if (isEditMode) Modifier.pointerInput(snapFn) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            rawOffset += dragAmount
                            displayOffset = snapFn(rawOffset)
                        },
                        onDragEnd = { onDragEnd(displayOffset.x, displayOffset.y) },
                    )
                } else Modifier
            )
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { if (isEditMode) showMenu = true },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(4.dp),
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text("Kovani Sil") },
                onClick = {
                    showMenu = false
                    onDeleteClick()
                },
            )
        }
    }
}
