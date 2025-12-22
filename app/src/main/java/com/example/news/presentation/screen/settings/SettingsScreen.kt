package com.example.news.presentation.screen.settings

import android.Manifest
import android.R.attr.subtitle
import android.os.Build
import android.util.Log.v
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.news.R


@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    SettingsContent(
        onBackClick = onBackClick
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.processCommand(SettingsCommand.SetNotificationsEnabled(it))
        }
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                onBackClick()
                            },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) { innerPadding ->
        val state by viewModel.state.collectAsState()
        when (val currentState = state) {
            is SettingsState.Configuration -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsCard(
                            title = stringResource(R.string.search_language),
                            subtitle = stringResource(R.string.select_language_for_news_search),
                        ) {
                            SettingsDropDown(
                                items = currentState.languages,
                                selectedItem = currentState.language,
                                onItemSelected = {
                                    viewModel.processCommand(SettingsCommand.SelectLanguage(it))
                                },
                                itemAsString = {
                                    it.toReadableFormat()
                                }
                            )
                        }
                    }
                    item {
                        SettingsCard(
                            title = stringResource(R.string.update_interval),
                            subtitle = stringResource(R.string.how_often_to_update_news),
                        ) {
                            SettingsDropDown(
                                items = currentState.intervals,
                                selectedItem = currentState.interval,
                                onItemSelected = {
                                    viewModel.processCommand(SettingsCommand.SelectInterval(it))
                                },
                                itemAsString = {
                                    it.toReadableFormat()
                                }
                            )
                        }
                    }
                    item {
                        SettingsCard(
                            title = stringResource(R.string.notifications),
                            subtitle = stringResource(R.string.show_notifications_about_new_articles),
                        ) {
                            Switch(
                                checked = currentState.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.processCommand(
                                                SettingsCommand.SetNotificationsEnabled(
                                                    true
                                                )
                                            )
                                        }
                                    } else {
                                        viewModel.processCommand(
                                            SettingsCommand.SetNotificationsEnabled(
                                                false
                                            )
                                        )

                                    }
                                }
                            )
                        }
                    }

                    item {
                        SettingsCard(
                            title = stringResource(R.string.update_only_via_wi_fi),
                            subtitle = stringResource(R.string.save_mobile_data),
                        ) {
                            Switch(
                                checked = currentState.wifiOnly,
                                onCheckedChange = {
                                    viewModel.processCommand(SettingsCommand.SetWifiOnly(it))
                                }
                            )
                        }
                    }
                }
            }

            SettingsState.Initial -> {
            }
        }
    }
}