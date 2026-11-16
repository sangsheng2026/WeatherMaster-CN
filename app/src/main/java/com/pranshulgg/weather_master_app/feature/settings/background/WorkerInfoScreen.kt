package com.pranshulgg.weather_master_app.feature.settings.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pranshulgg.weather_master_app.core.prefs.helper.PreferencesHelper
import com.pranshulgg.weather_master_app.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.weather_master_app.core.ui.components.NavigateUpBtn
import com.pranshulgg.weather_master_app.core.ui.components.SettingSection
import com.pranshulgg.weather_master_app.core.ui.components.SettingTile
import com.pranshulgg.weather_master_app.data.worker.WeatherBackgroundUpdateScheduler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkerInfoScreen(navController: NavController) {
    val context = LocalContext.current

    val workManager = WorkManager.getInstance(context)

    val workInfos by workManager.getWorkInfosForUniqueWorkFlow(WeatherBackgroundUpdateScheduler.WORK_NAME)
        .collectAsState(emptyList())

    val formatter: (Long) -> String = {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val instant = Instant.ofEpochMilli(it)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        formatter.format(dateTime)
    }

    val states = workInfos.filter {
        it.state in setOf(
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.CANCELLED,
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.FAILED
        )
    }

    val cancelled = states.firstOrNull { it.state == WorkInfo.State.CANCELLED }
    val enqueued = states.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
    val failed = states.firstOrNull { it.state == WorkInfo.State.FAILED }
    val succeeded = states.firstOrNull { it.state == WorkInfo.State.SUCCEEDED }

    val lastRun = PreferencesHelper.getLong("LAST_WORKER_SUCCESS_RUN")


    LargeTopBarScaffold(
        title = "后台任务信息",
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
                    SettingTile.TextTile(
                        title = "上次成功运行",
                        description = if (lastRun == null) "从未运行" else formatter(lastRun)
                    )
                )
            )
            if (enqueued != null) {
                SettingSection(
                    title = enqueued.state.name,
                    tiles = listOf(
                        SettingTile.TextTile(
                            title = "尝试次数",
                            description = enqueued.runAttemptCount.toString(),
                        ),
                        SettingTile.TextTile(
                            title = "任务标记",
                            description = enqueued.tags.toString(),
                        ),
                        SettingTile.TextTile(
                            title = "下次计划时间",
                            description = formatter(enqueued.nextScheduleTimeMillis),
                        ),
                    )
                )
            }
            if (cancelled != null) {
                SettingSection(
                    title = cancelled.state.name,
                    tiles = listOf(
                        SettingTile.TextTile(
                            title = "尝试次数",
                            description = cancelled.runAttemptCount.toString(),
                        ),
                        SettingTile.TextTile(
                            title = "任务标记",
                            description = cancelled.tags.toString(),
                        ),
                    )
                )
            }
            if (succeeded != null) {
                SettingSection(
                    title = succeeded.state.name,
                    tiles = listOf(
                        SettingTile.TextTile(
                            title = "尝试次数",
                            description = succeeded.runAttemptCount.toString(),
                        ),
                        SettingTile.TextTile(
                            title = "任务标记",
                            description = succeeded.tags.toString(),
                        ),
                    )
                )
            }
            if (failed != null) {
                SettingSection(
                    title = failed.state.name,
                    tiles = listOf(
                        SettingTile.TextTile(
                            title = "尝试次数",
                            description = failed.runAttemptCount.toString(),
                        ),
                        SettingTile.TextTile(
                            title = "任务标记",
                            description = failed.tags.toString(),
                        ),
                    )
                )
            }
        }

    }
}


