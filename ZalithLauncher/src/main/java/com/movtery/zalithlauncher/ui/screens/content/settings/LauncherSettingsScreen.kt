/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.settings

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.VideoPreferences
import com.movtery.zalithlauncher.contract.MediaPickerContract
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.AppLanguage
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.enums.applyLanguage
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.EnumSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.utils.settings.SettingsTransferUtils
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LauncherSettingsScreen"

private sealed interface CustomColorOperation {
    data object None : CustomColorOperation
    data object Dialog : CustomColorOperation
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LauncherSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    toHomePageEditor: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Launcher, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                val importLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            val success = SettingsTransferUtils.importData(context, listOf(it)) // ✅ تم التصحيح
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    if (success) R.string.settings_import_success else R.string.settings_import_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SettingsCard(
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_export),
                        onClick = {
                            val activity = context as? android.app.Activity ?: return@SettingsCard
                            checkStoragePermissions(
                                activity = activity,
                                title = R.string.storage_permission_request_title,
                                message = context.getString(R.string.storage_permission_request_message),
                                hasPermission = {
                                    coroutineScope.launch {
                                        val file = SettingsTransferUtils.exportSettings(context)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) {
                                                Toast.makeText(context, context.getString(R.string.settings_export_success, file.absolutePath), Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, R.string.settings_export_failed, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    )
                    SettingsCard(
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_import),
                        onClick = {
                            importLauncher.launch("application/json")
                        }
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    var customColorOperation by remember { mutableStateOf<CustomColorOperation>(CustomColorOperation.None) }
                    CustomColorOperation(
                        customColorOperation = customColorOperation,
                        updateOperation = { customColorOperation = it }
                    )

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.launcherColorTheme,
                        title = stringResource(R.string.settings_launcher_color_theme_title),
                        summary = stringResource(R.string.settings_launcher_color_theme_summary),
                        entries = ColorThemeType.entries,
                        getRadioEnable = { enum ->
                            if (enum == ColorThemeType.DYNAMIC) Build.VERSION.SDK_INT >= Build.VERSION_CODES.S else true
                        },
                        getRadioText = { enum ->
                            when (enum) {
                                ColorThemeType.DYNAMIC -> stringResource(R.string.theme_color_dynamic)
                                ColorThemeType.EMBERMIRE -> stringResource(R.string.theme_color_embermire)
                                ColorThemeType.VELVET_ROSE -> stringResource(R.string.theme_color_velvet_rose)
                                ColorThemeType.MISTWAVE -> stringResource(R.string.theme_color_mistwave)
                                ColorThemeType.GLACIER -> stringResource(R.string.theme_color_glacier)
                                ColorThemeType.VERDANTFIELD -> stringResource(R.string.theme_color_verdant_field)
                                ColorThemeType.URBAN_ASH -> stringResource(R.string.theme_color_urban_ash)
                                ColorThemeType.VERDANT_DAWN -> stringResource(R.string.theme_color_verdant_dawn)
                                ColorThemeType.CUSTOM -> stringResource(R.string.generic_custom)
                            }
                        },
                        maxItemsInEachRow = 5,
                        onRadioClick = { enum ->
                            if (enum == ColorThemeType.CUSTOM) customColorOperation = CustomColorOperation.Dialog
                        }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherDarkMode,
                        items = DarkMode.entries,
                        title = stringResource(R.string.settings_launcher_dark_mode_title),
                        getItemText = { stringResource(it.textRes) }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherLanguage,
                        items = AppLanguage.entries,
                        title = stringResource(R.string.settings_launcher_language),
                        getItemText = { stringResource(it.textRes) },
                        onValueChange = {
                            applyLanguage(it)
                        }
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherFestivalEffects,
                        title = stringResource(R.string.settings_launcher_festivals_effects_title),
                        summary = stringResource(R.string.settings_launcher_festivals_effects_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.launcherFullScreen,
                        title = stringResource(R.string.settings_launcher_full_screen_title),
                        summary = stringResource(R.string.settings_launcher_full_screen_summary)
                    )
                }
            }

            //启动器背景设置板块
            LocalBackgroundViewModel.current?.let { backgroundViewModel ->
                AnimatedItem(scope) { yOffset ->
                    SettingsCardColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                    ) {
                        SettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top
                        ) {
                            CustomBackground(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundViewModel = backgroundViewModel,
                                submitError = submitError
                            )
                        }

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.launcherBackgroundOpacity,
                            title = stringResource(R.string.settings_launcher_background_opacity_title),
                            summary = stringResource(R.string.settings_launcher_background_opacity_summary),
                            valueRange = AllSettings.launcherBackgroundOpacity.floatRange,
                            suffix = "%",
                            enabled = backgroundViewModel.isValid,
                            fineTuningControl = true
                        )

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.videoBackgroundVolume,
                            title = stringResource(R.string.settings_launcher_background_video_volume_title),
                            summary = stringResource(R.string.settings_launcher_background_video_volume_summary),
                            valueRange = AllSettings.videoBackgroundVolume.floatRange,
                            suffix = "%",
                            enabled = backgroundViewModel.isValid && backgroundViewModel.isVideo,
                            fineTuningControl = true
                        )

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Bottom,
                            unit = AllSettings.backgroundBlur,
                            title = stringResource(R.string.settings_title_blur),
                            summary = stringResource(R.string.settings_launcher_background_blur_summary),
                            valueRange = AllSettings.backgroundBlur.floatRange,
                            suffix = "Dp",
                            enabled = backgroundViewModel.isValid,
                            fineTuningControl = true
                        )
                    }
                }
            }

            // ✅ إعدادات فيديو شاشة الإقلاع
            AnimatedItem(scope) { yOffset ->
                var hasSplashVideo by remember { mutableStateOf(VideoPreferences.getVideoUri(context) != null) }
                val splashVideoPicker = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        VideoPreferences.saveVideoUri(context, it.toString())
                        hasSplashVideo = true
                        Toast.makeText(context, R.string.settings_splash_video_saved, Toast.LENGTH_SHORT).show()
                    }
                }

                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = if (hasSplashVideo) CardPosition.Top else CardPosition.Bottom,
                        title = stringResource(R.string.settings_splash_video_title),
                        onClick = { splashVideoPicker.launch("video/*") }
                    )
                    if (hasSplashVideo) {
                        SettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Bottom,
                            title = stringResource(R.string.settings_splash_video_reset),
                            onClick = {
                                VideoPreferences.clearVideoUri(context)
                                hasSplashVideo = false
                                Toast.makeText(context, R.string.settings_splash_video_removed, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomColorOperation(
    customColorOperation: CustomColorOperation,
    updateOperation: (CustomColorOperation) -> Unit
) {
    when (customColorOperation) {
        CustomColorOperation.None -> {}
        CustomColorOperation.Dialog -> {
        }
    }
}

@Composable
private fun CustomBackground(
    modifier: Modifier = Modifier,
    backgroundViewModel: BackgroundViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val mediaPicker = rememberLauncherForActivityResult(
        contract = MediaPickerContract()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    backgroundViewModel.import(context, it)
                } catch (e: Exception) {
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.error_import_file), // ✅ إضافة title
                            message = androidText(e.message ?: "Unknown error")
                        )
                    )
                }
            }
        }
    }

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = stringResource(R.string.settings_launcher_background_title))
            Text(text = stringResource(R.string.settings_launcher_background_summary))
        }
        Button(
            onClick = {
                mediaPicker.launch(Unit)
            }
        ) {
            Text(text = stringResource(R.string.generic_select))
        }
    }
}
