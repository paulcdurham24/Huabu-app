package com.huabu.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.huabu.app.ui.theme.*

@Composable
fun ImagePickerButton(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CircleShape,
    placeholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HuabuDivider),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = HuabuSilver,
                modifier = Modifier.size(32.dp)
            )
        }
    }
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onImageSelected(it) }
        }
    )

    Box(
        modifier = modifier
            .clip(shape)
            .clickable {
                photoPicker.launch("image/*")
            }
    ) {
        if (!currentImageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = currentImageUrl,
                contentDescription = "Selected image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            placeholder()
        }

        // Camera icon overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HuabuDarkBg.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change photo",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ProfileImagePicker(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    ImagePickerButton(
        currentImageUrl = currentImageUrl,
        onImageSelected = onImageSelected,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        shape = CircleShape,
        placeholder = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HuabuDeepPurple.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size((size / 2).dp)
                )
            }
        }
    )
}

@Composable
fun CoverImagePicker(
    currentImageUrl: String?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 200
) {
    ImagePickerButton(
        currentImageUrl = currentImageUrl,
        onImageSelected = onImageSelected,
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        shape = RoundedCornerShape(16.dp),
        placeholder = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(HuabuDeepPurple, HuabuHotPink.copy(alpha = 0.5f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to add cover photo",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

@Composable
fun PostImagePicker(
    currentImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onImageClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onImageSelected(it) }
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable {
                if (currentImageUri == null) {
                    photoPicker.launch("image/*")
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg)
    ) {
        if (currentImageUri != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = currentImageUri,
                    contentDescription = "Post image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Clear button
                IconButton(
                    onClick = onImageClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(HuabuDarkBg.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = HuabuSilver,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add photo to post",
                    color = HuabuSilver,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
