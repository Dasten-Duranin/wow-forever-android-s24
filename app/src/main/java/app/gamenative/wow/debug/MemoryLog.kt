package app.gamenative.wow.debug

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import java.io.File
import timber.log.Timber

/**
 * With debug logging on, writes the device's free memory and what the app's processes (the app plus Wine
 * and the game, which run as its children) hold, every [INTERVAL_MS]. When Android's low-memory killer
 * ends the app, its logs just stop, so this trail is what shows where the memory went.
 */
object MemoryLog {
    private const val INTERVAL_MS = 15_000L

    @Volatile private var started = false

    fun start(context: Context) {
        if (started || android.os.Build.FINGERPRINT == "robolectric") return
        started = true
        val app = context.applicationContext
        Thread({
            while (true) {
                if (DebugLog.isEnabled(app)) runCatching { Timber.i(snapshot(app)) }
                Thread.sleep(INTERVAL_MS)
            }
        }, "MemoryLog").apply { isDaemon = true }.start()
    }

    private fun snapshot(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val mb = { bytes: Long -> bytes / (1024 * 1024) }
        val procs = ourProcesses()
        val top = procs.sortedByDescending { it.second }.take(6).joinToString(", ") { "${it.first} ${it.second / 1024} MB" }
        val swap = meminfo("SwapFree")?.let { free -> meminfo("SwapTotal")?.let { total -> ", swap free ${free / 1024}/${total / 1024} MB" } }.orEmpty()
        return "Memory: available ${mb(info.availMem)}/${mb(info.totalMem)} MB (low at ${mb(info.threshold)} MB${if (info.lowMemory) ", LOW" else ""})$swap; " +
            "app processes ${procs.sumOf { it.second } / 1024} MB rss: $top"
    }

    /** (name, VmRSS in kB) for every process running as this app's user. */
    private fun ourProcesses(): List<Pair<String, Long>> {
        val uid = Process.myUid().toString()
        return File("/proc").listFiles { f -> f.name.all(Char::isDigit) }.orEmpty().mapNotNull { dir ->
            runCatching {
                val status = File(dir, "status").readLines()
                val owner = status.firstOrNull { it.startsWith("Uid:") }?.split(Regex("\\s+"))?.getOrNull(1)
                if (owner != uid) return@runCatching null
                val name = status.firstOrNull { it.startsWith("Name:") }?.substringAfter(':')?.trim().orEmpty()
                val rss = status.firstOrNull { it.startsWith("VmRSS:") }?.filter(Char::isDigit)?.toLongOrNull() ?: 0L
                name to rss
            }.getOrNull()
        }
    }

    private fun meminfo(key: String): Long? = runCatching {
        File("/proc/meminfo").useLines { lines -> lines.firstOrNull { it.startsWith("$key:") }?.filter(Char::isDigit)?.toLong() }
    }.getOrNull()
}
