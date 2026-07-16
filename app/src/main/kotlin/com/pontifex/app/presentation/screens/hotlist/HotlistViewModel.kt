package com.pontifex.app.presentation.screens.hotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.db.entity.HotlistCommand
import com.pontifex.app.domain.repository.HotlistRepository
import com.pontifex.app.service.terminal.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotlistViewModel @Inject constructor(
    private val hotlistRepository: HotlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val commands: StateFlow<List<HotlistCommand>> = hotlistRepository.getCommands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = MutableStateFlow(
        listOf("General", "ADB", "Fastboot", "Networking", "File Management", "Debug")
    ).asStateFlow()

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addCommand(command: HotlistCommand) {
        viewModelScope.launch {
            hotlistRepository.addCommand(command)
        }
    }

    fun updateCommand(command: HotlistCommand) {
        viewModelScope.launch {
            hotlistRepository.updateCommand(command)
        }
    }

    fun removeCommand(id: Long) {
        viewModelScope.launch {
            hotlistRepository.removeCommand(id)
        }
    }

    fun executeCommand(command: HotlistCommand) {
        viewModelScope.launch {
            sessionManager.getActiveSession()?.let { session ->
                session.sendCommand(command.command)
            }
        }
    }
}
