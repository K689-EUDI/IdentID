/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package com.k689.identid.ui.dashboard.home

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.k689.identid.R
import com.k689.identid.extension.ui.finish
import com.k689.identid.extension.ui.openAppSettings
import com.k689.identid.extension.ui.openBleSettings
import com.k689.identid.ui.component.AppIcons
import com.k689.identid.ui.component.content.ContentScreen
import com.k689.identid.ui.component.content.ScreenNavigateAction
import com.k689.identid.ui.component.preview.PreviewTheme
import com.k689.identid.ui.component.preview.ThemeModePreviews
import com.k689.identid.ui.component.utils.OneTimeLaunchedEffect
import com.k689.identid.ui.component.utils.SPACING_EXTRA_LARGE
import com.k689.identid.ui.component.utils.SPACING_EXTRA_SMALL
import com.k689.identid.ui.component.utils.SPACING_LARGE
import com.k689.identid.ui.component.utils.SPACING_MEDIUM
import com.k689.identid.ui.component.utils.SPACING_SMALL
import com.k689.identid.ui.component.wrap.ActionCardConfig
import com.k689.identid.ui.component.wrap.BottomSheetTextDataUi
import com.k689.identid.ui.component.wrap.DialogBottomSheet
import com.k689.identid.ui.component.wrap.WrapIconButton
import com.k689.identid.ui.component.wrap.WrapModalBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

typealias DashboardEvent = com.k689.identid.ui.dashboard.dashboard.Event
typealias OpenSideMenuEvent = com.k689.identid.ui.dashboard.dashboard.Event.SideMenu.Open

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavController,
    viewModel: HomeViewModel,
    onDashboardEventSent: (DashboardEvent) -> Unit,
) {
    val context = LocalContext.current
    val state: State by viewModel.viewState.collectAsStateWithLifecycle()
    val isBottomSheetOpen = state.isBottomSheetOpen
    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.PartiallyExpanded,
                ),
        )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.NONE,
        onBack = { context.finish() },
        topBar = {
            TopBar(onEventSent = onDashboardEventSent)
        },
    ) { paddingValues ->
        BottomSheetScaffold(
            // Only apply top padding so the bottom sheet sits all the way to the bottom edge of the device
            modifier =
                Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                ),
            scaffoldState = scaffoldState,
            sheetShadowElevation = 16.dp,
            // Add the system bottom padding to ensure it peeks exactly 240dp visibly above the nav bar
            sheetPeekHeight = 400.dp + paddingValues.calculateBottomPadding(),
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetContent = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            // Ensure the actual text/content inside the sheet clears the navigation bar
                            .padding(bottom = paddingValues.calculateBottomPadding()),
                ) {
                    Text(
                        text = stringResource(R.string.recent_transactions),
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = SPACING_LARGE.dp,
                                    vertical = SPACING_EXTRA_SMALL.dp,
                                ),
                    )

                    HomeScreenSheetContent(
                        sheetContent = state.sheetContent,
                        onEventSent = { event -> viewModel.setEvent(event) },
                    )
                }
            },
        ) { scaffoldPadding ->
            Content(
                state = state,
                effectFlow = viewModel.effect,
                onEventSent = { viewModel.setEvent(it) },
                onNavigationRequested = { handleNavigationEffect(it, navHostController, context) },
                coroutineScope = scope,
                modalBottomSheetState = scaffoldState.bottomSheetState,
                // The Scaffold automatically calculates its bottom padding to include the sheet's peek height
                paddingValues =
                    PaddingValues(
                        top = scaffoldPadding.calculateTopPadding(),
                        bottom = scaffoldPadding.calculateBottomPadding(),
                    ),
            )
        }
    }

    if (isBottomSheetOpen) {
        WrapModalBottomSheet(
            onDismissRequest = {
                viewModel.setEvent(
                    Event.BottomSheet.UpdateBottomSheetState(
                        isOpen = false,
                    ),
                )
            },
            sheetState = bottomSheetState,
        ) {
            HomeScreenSheetContent(
                sheetContent = state.sheetContent,
                onEventSent = { event -> viewModel.setEvent(event) },
            )
        }
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Init)
    }
}

