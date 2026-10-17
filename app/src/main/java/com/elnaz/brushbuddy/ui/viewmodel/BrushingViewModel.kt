package com.elnaz.brushbuddy.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elnaz.brushbuddy.data.local.BrushingSession
import com.elnaz.brushbuddy.data.local.BrushingSessionRepository
import kotlinx.coroutines.launch
import com.elnaz.brushbuddy.data.bluetooth.AndroidBluetoothRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

//Bluetooth repository publishes sessions through:
//val confirmedSessions = _confirmedSessions.asSharedFlow()
//BrushingViewModel receives those events and save them using the Room repository.
class BrushingViewModel(
    private val repository: BrushingSessionRepository,
    private val bluetoothRepository: AndroidBluetoothRepository
): ViewModel() {
    init {
        viewModelScope.launch {
            bluetoothRepository.confirmedSessions.collect { session ->
                repository.insertSession(session)
            }
        }
    }
    /* map converts the list into a count.
    stateIn makes that count observable by the Compose UI.
    When another confirmed session is saved, the count updates automatically.
    1. Start collecting database updates when the UI subscribes to totalBrushes.
    2. Keep collecting for another 5 seconds after the last subscriber disappears.
    3. Stop collecting if nobody subscribes again during those 5 seconds.
    4. Resume collecting when the UI subscribes again.*/

    val allSessions: StateFlow<List<BrushingSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun saveConfirmedSession(
        startTime: Long,
        durationSeconds: Int
    ) {
        if (durationSeconds <= 30) return
        viewModelScope.launch {
            val session = BrushingSession(
                startTime = startTime,
                durationSeconds = durationSeconds
            )
            repository.insertSession(session)
        }
    }
}