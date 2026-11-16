package com.pranshulgg.weather_master_app.feature.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pranshulgg.weather_master_app.BuildConfig
import com.pranshulgg.weather_master_app.R
import com.pranshulgg.weather_master_app.core.ui.components.Gap


@Composable
fun ChangelogContent(hideSheet: () -> Unit) {
    Column(Modifier.heightIn(max = 700.dp)) {
        Text(
            "更新记录 ${BuildConfig.APP_VERSION}",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        HorizontalDivider()

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Gap(5.dp)
            ChangelogBox {
                ChangelogHeader("本版（3.11.0-cn3）")
                ChangelogText("不再检查新版本，也不再弹「有新版本可用」提示")
                ChangelogText("手动刷新不再有 15 分钟冷却，想刷就刷")
            }
            ChangelogBox {
                ChangelogHeader("上一版（3.11.0-cn2）")
                ChangelogText("位置在中国时，天气数据源默认使用中国天气网，不用再手动改")
                ChangelogText("设置里的「支持」换成了「项目仓库」，可以直接跳去看源码、下载新版、提 issue")
                ChangelogText("服务条款与隐私政策改成中文，并按本分发版的实际情况重写")
                ChangelogText("外观默认值调整：跟随系统主题、开启动态配色、关闭小青蛙布局")
                ChangelogText("「后台更新」页只保留电池优化等工具，去掉自动刷新开关和更新间隔")
                ChangelogText("移除「加入 Discord 社区」入口")
            }
            ChangelogBox {
                ChangelogHeader("3.11.0-cn1")
                ChangelogText("地名查询不再阻塞天气请求，首次加载从 32 秒降到 4 秒")
                ChangelogText("内置 509 个中国省市中心点，城市名改为离线解析")
                ChangelogText("修复首页地名更新后不刷新的问题")
                ChangelogText("刷新转圈只跟天气请求走")
            }
            ChangelogBox {
                ChangelogHeader("说明")
                ChangelogText("本项目基于上游 WeatherMaster，上游的完整更新日志见项目仓库 Releases 页面")
            }
        }

        Button(
            onClick = hideSheet, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shapes = ButtonDefaults.shapes()
        ) {
            Text(stringResource(R.string.action_ok))
        }
    }
}

@Composable
private fun ChangelogBox(content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = 5.dp, horizontal = 16.dp)) {
        content()
    }
}

@Composable
private fun ChangelogText(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            "•",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text.trim(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ChangelogSubText(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(start = 12.dp)
    ) {
        Text(
            "•",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text.trim(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ChangelogHeader(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 3.dp)
    )
}