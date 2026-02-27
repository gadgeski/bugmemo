package com.gadgeski.bugmemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiDeckViewModel @Inject constructor() : ViewModel() {

    private val _aiLogStream = MutableStateFlow("> System initialized.\n> Awaiting Agent commands...\n")
    val aiLogStream: StateFlow<String> = _aiLogStream.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    fun startAnalysisMock() {
        if (_isStreaming.value) return
        viewModelScope.launch {
            _isStreaming.value = true
            val mockLogs = listOf(
                "> Analyzing stack trace...",
                "> NullPointerException detected in NoteRepository.kt:42",
                "> Proposal: Implement safe call operator and let block.",
            )
            _aiLogStream.value = ""
            mockLogs.forEach { log ->
                log.forEach { char ->
                    _aiLogStream.value += char
                    delay(20)
                }
                _aiLogStream.value += "\n"
                delay(300)
            }
            _isStreaming.value = false
        }
    }

    fun approveAction() {
        viewModelScope.launch {
            _aiLogStream.value += "\n> Action APPROVED. Patching target file..."
        }
    }

    fun rejectAction() {
        viewModelScope.launch {
            _aiLogStream.value += "\n> Action REJECTED. Discarding diff."
        }
    }
}
