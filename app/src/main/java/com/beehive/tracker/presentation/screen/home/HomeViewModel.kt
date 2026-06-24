package com.beehive.tracker.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.usecase.apiary.CreateApiaryUseCase
import com.beehive.tracker.domain.usecase.apiary.DeleteApiaryUseCase
import com.beehive.tracker.domain.usecase.apiary.ExportApiaryUseCase
import com.beehive.tracker.domain.usecase.apiary.GetApiarysUseCase
import com.beehive.tracker.domain.usecase.apiary.ImportApiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getApiarys: GetApiarysUseCase,
    private val createApiary: CreateApiaryUseCase,
    private val deleteApiary: DeleteApiaryUseCase,
    private val exportApiary: ExportApiaryUseCase,
    private val importApiary: ImportApiaryUseCase,
) : ViewModel() {

    val apiaries: StateFlow<List<Apiary>> = getApiarys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun addApiary(name: String) {
        viewModelScope.launch { createApiary(name) }
    }

    fun deleteApiary(id: String) {
        viewModelScope.launch {
            runCatching { deleteApiary.invoke(id) }
                .onFailure { _snackbarMessage.value = "Silme başarısız: ${it.message}" }
        }
    }

    fun export(apiaryId: String, outputStream: OutputStream) {
        viewModelScope.launch {
            runCatching { exportApiary(apiaryId, outputStream) }
                .onSuccess { _snackbarMessage.value = "Arılık dışa aktarıldı" }
                .onFailure { _snackbarMessage.value = "Dışa aktarma başarısız: ${it.message}" }
        }
    }

    fun import(inputStream: InputStream) {
        viewModelScope.launch {
            runCatching { importApiary(inputStream) }
                .onSuccess { _snackbarMessage.value = "Arılık içe aktarıldı" }
                .onFailure { _snackbarMessage.value = "İçe aktarma başarısız: ${it.message}" }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
