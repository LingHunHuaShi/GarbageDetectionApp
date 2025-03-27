package com.zzh.garbagedetection.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzh.garbagedetection.data.ModelNameEnums
import com.zzh.garbagedetection.data.SettingsViewModel

sealed class SettingItem {
    data class SwitchItem(
        val key: String,
        val title: String,
        val checked: Boolean,
        val onToggle: (Boolean) -> Unit
    ) : SettingItem()

    data class DropdownItem(
        val key: String,
        val title: String,
        val options: List<String>,
        val selected: String,
        val onSelect: (String) -> Unit
    ) : SettingItem()

    data class TextItem(
        val key: String,
        val title: String,
        val textContent: String,
        val textType: KeyboardType,
        val onTextChange: (String) -> Unit
    ) : SettingItem()
}

@Composable
fun SettingsPageContainer(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val modelName by viewModel.modelName.collectAsState()
    val threshold by viewModel.threshold.collectAsState()

    var switchTest by remember{ mutableStateOf(false)}

    val modelSetting = SettingItem.DropdownItem(
        key = "model_name",
        title = "推理模型",
        options = listOf(ModelNameEnums.GOOGLE.label, ModelNameEnums.YOLO.label),
        selected = modelName,
        onSelect = viewModel::setModelName
    )

    val thresholdSetting = SettingItem.TextItem(
        key = "threshold",
        title = "置信度阈值",
        textContent = threshold.toString(),
        textType = KeyboardType.Number,
        onTextChange = { threshold ->
            viewModel.setThreshold(threshold.toFloat())
            Log.d("Settings Page", "threshold number: $threshold")
        }
    )

    val switchTestSetting = SettingItem.SwitchItem(
        key = "switch_test",
        title = "测试",
        checked = switchTest,
        onToggle = {switchTest = !switchTest}
    )

    val settingItems = listOf(modelSetting, thresholdSetting, switchTestSetting)
    SettingsPage(settingItems)
}


@Composable
fun SettingsPage(settingItemList: List<SettingItem>) {
    LazyColumn {
        item { Text(text = "设置", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp)) }
        items(items = settingItemList) { item ->
            when(item) {
                is SettingItem.SwitchItem -> SwitchRow(item)
                is SettingItem.DropdownItem -> DropdownRow(item)
                is SettingItem.TextItem -> TextRow(item)
            }
            HorizontalDivider(Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), 2.dp)
        }
    }
}

@Composable
private fun SwitchRow(item: SettingItem.SwitchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.title, modifier = Modifier.weight(1f))
        Switch(checked = item.checked, onCheckedChange = item.onToggle)
    }
}

@Composable
private fun DropdownRow(item: SettingItem.DropdownItem) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.title, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.selected)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            Box {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .wrapContentWidth(Alignment.End)
                        .align(Alignment.CenterEnd),
                ) {
                    item.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                item.onSelect(option)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TextRow(item: SettingItem.TextItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.title, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = item.textContent,
            onValueChange = item.onTextChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = item.textType),
            modifier = Modifier.weight(0.35f)
        )
    }
}

@Preview
@Composable
fun TextRowPreview() {
    TextRow(SettingItem.TextItem("", "置信度阈值", "", KeyboardType.Number, {}))

}


@Preview
@Composable
fun SettingsPagePreview() {
    var modelName = "Yolo 11m"
    val modelSetting = SettingItem.DropdownItem(
        key = "model_name",
        title = "推理模型",
        options = listOf(ModelNameEnums.GOOGLE.label, ModelNameEnums.YOLO.label),
        selected = modelName,
        onSelect = {}
        )
    val settingItems = listOf(modelSetting)
    SettingsPage(settingItems)
}