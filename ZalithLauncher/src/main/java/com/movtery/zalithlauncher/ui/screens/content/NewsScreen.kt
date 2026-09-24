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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.defaultMarkdownConfig
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * رابط ملف الأخبار
 */
private const val NEWS_URL = "https://raw.githubusercontent.com/hassan191-w/ZalithLauncher2Plus/refs/heads/main/NEWS.md"

/**
 * شاشة الأخبار
 * تعرض محتوى NEWS.md من GitHub
 */
@Composable
fun NewsScreen(
    key: NormalNavKey.News,
    backStackViewModel: ScreenBackStackViewModel
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) {
        var content by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var reloadTrigger by remember { mutableStateOf(0) }

        LaunchedEffect(reloadTrigger) {
            isLoading = true
            try {
                val text = withContext(Dispatchers.IO) {
                    java.net.URL(NEWS_URL).readText()
                }
                content = text
            } catch (_: Exception) {
                content = null
            }
            isLoading = false
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoadingIndicator()
                            Text(
                                text = "جاري تحميل الأخبار...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                content == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "فشل تحميل الأخبار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحقق من اتصالك بالإنترنت وحاول مرة أخرى",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { reloadTrigger++ },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(text = "🔄 إعادة المحاولة")
                            }
                        }
                    }
                }
                else -> {
                    val config = defaultMarkdownConfig()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        MarkdownView(
                            content = content!!,
                            modifier = Modifier.fillMaxWidth(),
                            config = config
                        )
                    }
                }
            }
        }
    }
}
