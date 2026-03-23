package com.example.tidy

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppLifecycleObserver(
    private val exportManager: ExportManager
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStop(owner: LifecycleOwner) { // TODO this should not just work on Onstop
        // Triggered when app goes to background
        scope.launch {
            exportManager.exportSilently()
        }
    }
}