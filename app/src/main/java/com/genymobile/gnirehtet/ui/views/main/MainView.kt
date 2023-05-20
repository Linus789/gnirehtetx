package com.genymobile.gnirehtet.ui.views.main

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.domain.BlockedApps
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.ui.views.utils.LoadData
import com.genymobile.gnirehtet.ui.views.utils.LoadStatus
import com.genymobile.gnirehtet.ui.views.utils.navigate
import com.genymobile.gnirehtet.utils.ContextUtils
import com.genymobile.gnirehtet.utils.getAppIcon
import com.genymobile.gnirehtet.utils.getInstalledPackagesCompat
import com.genymobile.gnirehtet.utils.versionCodeCompat
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.genymobile.gnirehtet.domain.Gnirehtet
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.utils.getDefaultAppIcon
import kotlinx.coroutines.*

private class InstalledApp(
    val packageInfo: PackageInfo,
    val name: String?,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val hasInternetRequested: Boolean,
    isBlocked: Boolean,
) {
    val displayName: String = name ?: packageInfo.packageName
    private val isBlockedMutableState: MutableState<Boolean> = mutableStateOf(isBlocked)

    fun isBlocked(): Boolean = isBlockedMutableState.value

    suspend fun setBlocked(context: Context, blocked: Boolean) {
        BlockedApps.setAppBlocked(context, packageInfo.packageName, blocked)
        isBlockedMutableState.value = blocked
    }
}

private fun loadInstalledAppsBlocking(): List<InstalledApp> {
    val packageManager = ContextUtils.PackageManager

    return packageManager.getInstalledPackagesCompat(PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
        .map {
            InstalledApp(
                packageInfo = it,
                name = packageManager.getApplicationLabel(it.applicationInfo) as? String?,
                isSystemApp = it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                isEnabled = it.applicationInfo.enabled,
                hasInternetRequested = it.requestedPermissions?.contains(Manifest.permission.INTERNET) ?: false,
                isBlocked = BlockedApps.isAppBlocked(it.packageName),
            )
        }.sortedWith(
            compareByDescending<InstalledApp> { it.isBlocked() }.then(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName }
            )
        )
}

