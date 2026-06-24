package com.beehive.tracker.presentation.screen.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.data.preferences.GridPreferencesDataSource
import com.beehive.tracker.domain.model.GridPrefs
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.usecase.hive.ArrangeHivesInGridUseCase
import com.beehive.tracker.domain.usecase.hive.CreateHiveUseCase
import com.beehive.tracker.domain.usecase.hive.DeleteHiveUseCase
import com.beehive.tracker.domain.usecase.hive.FindFirstEmptyGridSlotUseCase
import com.beehive.tracker.domain.usecase.hive.GetHivesByApiaryUseCase
import com.beehive.tracker.domain.usecase.hive.MoveHiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getHivesByApiary: GetHivesByApiaryUseCase,
    private val createHive: CreateHiveUseCase,
    private val moveHive: MoveHiveUseCase,
    private val deleteHive: DeleteHiveUseCase,
    private val arrangeInGrid: ArrangeHivesInGridUseCase,
    private val findFirstEmptySlot: FindFirstEmptyGridSlotUseCase,
    gridPrefsSource: GridPreferencesDataSource,
) : ViewModel() {

    private val apiaryId: String = checkNotNull(savedStateHandle["apiaryId"])

    val hives: StateFlow<List<Hive>> = getHivesByApiary(apiaryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gridPrefs: StateFlow<GridPrefs> = gridPrefsSource.gridPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GridPrefs.DEFAULT)

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun firstEmptySlot(cellPx: Float, halfGap: Float): Pair<Float, Float> =
        findFirstEmptySlot(hives.value, gridPrefs.value.cols, gridPrefs.value.rows, cellPx, halfGap)

    fun addHive(name: String, posX: Float, posY: Float) {
        viewModelScope.launch { createHive(apiaryId, name, posX, posY) }
    }

    // Sürükle-bırak sonrası yeni koordinatlar DB'ye yazar
    fun onHiveMoved(hiveId: String, posX: Float, posY: Float) {
        viewModelScope.launch { moveHive(hiveId, posX, posY) }
    }

    // Uzun bas menüsünden kovan sil; Room Flow haritayı otomatik günceller
    fun onDeleteHive(hiveId: String) {
        viewModelScope.launch { deleteHive(hiveId) }
    }

    fun arrangeGrid(cellPx: Float, halfGap: Float) {
        viewModelScope.launch {
            arrangeInGrid(hives.value.map { it.id }, gridPrefs.value.cols, cellPx, halfGap)
        }
    }
}
