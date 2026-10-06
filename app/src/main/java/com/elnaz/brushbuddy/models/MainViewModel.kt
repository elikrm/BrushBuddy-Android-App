package com.elnaz.brushbuddy.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    val brushingState = MutableStateFlow(BrushingStateEnum.NOT_BRUSHING)
    val elapsedSeconds = MutableStateFlow(0)

    private var timerJob: Job? = null
    fun onHeartClicked(){
        when(brushingState.value){
            BrushingStateEnum.NOT_BRUSHING -> startBrushing()
            BrushingStateEnum.BRUSHING -> stopBrushing()
            else -> resetBrushing()
        }
    }
    private fun startBrushing(){
        brushingState.value = BrushingStateEnum.BRUSHING
        elapsedSeconds.value = 0
        timerJob = viewModelScope.launch{
            while(isActive){
                delay(1000)
                elapsedSeconds.value++
            }
        }
    }
    private fun stopBrushing(){
        timerJob?.cancel()
        brushingState.value = if (elapsedSeconds.value <10) {
            BrushingStateEnum.ABORTED_SESSION
        }else{
            BrushingStateEnum.CONFIRMED_SESSION
        }
    }
    private fun resetBrushing(){
        BrushingStateEnum.NOT_BRUSHING
    }
    val messageToUser: String
        get() = when (brushingState.value) {
            BrushingStateEnum.NOT_BRUSHING -> "Ready to brush"
            BrushingStateEnum.BRUSHING -> "Keep going! You need at least 30 seconds"
            BrushingStateEnum.CONFIRMED_SESSION -> "Great job!"
            BrushingStateEnum.ABORTED_SESSION -> "Session too short"
        }

}