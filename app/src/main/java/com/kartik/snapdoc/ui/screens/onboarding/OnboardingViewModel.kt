package com.kartik.snapdoc.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPrefsRepository,
) : ViewModel() {

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingSeen(true)
            onDone()
        }
    }
}
