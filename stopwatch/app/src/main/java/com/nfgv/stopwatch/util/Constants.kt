package com.nfgv.stopwatch.util

object Constants {
    // job intervals in milliseconds
    const val PUBLISH_TIMESTAMPS_TASK_INTERVAL = 1200L
    const val FETCH_TIMESTAMPS_TASK_INTERVAL = 1200L
    const val STOPWATCH_TASK_INTERVAL = 50L

    // preferences repository
    const val PREFERENCES_DATA_STORE_NAME = "preferences"

    // preferences keys
    const val STOPPER_ID_KEY = "stopperId"
    const val SHEETS_ID_KEY = "sheetId"
    const val RUN_START_TIME_KEY = "runStartTime"
    const val RUN_NAME_KEY = "runName"

    // sheet ranges
    const val RUN_DATA_RANGE = "Laufdaten!B1:B2"

    // backup
    const val BACKUP_FILE_NAME_PREFIX = "backup_"
}