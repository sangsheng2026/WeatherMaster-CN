package com.pranshulgg.weather_master_app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.weather_master_app.R
import com.pranshulgg.weather_master_app.core.ui.components.BottomNavSpacing
import com.pranshulgg.weather_master_app.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.weather_master_app.core.ui.components.NavigateUpBtn
import com.pranshulgg.weather_master_app.core.ui.components.SettingSection
import com.pranshulgg.weather_master_app.core.ui.components.SettingTile
import com.pranshulgg.weather_master_app.core.ui.components.SettingsTileIcon
import com.pranshulgg.weather_master_app.core.ui.navigation.NavRoutes
import com.pranshulgg.weather_master_app.core.utils.locale.getCurrentAppLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    val uriHandler = LocalUriHandler.current

    LargeTopBarScaffold(
        title = stringResource(R.string.settings),
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
                tiles = listOf(
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.format_paint_24px) },
                        title = stringResource(R.string.setting_appearance),
                        description = stringResource(R.string.setting_appearance_secondary),
                        onClick = { navController.navigate(NavRoutes.APPEARANCE) }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.github_invertocat_black) },
                        title = "项目仓库",
                        description = "查看源码、下载新版、反馈问题",
                        onClick = {
                            uriHandler.openUri("https://github.com/sangsheng2026/WeatherMaster-CN")
                        }
                    )

                )
            )
            SettingSection(
                tiles = listOf(
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.sync_24px) },
                        title = stringResource(R.string.setting_background_updates),
                        description = stringResource(R.string.setting_background_updates_secondary),
                        onClick = { navController.navigate(NavRoutes.BACKGROUND_UPDATES) }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.nest_farsight_weather_24px) },
                        title = stringResource(R.string.weather_sources),
                        description = stringResource(R.string.setting_weather_sources_secondary),
                        onClick = {
                            navController.navigate(NavRoutes.SOURCES)
                        }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.language_24px) },
                        title = stringResource(R.string.setting_language),
                        description = getCurrentAppLocale().displayName,
                        onClick = { navController.navigate(NavRoutes.LANGUAGE) }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.notifications_unread_24px) },
                        title = stringResource(R.string.settings_notifications),
                        description = stringResource(R.string.settings_notifications_secondary),
                        onClick = { navController.navigate(NavRoutes.NOTIFICATIONS) }
                    ),
                )
            )
            SettingSection(
                tiles = listOf(
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.cloud_download_24px) },
                        title = stringResource(R.string.setting_backup_restore),
                        description = stringResource(R.string.setting_backup_restore_secondary),
                        onClick = { navController.navigate(NavRoutes.BACKUP_RESTORE) }
                    ),
                    SettingTile.ActionTile(
                        leading = { SettingsTileIcon(R.drawable.info_24px) },
                        title = stringResource(R.string.setting_about_app),
                        description = stringResource(R.string.setting_about_app_secondary),
                        onClick = {
                            navController.navigate(NavRoutes.ABOUT)
                        }
                    ),
                )
            )

            BottomNavSpacing(30.dp)
        }
    }
}