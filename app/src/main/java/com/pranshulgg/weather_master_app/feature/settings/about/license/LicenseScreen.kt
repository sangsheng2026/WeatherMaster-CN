package com.pranshulgg.weather_master_app.feature.settings.about.license

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
fun LicenseScreen(navController: NavController) {

    LargeTopBarScaffold(
        title = stringResource(R.string.about_license),
        navigationIcon = { NavigateUpBtn(navController) },
    ) { paddingValues ->

        Column(
            Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "GNU General Public License v3.0",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Gap(8.dp)

            Text(
                text =
                    "本应用基于 GNU 通用公共许可证第 3 版（GPLv3）授权。\n\n" +
                            "你可以在 GPLv3 条款下自由使用、研究、修改和分发本软件。\n\n" +
                            "任何修改版本或再分发的副本也必须保持开源，并附带同一许可证。\n\n" +
                            "本软件按「现状」提供，不附带任何明示或暗示的担保，包括对适销性或特定用途适用性的担保。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "你的权利",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Gap(6.dp)

            Text(
                text =
                    "• 出于任何目的使用本软件\n" +
                            "• 获取并修改源代码\n" +
                            "• 分享本软件的副本\n" +
                            "• 以 GPLv3 分发修改后的版本",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "条件",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Gap(6.dp)

            Text(
                text =
                    "• 再分发时必须附带 GPLv3 许可证\n" +
                            "• 修改版本必须清楚说明所做的更改\n" +
                            "• 衍生作品也必须以 GPLv3 授权\n" +
                            "• 源代码必须保持可获取",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "完整许可证",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Gap(6.dp)

            Text(
                text = buildAnnotatedString {
                    append("你可以在以下地址阅读完整的 GNU GPLv3 许可证：")
                    withLink(
                        LinkAnnotation.Url("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    ) {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.tertiary
                            ),

                            ) {
                            append("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Gap(WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 30.dp)

        }
    }
}