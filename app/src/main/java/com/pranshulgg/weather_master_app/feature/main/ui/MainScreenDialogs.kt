package com.pranshulgg.weather_master_app.feature.main.ui

import androidx.compose.runtime.Composable
import com.pranshulgg.weather_master_app.core.ui.components.TextAlertDialog

object MainScreenDialogs {


    @Composable
    fun UnsupportedSelectedSourceDialog(
        show: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        TextAlertDialog(
            show,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            confirmText = "去修改",
            title = "当前数据源不支持",
            message = "当前选择的数据源不支持你的所在地区，请换一个数据源"
        )
    }

}