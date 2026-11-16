package com.pranshulgg.weather_master_app.feature.settings.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.weather_master_app.R
import com.pranshulgg.weather_master_app.core.prefs.helper.PreferencesHelper
import com.pranshulgg.weather_master_app.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.weather_master_app.core.ui.components.NavigateUpBtn
import com.pranshulgg.weather_master_app.core.ui.components.SettingSection
import com.pranshulgg.weather_master_app.core.ui.components.SettingTile
import com.pranshulgg.weather_master_app.core.ui.components.SettingsTileIcon
import com.pranshulgg.weather_master_app.core.ui.navigation.NavRoutes
import com.pranshulgg.weather_master_app.core.ui.snackbar.SnackbarManager
import com.pranshulgg.weather_master_app.data.worker.gadgetbridge.isGadgetbridgeInstalled
import com.pranshulgg.weather_master_app.feature.settings.background.batteryoptimization.BatteryOptimizationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundUpdatesScreen(navController: NavController) {
    val context = LocalContext.current

    val uriHandler = LocalUriHandler.current

    var isSendDataToGadgetBridge by remember {
        mutableStateOf(
            PreferencesHelper.getBool("isSendDataToGadgetbridge") ?: false
        )
    }


    val isGadgetBridgeInstalled = isGadgetbridgeInstalled(context)

    LargeTopBarScaffold(
        title = stringResource(R.string.setting_background_updates),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            SettingSection(
                title = stringResource(R.string.setting_additional),
                tiles = listOf(
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.info_24px) },
                        title = "后台任务信息",
                        onClick = {
                            navController.navigate(NavRoutes.WORKER_INFO)
                        }
                    ),
                    SettingTile.SwitchTile(
                        leading = { SettingsTileIcon(R.drawable.aod_watch_24px) },
                        title = "发送数据到 Gadgetbridge",
                        enabled = isGadgetBridgeInstalled,
                        checked = isSendDataToGadgetBridge,
                        onCheckedChange = {
                            if (!isGadgetBridgeInstalled) {
                                SnackbarManager.show(R.string.gadgetbridge_not_available_error)
                                return@SwitchTile
                            }
                            isSendDataToGadgetBridge = it
                            PreferencesHelper.setBool("isSendDataToGadgetbridge", it)
                        }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.sync_problem_24px) },
                        title = stringResource(R.string.setting_disable_battery_opt_title),
                        description = stringResource(R.string.setting_disable_battery_opt_secondary),
                        onClick = {
                            BatteryOptimizationHelper.requestDisableBatteryOptimization(context)
                        }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.help_24px) },
                        title = stringResource(R.string.setting_dontkillmyapp_title),
                        description = stringResource(R.string.setting_dontkillmyapp_secondary),
                        onClick = { uriHandler.openUri("https://dontkillmyapp.com/") }
                    )
                )
            )
        }
    }
}