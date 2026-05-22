package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.Place
import com.example.ui.theme.RatingGold
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AdventureUiState
import com.example.ui.viewmodel.AdventureViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(viewModel: AdventureViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val activeTab by viewModel.discoveryTab.collectAsState()
    val activePlaces by viewModel.activePlaces.collectAsState()

    if (activePlaces.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FilterFrames,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Active Search",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Head to the Home tab and tap the big Adventure button to generate secret local micro-adventures!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Selector Tab Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setDiscoveryTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Swipe Cards", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_swipe_cards")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setDiscoveryTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Spin Wheel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_spin_wheel")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { viewModel.setDiscoveryTab(2) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("List View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_list_view")
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

        // Dynamic Mode Content Rendering
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> SwipeCardView(viewModel, activePlaces)
                1 -> SpinWheelView(viewModel, activePlaces)
                2 -> TraditionalListView(viewModel, activePlaces)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Swipe Cards - Tinder-style swipe simulation layout
// -------------------------------------------------------------
@Composable
fun SwipeCardView(viewModel: AdventureViewModel, places: List<Place>) {
    val context = LocalContext.current
    val favorites by viewModel.favorites.collectAsState()
    val currentIndex by viewModel.swipeIndex.collectAsState()

    if (currentIndex >= places.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "End of Deck!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You've explored all generated adventures for this search. You can click 'Reset Deck' to browse again or head home to set new search filters!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.resetSwipeDeck() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Deck")
                }
            }
        }
        return
    }

    val place = places[currentIndex]
    val isFav = favorites.any { it.placeId == place.placeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Swipeable Card Stack Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("swipe_card_container")
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { viewModel.selectPlace(place) }
        ) {
            // Unsplash Graphic Backdrop
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(place.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Place Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dim gradient scrim overlay for visual contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 400f
                        )
                    )
            )

            // Quick Category Tag at Top Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = place.categories.firstOrNull() ?: "Adventure",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            // Text Labels at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                // Name Row
                Text(
                    text = place.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rating & Price Indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = place.rating.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "•", color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = formatPrice(place.priceLevel), color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "•", color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "${place.distance} km away", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = place.address,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large CTA Action Control Hub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dislike/Skip Button
            IconButton(
                onClick = { viewModel.incrementSwipeIndex() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .testTag("action_skip")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Skip Adventure",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Info Click Button
            IconButton(
                onClick = { viewModel.selectPlace(place) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("action_details")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Favorite/Like Button
            IconButton(
                onClick = {
                    viewModel.toggleFavorite(place)
                    viewModel.incrementSwipeIndex()
                    Toast.makeText(context, "Added to Favorites!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .testTag("action_favorite")
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Adventure",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Spin The Wheel View (Real segmented canvas & float physics)
// -------------------------------------------------------------
@Composable
fun SpinWheelView(viewModel: AdventureViewModel, places: List<Place>) {
    val context = LocalContext.current
    val isSpinning by viewModel.isSpinning.collectAsState()
    val selectedResult by viewModel.spinSelectedPlace.collectAsState()

    // Control continuous rotation value smoothly
    var rotationValue by remember { mutableStateOf(0f) }
    val animatedRotation = remember { Animatable(0f) }

    // Start physical decelerating rotation when ViewModel trigger hits isSpinning
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            // Rotate full speed
            animatedRotation.animateTo(
                targetValue = animatedRotation.value + 1440f + (30..330).random().toFloat(),
                animationSpec = tween(
                    durationMillis = 2500,
                    easing = CubicBezierEasing(0.12f, 0.8f, 0.15f, 1.0f)
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Spin for Spontaneity!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Rendering Wheel Stage Space
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anchor marker pointer arrows
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
            )

            // Dynamic Rotating segmented Canvas Circle
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .rotate(animatedRotation.value)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val colorsList = listOf(
                        Color(0xFFBD93F9), Color(0xFF8BE9FD), Color(0xFF50FA7B),
                        Color(0xFFFFB86C), Color(0xFFFF79C6), Color(0xFFF1FA8C),
                        Color(0xFFFF5555), Color(0xFF6272A4)
                    )

                    val totalSegments = places.size.coerceAtLeast(1)
                    val sweepAngle = 360f / totalSegments

                    for (i in 0 until totalSegments) {
                        val segmentColor = colorsList[i % colorsList.size]
                        drawArc(
                            color = segmentColor,
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = size,
                            topLeft = Offset.Zero
                        )
                        
                        // Draw segment borders
                        drawArc(
                            color = Color.Black.copy(alpha = 0.15f),
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = size,
                            topLeft = Offset.Zero,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Inner circle
                    drawCircle(
                        color = Color.White,
                        radius = 24.dp.toPx(),
                        center = center
                    )
                }

                // Numbers Labels on segments
                places.forEachIndexed { i, _ ->
                    val totalSegments = places.size
                    val angleRad = (i * (360f / totalSegments) + (180f / totalSegments)) * PI / 180f
                    val radius = 80.dp
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .offset(
                                x = (radius.value * cos(angleRad)).dp,
                                y = (radius.value * sin(angleRad)).dp
                            )
                    ) {
                        Text(
                            text = "${i + 1}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // SPIN Button
            Button(
                onClick = { viewModel.spinWheel() },
                enabled = !isSpinning,
                modifier = Modifier
                    .size(70.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("spin_the_wheel_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "SPIN",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        // Revealed Selection Card Popup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSpinning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selecting micro-adventure...", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            } else if (selectedResult != null) {
                val chosen = selectedResult!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("spin_selection_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    onClick = { viewModel.selectPlace(chosen) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Thumbnail
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(chosen.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎉 You Got: ${chosen.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = chosen.address,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(chosen.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${chosen.distance} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap SPIN and let randomness decide your next memory!",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Traditional List View
// -------------------------------------------------------------
@Composable
fun TraditionalListView(viewModel: AdventureViewModel, places: List<Place>) {
    val context = LocalContext.current
    val favorites by viewModel.favorites.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places) { place ->
            val isFav = favorites.any { it.placeId == place.placeId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .testTag("list_card_${place.placeId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                onClick = { viewModel.selectPlace(place) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(place.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text Details Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = place.address,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(place.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(formatPrice(place.priceLevel), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${place.distance} km", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Small Quick Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(place) }
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Favorite",
                            tint = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// Formatting helpers
private fun formatPrice(priceLevel: Int): String {
    return when (priceLevel) {
        0 -> "Free"
        1 -> "$"
        2 -> "$$"
        3 -> "$$$"
        4 -> "$$$$"
        else -> "$"
    }
}
