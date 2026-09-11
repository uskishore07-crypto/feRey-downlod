package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.Moment
import com.example.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppNavDestination {
    STARTUP_ANIMATION,
    WELCOME,
    ACCOUNT_SETUP,
    MAIN_APP
}

enum class BottomTab {
    HOME,
    TIMELINE,
    REPLAY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val repository: MomentRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MomentRepository(database.momentDao())
    }

    // Navigation & Screen Flow
    private val _appDestination = MutableStateFlow(AppNavDestination.STARTUP_ANIMATION)
    val appDestination: StateFlow<AppNavDestination> = _appDestination.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.HOME)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(userPreferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // User Profile
    private val _username = MutableStateFlow(userPreferences.username)
    val username: StateFlow<String> = _username.asStateFlow()

    // Today Date string: "yyyy-MM-dd"
    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // All moments
    val allMoments: StateFlow<List<Moment>> = repository.allMoments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's moments
    val todayMoments: StateFlow<List<Moment>> = repository.getMomentsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All distinct dates
    val allDates: StateFlow<List<String>> = repository.allDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add Moment modal sheet state
    private val _isAddMomentOpen = MutableStateFlow(false)
    val isAddMomentOpen: StateFlow<Boolean> = _isAddMomentOpen.asStateFlow()

    // Replay State
    private val _isReplayActive = MutableStateFlow(false)
    val isReplayActive: StateFlow<Boolean> = _isReplayActive.asStateFlow()

    private val _replayDate = MutableStateFlow(todayDateString)
    val replayDate: StateFlow<String> = _replayDate.asStateFlow()

    private val _replayMoments = MutableStateFlow<List<Moment>>(emptyList())
    val replayMoments: StateFlow<List<Moment>> = _replayMoments.asStateFlow()

    private val _replayCurrentIndex = MutableStateFlow(0)
    val replayCurrentIndex: StateFlow<Int> = _replayCurrentIndex.asStateFlow()

    private val _isReplayPlaying = MutableStateFlow(true)
    val isReplayPlaying: StateFlow<Boolean> = _isReplayPlaying.asStateFlow()

    fun onStartupAnimationComplete() {
        if (userPreferences.isFirstLaunch && !userPreferences.isLoggedIn) {
            _appDestination.value = AppNavDestination.WELCOME
        } else {
            _appDestination.value = AppNavDestination.MAIN_APP
        }
    }

    fun onGetStartedClicked() {
        _appDestination.value = AppNavDestination.ACCOUNT_SETUP
    }

    fun completeAccountSetup(username: String) {
        val finalUsername = username.trim().ifEmpty { "User" }
        userPreferences.username = finalUsername
        userPreferences.isLoggedIn = true
        userPreferences.isFirstLaunch = false
        _username.value = finalUsername
        _appDestination.value = AppNavDestination.MAIN_APP
    }

    fun continueAsGuest() {
        userPreferences.username = "Guest"
        userPreferences.isLoggedIn = true
        userPreferences.isFirstLaunch = false
        _username.value = "Guest"
        _appDestination.value = AppNavDestination.MAIN_APP
    }

    fun selectTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    fun openAddMoment() {
        _isAddMomentOpen.value = true
    }

    fun closeAddMoment() {
        _isAddMomentOpen.value = false
    }

    fun saveMoment(
        photoUri: String?,
        thoughtText: String?,
        songTitle: String?,
        artistName: String?
    ) {
        val now = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)

        val cleanThought = thoughtText?.trim()?.ifEmpty { null }
        val cleanSong = songTitle?.trim()?.ifEmpty { null }
        val cleanArtist = artistName?.trim()?.ifEmpty { null }
        val cleanPhoto = photoUri?.trim()?.ifEmpty { null }

        // Determine primary type
        val type = when {
            cleanPhoto != null && (cleanThought != null || cleanSong != null) -> "COMBINED"
            cleanPhoto != null -> "PHOTO"
            cleanSong != null -> "MUSIC"
            else -> "THOUGHT"
        }

        val moment = Moment(
            timestamp = System.currentTimeMillis(),
            dateString = dateStr,
            timeString = timeStr,
            type = type,
            thoughtText = cleanThought,
            imageUri = cleanPhoto,
            songTitle = cleanSong,
            artistName = cleanArtist,
            userId = _username.value
        )

        viewModelScope.launch {
            repository.insertMoment(moment)
            _isAddMomentOpen.value = false
        }
    }

    fun deleteMoment(moment: Moment) {
        viewModelScope.launch {
            repository.deleteMoment(moment)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun setThemeMode(mode: String) {
        userPreferences.themeMode = mode
        _themeMode.value = mode
    }

    fun logOut() {
        userPreferences.clearUser()
        userPreferences.isFirstLaunch = true
        _username.value = ""
        _appDestination.value = AppNavDestination.WELCOME
    }

    // Replay Mode controls
    fun startReplayForDate(dateString: String, moments: List<Moment>) {
        if (moments.isNotEmpty()) {
            _replayDate.value = dateString
            _replayMoments.value = moments
            _replayCurrentIndex.value = 0
            _isReplayPlaying.value = true
            _isReplayActive.value = true
        }
    }

    fun stopReplay() {
        _isReplayActive.value = false
        _isReplayPlaying.value = false
    }

    fun nextReplayMoment() {
        val current = _replayCurrentIndex.value
        if (current < _replayMoments.value.size - 1) {
            _replayCurrentIndex.value = current + 1
        } else {
            // Finished
            _isReplayPlaying.value = false
        }
    }

    fun prevReplayMoment() {
        val current = _replayCurrentIndex.value
        if (current > 0) {
            _replayCurrentIndex.value = current - 1
        }
    }

    fun toggleReplayPlayPause() {
        _isReplayPlaying.value = !_isReplayPlaying.value
    }

    fun setReplayIndex(index: Int) {
        if (index in _replayMoments.value.indices) {
            _replayCurrentIndex.value = index
        }
    }

    fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