@Composable
private fun loadInstalledApps(): MutableState<LoadStatus<List<InstalledApp>>> {
    return produceState<LoadStatus<List<InstalledApp>>>(initialValue = LoadStatus.Loading) {
        withContext(Dispatchers.Default) {
            value = LoadStatus.Data(loadInstalledAppsBlocking())
        }
    } as MutableState
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val packageManager = ContextUtils.PackageManager
    val activityManager = ContextUtils.ActivityManager
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()
    val gnirehtetIsRunning by Gnirehtet.isRunning().collectAsStateWithLifecycle()
    val gnirehtetIsConnected by Gnirehtet.isConnected().collectAsStateWithLifecycle()
    val shouldStopOnDisconnect by Preferences.shouldStopOnDisconnect().collectAsStateWithLifecycle()
    val gnirehtetEnabled by remember {
        derivedStateOf {
            if (!gnirehtetIsConnected && shouldStopOnDisconnect) {
                false
            } else {
                gnirehtetIsRunning
            }
        }
    }
    val filterAppValue = rememberSaveable { mutableStateOf("") }
    val showSystemApps = rememberSaveable { mutableStateOf(false) }
    val showDisabledApps = rememberSaveable { mutableStateOf(true) }
    val showAppsWithoutInternetPermission = rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                gnirehtetEnabled = gnirehtetEnabled,
                filterAppValue = filterAppValue,
                showSystemApps = showSystemApps,
                showDisabledApps = showDisabledApps,
                showAppsWithoutInternetPermission = showAppsWithoutInternetPermission,
            )
        },
    ) { scaffoldPadding ->
        val installedAppsState = loadInstalledApps()
        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }

        fun refresh() = refreshScope.launch {
            refreshing = true
            withContext(Dispatchers.Default) {
                context.imageLoader.memoryCache?.clear()
                installedAppsState.value = LoadStatus.Data(loadInstalledAppsBlocking())
            }
            refreshing = false
        }

        val state = rememberPullRefreshState(refreshing, ::refresh)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
                .pullRefresh(
                    state,
                    enabled = !refreshing && installedAppsState.value is LoadStatus.Data
                )
        ) {
            LoadData(loadStatus = installedAppsState.value) { installedApps ->
                val defaultIcon = remember { packageManager.getDefaultAppIcon(activityManager) }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                ) {
                    val filteredApps = installedApps.filter {
                        if (!showSystemApps.value && it.isSystemApp) {
                            return@filter false
                        }

                        if (!showDisabledApps.value && !it.isEnabled) {
                            return@filter false
                        }

                        if (!showAppsWithoutInternetPermission.value && !it.hasInternetRequested) {
                            return@filter false
                        }

                        it.displayName.contains(filterAppValue.value, ignoreCase = true)
                    }

                    items(filteredApps, itemContent = { installedApp ->
                        val toggleActionLabel = if (installedApp.isBlocked()) "Unblock" else "Block"
                        val toggleBlock = {
                            Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                                installedApp.setBlocked(context, !installedApp.isBlocked())
                            }

                            true
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 10.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        stateDescription =
                                            if (installedApp.isBlocked()) "Blocked" else "Unblocked"
                                        customActions = listOf(
                                            CustomAccessibilityAction(
                                                toggleActionLabel,
                                                toggleBlock
                                            )
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(packageManager.getAppIcon(activityManager, installedApp.packageInfo.applicationInfo))
                                            .fallback(defaultIcon)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .memoryCacheKey(installedApp.packageInfo.packageName)
                                            .build(),
                                        contentDescription = installedApp.displayName,
                                        modifier = Modifier.size(50.dp)
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(installedApp.displayName, style = MaterialTheme.typography.titleSmall)

                                        if (installedApp.name != null) {
                                            Text(installedApp.packageInfo.packageName, fontSize = 10.sp, lineHeight = 12.sp)
                                        }

                                        Text("${installedApp.packageInfo.versionName} (${installedApp.packageInfo.versionCodeCompat})".trimStart(), fontSize = 10.sp, lineHeight = 12.sp)
                                    }
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    IconButton(
                                        onClick = { toggleBlock() },
                                        modifier = Modifier.clearAndSetSemantics {}
                                    ) {
                                        Icon(
                                            Icons.Filled.Block,
                                            tint = if (installedApp.isBlocked()) Color.Red else LocalContentColor.current,
                                            contentDescription = toggleActionLabel,
                                        )
                                    }
                                }
                            }
                        }
                    })
                }
            }

            PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavHostController,
    gnirehtetEnabled: Boolean,
    filterAppValue: MutableState<String>,
    showSystemApps: MutableState<Boolean>,
    showDisabledApps: MutableState<Boolean>,
    showAppsWithoutInternetPermission: MutableState<Boolean>,
) {
    val context = LocalContext.current
    var isSearching by rememberSaveable { mutableStateOf(false) }
    val vpnRequestReceiver = Gnirehtet.createVpnRequestReceiver(context)

    TopAppBar(
        title = {
            if (isSearching) {
                AppBarTextField(
                    value = filterAppValue.value,
                    onValueChange = { newValue -> filterAppValue.value = newValue },
                    hint = "Search...",
                )
            }
        },
        navigationIcon = {
            if (isSearching) {
                IconButton(
                    onClick = {
                        isSearching = false
                        filterAppValue.value = ""
                    }
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Cancel search"
                    )
                }
            } else {
                Box(modifier = Modifier.padding(all = 5.dp)) {
                    Switch(
                        checked = gnirehtetEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                Gnirehtet.start(context, vpnRequestReceiver)
                            } else {
                                Gnirehtet.stop(context)
                            }
                        },
                        modifier = Modifier.semantics {
                            stateDescription = "Gnirehtet " + (if (gnirehtetEnabled) "enabled" else "disabled")
                            contentDescription = "Toggle Gnirehtet"
                        },
                    )
                }
            }
        },
        actions = {
            if (isSearching) {
                IconButton(
                    onClick = { filterAppValue.value = "" }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear search"
                    )
                }
            } else {
                IconButton(
                    onClick = { isSearching = true }
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search apps"
                    )
                }
                Box {
                    var showMenu by rememberSaveable { mutableStateOf(false) }

                    IconButton(
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filter apps"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(text = "Show system apps", state = showSystemApps)
                        DropdownMenuItem(text = "Show disabled apps", state = showDisabledApps)
                        DropdownMenuItem(text = "Show apps without internet permission", state = showAppsWithoutInternetPermission)
                    }
                }
                IconButton(
                    onClick = { navController.navigate(Views.Settings) }
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Open settings"
                    )
                }
            }
        }
    )
}

@Composable
fun DropdownMenuItem(text: String, state: MutableState<Boolean>) {
    DropdownMenuItem(
        text = { Text(text = text) },
        trailingIcon = {
            Checkbox(
                checked = state.value,
                onCheckedChange = null,
            )
        },
        onClick = { state.value = !state.value },
    )
}

// https://stackoverflow.com/questions/73664765/showing-a-text-field-in-the-app-bar-in-jetpack-compose-with-material3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textStyle = LocalTextStyle.current
    // make sure there is no background color in the decoration box
    val colors = TextFieldDefaults.colors(
        unfocusedContainerColor = Color.Unspecified,
        focusedContainerColor = Color.Unspecified,
        disabledContainerColor = Color.Unspecified,
        errorContainerColor = Color.Unspecified,
    )

    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        MaterialTheme.colorScheme.onSurface
    }
    val fontSize = 18.sp
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor, lineHeight = 50.sp, fontSize = fontSize))

    // request focus when this composable is first initialized
    val focusRequester = FocusRequester()
    SideEffect {
        focusRequester.requestFocus()
    }

    // set the correct cursor position when this composable is first initialized
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    textFieldValue = textFieldValue.copy(text = value) // make sure to keep the value updated

    CompositionLocalProvider(
        LocalTextSelectionColors provides LocalTextSelectionColors.current
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                // remove newlines to avoid strange layout issues, and also because singleLine=true
                onValueChange(it.text.replace("\n", ""))
            },
            modifier = modifier
                .fillMaxWidth()
                .heightIn(32.dp)
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                )
                .focusRequester(focusRequester),
            textStyle = mergedTextStyle,
            cursorBrush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                0.05f to Color.Transparent,
                0.05f to MaterialTheme.colorScheme.primary,
                0.95f to MaterialTheme.colorScheme.primary,
                0.95f to Color.Transparent,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = true,
            decorationBox = { innerTextField ->
                // places text field with placeholder and appropriate bottom padding
                TextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = { Text(text = hint, fontSize = fontSize) },
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    contentPadding = PaddingValues(bottom = 4.dp)
                )
            }
        )
    }
}
