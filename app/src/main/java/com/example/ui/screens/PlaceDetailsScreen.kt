package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.RatingGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AdventureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsScreen(viewModel: AdventureViewModel) {
    val context = LocalContext.current
    val place by viewModel.selectedPlace.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    if (place == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Adventure Selected", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val item = place!!
    val isFav = favorites.any { it.placeId == item.placeId }
    val matchingFavorite = favorites.find { it.placeId == item.placeId }

    // local note editing state
    var noteText by remember { mutableStateOf(matchingFavorite?.note ?: "") }
    var isEditingNote by remember { mutableStateOf(false) }

    // Keep note state synchronized if the favorite is edited or added
    LaunchedEffect(matchingFavorite) {
        noteText = matchingFavorite?.note ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Cover Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Place Hero Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scaffold-style Dark Top Gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            endY = 180f
                        )
                    )
            )

            // Header Back Button & Toggle Favorite floating
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("discovery") },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back", tint = Color.White)
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(item) },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Toggle",
                        tint = if (isFav) MaterialTheme.colorScheme.secondary else Color.White
                    )
                }
            }
        }

        // Details Panel Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Category badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.categories.forEach { cat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Name
            Text(
                text = item.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Sub-header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${item.rating} Rating", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Text("•", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = translateBudgetLevel(item.priceLevel), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("•", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "${item.distance} km away", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Direct Call, Website, and Navigation Buttons Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Action button
                OutlinedButton(
                    onClick = {
                        if (item.phone.isNotBlank() && item.phone != "No Phone") {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
                            context.startActivity(callIntent)
                        } else {
                            Toast.makeText(context, "No phone number listed for this location.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Web Action button
                OutlinedButton(
                    onClick = {
                        if (item.website.isNotBlank() && item.website.startsWith("http")) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.website))
                            context.startActivity(webIntent)
                        } else {
                            Toast.makeText(context, "No website link listed for this location.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Website", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Directions action button
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${item.latitude},${item.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Fallback web maps link
                            val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.latitude},${item.longitude}"))
                            context.startActivity(webMapIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .padding(start = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Address and Open state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Physical address
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PHYSICAL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.address, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Open hours row
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SCHEDULE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (item.isOpenNow) SuccessGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (item.isOpenNow) "Open Now" else "Closed",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isOpenNow) SuccessGreen else Color.Red
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (item.openingHours.isNotEmpty()) {
                                item.openingHours.forEach { hours ->
                                    Text(text = hours, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            } else {
                                Text(text = "Opening hours not listed", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Notes Area for Saved Places
            if (isFav) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Quick Logs",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!isEditingNote) {
                                TextButton(onClick = { isEditingNote = true }) {
                                    Text("Edit")
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        viewModel.updateFavoriteNote(item.placeId, noteText)
                                        isEditingNote = false
                                        Toast.makeText(context, "Logs saved!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Save")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingNote) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("Add parking details, companion notes, or schedules here...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .testTag("details_note_textfield"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = noteText.ifEmpty { "Write down any thoughts, directions, secret coordinates, or reminders about this adventure spot." },
                                    fontSize = 13.sp,
                                    color = if (noteText.isEmpty()) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // If it is not favorite, suggest saving to favorite to log notes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    onClick = {
                        viewModel.toggleFavorite(item)
                        Toast.makeText(context, "Log active! Now you can save custom notes.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Love this adventure?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Save it to log private notes and keep track of it offline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

private fun translateBudgetLevel(level: Int): String {
    return when (level) {
        0 -> "Free"
        1 -> "$"
        2 -> "$$"
        3 -> "$$$"
        4 -> "$$$$"
        else -> "$"
    }
}
