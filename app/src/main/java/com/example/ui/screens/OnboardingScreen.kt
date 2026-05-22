package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AdventureViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(viewModel: AdventureViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showManualDialog by remember { mutableStateOf(false) }
    var manualCityInput by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }

    // Accompanist Permissions hook
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val hasLocationPermission = locationPermissionsState.permissions.any { it.status.isGranted }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            isLocating = true
            viewModel.detectGPSLocation(
                context = context,
                onSuccess = { resolvedCity ->
                    isLocating = false
                    Toast.makeText(context, "Location set to $resolvedCity", Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo("home")
                },
                onFailure = { error ->
                    isLocating = false
                    Toast.makeText(context, "GPS Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Creative Logo Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Map Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name / Headlines
            Text(
                text = "Random Adventure",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Break your routine and let spontaneity map your day. Generate secret spots and local micro-adventures matching your precise vibe and budget.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLocating) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Detecting your city...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                // Main GPS Detection Button
                Button(
                    onClick = {
                        if (hasLocationPermission) {
                            isLocating = true
                            viewModel.detectGPSLocation(
                                context = context,
                                onSuccess = { resolvedCity ->
                                    isLocating = false
                                    viewModel.navigateTo("home")
                                },
                                onFailure = { error ->
                                    isLocating = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("enable_gps_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Detect Location", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Entry Option Button
                OutlinedButton(
                    onClick = { showManualDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("manual_entry_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.PinDrop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter City Manually", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Manual City Entry Dialog modal
        if (showManualDialog) {
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                title = {
                    Text("Set Location", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            "Type a city name (e.g. Seattle, WA or London) or address to establish your adventure hub.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = manualCityInput,
                            onValueChange = { manualCityInput = it },
                            placeholder = { Text("Seattle, WA") },
                            leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("city_input_onboarding"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                if (manualCityInput.isNotBlank()) {
                                    viewModel.setManualLocation(manualCityInput)
                                    showManualDialog = false
                                    viewModel.navigateTo("home")
                                }
                            })
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualCityInput.isNotBlank()) {
                                viewModel.setManualLocation(manualCityInput)
                                showManualDialog = false
                                viewModel.navigateTo("home")
                            }
                        },
                        modifier = Modifier.testTag("confirm_city_button")
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