@Composable
private fun TopBar(
    onEventSent: (DashboardEvent) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = SPACING_LARGE.dp,
                    vertical = SPACING_SMALL.dp,
                ),
    ) {
        // home menu icon
        WrapIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            iconData = AppIcons.Menu,
            customTint = MaterialTheme.colorScheme.onSurface,
        ) {
            onEventSent(OpenSideMenuEvent)
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSent: ((event: Event) -> Unit),
    onNavigationRequested: (navigationEffect: Effect.Navigation) -> Unit,
    coroutineScope: CoroutineScope,
    modalBottomSheetState: SheetState,
    paddingValues: PaddingValues,
) {
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                ).verticalScroll(scrollState),
    ) {
        Text(
            text = state.welcomeUserMessage,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            modifier =
                Modifier.padding(
                    start = SPACING_EXTRA_LARGE.dp,
                    end = SPACING_EXTRA_LARGE.dp,
                    top = SPACING_SMALL.dp,
                    bottom = SPACING_EXTRA_LARGE.dp,
                ),
        )

        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            Card(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha =
                                lerp(
                                    start = 0.8f,
                                    stop = 1f,
                                    fraction = 1f,
                                )
                            scaleY =
                                lerp(
                                    start = 0.9f,
                                    stop = 1f,
                                    fraction = 1f,
                                )
                        },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Card $page")
                }
            }
        }
    }

    if (state.bleAvailability == BleAvailability.NO_PERMISSION) {
        RequiredPermissionsAsk(state, onEventSent)
    }

    LaunchedEffect(Unit) {
        effectFlow
            .onEach { effect ->
                when (effect) {
                    is Effect.Navigation -> {
                        onNavigationRequested(effect)
                    }

                    is Effect.CloseBottomSheet -> {
                        coroutineScope
                            .launch {
                                if (effect.hasNextBottomSheet.not()) {
                                    modalBottomSheetState.hide()
                                } else {
                                    modalBottomSheetState.hide().also {
                                        modalBottomSheetState.show()
                                        onEventSent(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                                    }
                                }
                            }.invokeOnCompletion {
                                if (!modalBottomSheetState.isVisible) {
                                    onEventSent(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                                }
                            }
                    }

                    is Effect.ShowBottomSheet -> {
                        onEventSent(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                    }
                }
            }.collect()
    }
}

private fun handleNavigationEffect(
    navigationEffect: Effect.Navigation,
    navController: NavController,
    context: Context,
) {
    when (navigationEffect) {
        is Effect.Navigation.SwitchScreen -> {
            navController.navigate(navigationEffect.screenRoute) {
                popUpTo(navigationEffect.popUpToScreenRoute) {
                    inclusive = navigationEffect.inclusive
                }
            }
        }

        is Effect.Navigation.OnAppSettings -> {
            context.openAppSettings()
        }

        is Effect.Navigation.OnSystemSettings -> {
            context.openBleSettings()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenSheetContent(
    sheetContent: HomeScreenBottomSheetContent,
    onEventSent: (event: Event) -> Unit,
) {
    when (sheetContent) {
        is HomeScreenBottomSheetContent.Bluetooth -> {
            DialogBottomSheet(
                textData =
                    BottomSheetTextDataUi(
                        title = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_title),
                        message = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_subtitle),
                        positiveButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_primary_button_text),
                        negativeButtonText = stringResource(id = R.string.dashboard_bottom_sheet_bluetooth_secondary_button_text),
                    ),
                onPositiveClick = {
                    onEventSent(
                        Event.BottomSheet.Bluetooth.PrimaryButtonPressed(
                            sheetContent.availability,
                        ),
                    )
                },
                onNegativeClick = { onEventSent(Event.BottomSheet.Bluetooth.SecondaryButtonPressed) },
            )
        }

        else -> {
            // Placeholder: Authenticate and Sign Document modals, or recent transactions will be added later
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequiredPermissionsAsk(
    state: State,
    onEventSend: (Event) -> Unit,
) {
    val permissions: MutableList<String> = mutableListOf()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && state.isBleCentralClientModeEnabled) {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    when {
        permissionsState.allPermissionsGranted -> {
            onEventSend(Event.StartProximityFlow)
        }

        !permissionsState.allPermissionsGranted && permissionsState.shouldShowRationale -> {
            onEventSend(Event.OnShowPermissionsRational)
        }

        else -> {
            onEventSend(Event.OnPermissionStateChanged(BleAvailability.UNKNOWN))
            LaunchedEffect(Unit) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun HomeScreenContentPreview() {
    PreviewTheme {
        val scaffoldState =
            rememberBottomSheetScaffoldState(
                bottomSheetState =
                    rememberStandardBottomSheetState(
                        initialValue = SheetValue.PartiallyExpanded,
                    ),
            )

        ContentScreen(
            isLoading = false,
            navigatableAction = ScreenNavigateAction.NONE,
            onBack = { },
            topBar = {
                TopBar(
                    onEventSent = {},
                )
            },
        ) { paddingValues ->
            BottomSheetScaffold(
                modifier =
                    Modifier.padding(
                        top = paddingValues.calculateTopPadding(),
                    ),
                scaffoldState = scaffoldState,
                sheetPeekHeight = 400.dp + paddingValues.calculateBottomPadding(),
                sheetContent = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .padding(bottom = paddingValues.calculateBottomPadding()),
                    ) {}
                },
            ) { scaffoldPadding ->
                Content(
                    state =
                        State(
                            isBottomSheetOpen = false,
                            welcomeUserMessage = "Welcome back, Alex",
                            authenticateCardConfig =
                                ActionCardConfig(
                                    title = stringResource(R.string.home_screen_authentication_card_title),
                                    icon = AppIcons.WalletActivated,
                                    primaryButtonText = stringResource(R.string.home_screen_authenticate),
                                    secondaryButtonText = stringResource(R.string.home_screen_learn_more),
                                ),
                            signCardConfig =
                                ActionCardConfig(
                                    title = stringResource(R.string.home_screen_sign_card_title),
                                    icon = AppIcons.Contract,
                                    primaryButtonText = stringResource(R.string.home_screen_sign),
                                    secondaryButtonText = stringResource(R.string.home_screen_learn_more),
                                ),
                        ),
                    effectFlow = Channel<Effect>().receiveAsFlow(),
                    onNavigationRequested = {},
                    coroutineScope = rememberCoroutineScope(),
                    modalBottomSheetState = rememberModalBottomSheetState(),
                    onEventSent = {},
                    paddingValues =
                        PaddingValues(
                            top = scaffoldPadding.calculateTopPadding(),
                            bottom = scaffoldPadding.calculateBottomPadding(),
                        ),
                )
            }
        }
    }
}
