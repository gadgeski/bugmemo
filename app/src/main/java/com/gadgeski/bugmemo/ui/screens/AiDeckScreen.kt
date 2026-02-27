package com.gadgeski.bugmemo.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.gadgeski.bugmemo.ui.AiDeckViewModel
import com.gadgeski.bugmemo.ui.components.deck.DeckConsole
import com.gadgeski.bugmemo.ui.components.deck.DeckMonitor
import com.gadgeski.bugmemo.ui.components.deck.DeepNavy
import kotlinx.coroutines.flow.map

@Composable
fun AiDeckScreen(
    viewModel: AiDeckViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    var isHalfOpened by remember { mutableStateOf(false) }

    // [Rule Compliance: Consume onNavigateBack via system gesture]
    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(context) {
        val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
        windowInfoTracker.windowLayoutInfo(context as Activity)
            .map { layoutInfo ->
                layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
            }
            .collect { foldingFeature ->
                isHalfOpened = foldingFeature?.state == FoldingFeature.State.HALF_OPENED
            }
    }

    val logStream by viewModel.aiLogStream.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        AnimatedContent(
            targetState = isHalfOpened,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "DeckMorphingAnimation",
        ) { halfOpened ->
            if (halfOpened) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DeckMonitor(logText = logStream, modifier = Modifier.weight(1f).padding(16.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    DeckConsole(
                        onApprove = { viewModel.approveAction() },
                        onReject = { viewModel.rejectAction() },
                        onAnalyze = { viewModel.startAnalysisMock() },
                        isStreaming = isStreaming,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    DeckMonitor(logText = logStream, modifier = Modifier.weight(1f).padding(16.dp))
                    DeckConsole(
                        onApprove = { viewModel.approveAction() },
                        onReject = { viewModel.rejectAction() },
                        onAnalyze = { viewModel.startAnalysisMock() },
                        isStreaming = isStreaming,
                        modifier = Modifier.height(200.dp),
                    )
                }
            }
        }
    }
}
