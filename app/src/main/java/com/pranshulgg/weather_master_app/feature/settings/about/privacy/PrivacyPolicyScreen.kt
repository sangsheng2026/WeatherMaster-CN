package com.pranshulgg.weather_master_app.feature.settings.about.privacy

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
fun PrivacyPolicyScreen(navController: NavController) {

    val repoUrl = "https://github.com/sangsheng2026/WeatherMaster-CN"

    LargeTopBarScaffold(
        title = stringResource(R.string.about_privacy_policy),
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
                text = "「WeatherMaster 大陆版」是开源软件，没有账号系统，也没有服务器保存你的数据。\n\n" +
                        "这个 App 本身：",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Gap(6.dp)
            Text(
                text = "· 不收集、不存储、不分享你的个人信息\n" +
                        "· 不追踪你的 IP、使用行为或设备标识\n" +
                        "· 不向本项目维护者回传任何数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "第三方服务",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = "获取天气、空气质量、预警和城市搜索时，App 会直接连接你选择的数据源" +
                        "（例如中国天气网、Open-Meteo、geonames 等）。这些请求会带上你的坐标或搜索词，" +
                        "因此对方可能看到你的 IP 地址和大致位置。这部分由各家服务商按自己的隐私政策处理，" +
                        "本项目无法控制，也不保存这些数据。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "位置权限",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = "· 定位权限是可选的，拒绝授权也能手动搜索城市\n" +
                        "· 坐标只在本机用来请求天气，不会被本项目保存到服务器\n" +
                        "· 中国境内的城市名由内置的离线行政区划数据解析，这一步完全在本机完成、不联网",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "本机保存的内容",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = "你添加的位置、界面设置、天气预报的缓存都只存在手机本地数据库里。" +
                        "卸载 App 即随之删除；你也可以在「备份与恢复」里导出或清除。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "儿童",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = "本 App 不面向 13 岁以下儿童，也不会主动收集儿童的个人信息。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                "变更",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Gap(8.dp)
            Text(
                text = "隐私政策可能随版本更新调整，改动会发布在项目仓库里。",
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
                    append("关于隐私有疑问，请到 ")
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