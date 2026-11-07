package app.gamenative.wow.product

import android.content.Context

/**
 * The graphics profile picked in Settings, by its label (for example "Xclipse 940"). Null means
 * automatic: the profile detected for this device.
 */
object GpuProfileChoice {
    private const val PREFS = "wow_settings"
    private const val KEY = "gpu_profile"

    @Volatile
    var chosen: String? = null

    fun load(context: Context) {
        chosen = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
    }

    fun save(context: Context, label: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (label == null) remove(KEY) else putString(KEY, label)
        }.apply()
        chosen = label
    }
}
