package com.pontifex.app.domain.model

sealed interface ContainerState {
    data object Uninitialized : ContainerState
    data class Initializing(val progress: Float) : ContainerState
    data class Ready(val rootUri: String) : ContainerState
    data class Error(val reason: String) : ContainerState
}
