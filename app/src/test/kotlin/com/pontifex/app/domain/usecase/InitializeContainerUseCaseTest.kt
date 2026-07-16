package com.pontifex.app.domain.usecase

import com.pontifex.app.domain.model.ContainerState
import com.pontifex.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitializeContainerUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: InitializeContainerUseCase

    @Before
    fun setup() {
        settingsRepository = mock()
    }

    @Test
    fun `invoke returns Uninitialized when no URI set`() = runTest {
        whenever(settingsRepository.getContainerUri()).thenReturn(flowOf(null))

        useCase = InitializeContainerUseCase(
            mock(),
            mock(),
            mock(),
            mock(),
            settingsRepository
        )

        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(ContainerState.Uninitialized, result.getOrNull())
    }

    @Test
    fun `invoke returns Uninitialized when empty URI`() = runTest {
        whenever(settingsRepository.getContainerUri()).thenReturn(flowOf(""))

        useCase = InitializeContainerUseCase(
            mock(),
            mock(),
            mock(),
            mock(),
            settingsRepository
        )

        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(ContainerState.Uninitialized, result.getOrNull())
    }

    @Test
    fun `invoke returns Ready when valid URI exists`() = runTest {
        val uri = tempFolder.root.absolutePath
        whenever(settingsRepository.getContainerUri()).thenReturn(flowOf(uri))

        val containerManager = mock<com.pontifex.app.data.binary.ContainerManager>()
        whenever(containerManager.isContainerValid(uri)).thenReturn(true)

        useCase = InitializeContainerUseCase(
            mock(),
            containerManager,
            mock(),
            mock(),
            settingsRepository
        )

        val result = useCase()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() is ContainerState.Ready)
    }

    @Test
    fun `invoke returns error when URI invalid`() = runTest {
        whenever(settingsRepository.getContainerUri()).thenReturn(flowOf("invalid://path"))

        val containerManager = mock<com.pontifex.app.data.binary.ContainerManager>()
        whenever(containerManager.isContainerValid("invalid://path")).thenReturn(false)

        useCase = InitializeContainerUseCase(
            mock(),
            containerManager,
            mock(),
            mock(),
            settingsRepository
        )

        val result = useCase()
        assertTrue(result.isFailure)
    }
}
