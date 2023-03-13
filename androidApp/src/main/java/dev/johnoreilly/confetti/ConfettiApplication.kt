package dev.johnoreilly.confetti

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.work.RefreshWorker
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory

class ConfettiApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Firebase.crashlytics.setCustomKeys {
                key("appName", "androidApp")
            }
        }

        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)

            workManagerFactory()
        }

        setupDailyRefresh()
    }

    private fun setupDailyRefresh() {
        get<WorkManager>().enqueueUniquePeriodicWork(
            RefreshWorker.WorkDaily,
            ExistingPeriodicWorkPolicy.UPDATE,
            RefreshWorker.dailyRefresh()
        )
    }
}