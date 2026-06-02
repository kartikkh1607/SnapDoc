package com.kartik.snapdoc.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kartik.snapdoc.data.prefs.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPrefsRepository,
) : ViewModel() {

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingSeen(true)
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}
