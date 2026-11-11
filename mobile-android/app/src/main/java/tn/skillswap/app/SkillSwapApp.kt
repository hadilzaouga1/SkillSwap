package tn.skillswap.app

import android.app.Application
import tn.skillswap.app.data.Repository

class SkillSwapApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = Repository(this)
    }
}
