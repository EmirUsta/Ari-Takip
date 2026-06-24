package com.beehive.tracker.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.data.preferences.AudioPreferencesDataSource
import com.beehive.tracker.data.preferences.GridPreferencesDataSource
import com.beehive.tracker.domain.model.GridPrefs
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.usecase.tag.CreateTagUseCase
import com.beehive.tracker.domain.usecase.tag.DeleteTagUseCase
import com.beehive.tracker.domain.usecase.tag.GetTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getTags: GetTagsUseCase,
    private val createTag: CreateTagUseCase,
    private val deleteTag: DeleteTagUseCase,
    private val gridPrefsSource: GridPreferencesDataSource,
    private val audioPrefsSource: AudioPreferencesDataSource,
) : ViewModel() {

    val tags: StateFlow<List<Tag>> = getTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gridPreferences: StateFlow<GridPrefs> = gridPrefsSource.gridPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GridPrefs.DEFAULT)

    val audioPurgeTtlDays: StateFlow<Int> = audioPrefsSource.purgeTtlDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addTag(label: String, colorHex: String) {
        viewModelScope.launch { createTag(label, colorHex, tags.value.size) }
    }

    fun removeTag(id: String) {
        viewModelScope.launch { deleteTag(id) }
    }

    fun setCols(value: Int) {
        viewModelScope.launch { gridPrefsSource.setCols(value) }
    }

    fun setRows(value: Int) {
        viewModelScope.launch { gridPrefsSource.setRows(value) }
    }

    fun setGap(value: Int) {
        viewModelScope.launch { gridPrefsSource.setGap(value) }
    }

    fun setAudioPurgeTtlDays(days: Int) {
        viewModelScope.launch { audioPrefsSource.setPurgeTtlDays(days) }
    }
}
