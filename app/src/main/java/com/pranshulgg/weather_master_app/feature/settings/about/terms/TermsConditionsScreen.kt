package com.pranshulgg.weather_master_app.feature.settings.about.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pranshulgg.weather_master_app.R
import com.pranshulgg.weather_master_app.core.ui.components.Gap
import com.pranshulgg.weather_master_app.core.ui.components.LargeTopBarScaffold
import com.pranshulgg.weather_master_app.core.ui.components.NavigateUpBtn

@Composable
fun TermsConditionsScreen(navController: NavController) {

    val repoUrl = "https://github.com/sangsheng2026/WeatherMaster-CN"

    LargeTopBarScaffold(
        title = stringResource(R.string.about_terms_conditions),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { paddingValues ->

        Column(
            Modifier
                .padding(top = paddingValues.calculateTopPadding(), start = 16.dp, end = 16.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Text(
                text = buildAnnotatedString {
                    append("本条款适用于「WeatherMaster 大陆版」这个 App。\n\n")
                    append("本项目是基于 ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("PranshulGG/WeatherMaster")
                    }
                    append(" 的二次分发，遵循 ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("GPL-3.0")
                    }
                    append(" 许可证。使用本 App 即表示你接受以下内容：")
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "使用方式",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "· 本 App 免费、开源，按「现状」提供，不承诺一定能用、一定准、一定不出错。\n" +
                        "· 你可以自由使用、修改和分发，但需要遵守 GPL-3.0 许可证的条款。\n" +
                        "· 不要把本项目说成是原作者的官方版本，也不要在未获许可的情况下使用原项目的名称或商标。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "数据与隐私",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "· 本 App 不收集、不存储、不上传你的个人信息。\n" +
                        "· 位置权限是可选的，只用于在你本机上查询天气，不会发送给本项目维护者。\n" +
                        "· 中国境内的城市名来自 App 内置的离线数据，这个判断过程不需要联网。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "天气数据",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "天气、空气质量、预警数据都来自你选择的数据源（中国天气网、Open-Meteo 等）。" +
                        "这些数据由对应的服务提供方生成，本项目只做展示，不对其准确性负责。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "免责",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "· 因使用本 App 产生的任何直接或间接损失，维护者不承担责任。\n" +
                        "· 你需要自行保证设备兼容、网络可用、电量充足。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "更新与可用性",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "· 本 App 会不定期更新，新版本发布在项目仓库的 Releases 页面。\n" +
                        "· 不保证在所有系统版本上都一直可用、一直维护。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "条款变更",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                "本条款可能随版本更新而调整，改动会发布在项目仓库里。继续使用即视为接受修改后的条款。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "联系",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = buildAnnotatedString {
                    append("有问题请到 ")
                    withLink(LinkAnnotation.Url(repoUrl)) {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            append(repoUrl)
                        }
                    }
                    append(" 提 issue。")
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Gap(WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 30.dp)

        }
    }
}