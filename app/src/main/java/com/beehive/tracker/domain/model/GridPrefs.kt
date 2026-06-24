package com.beehive.tracker.domain.model

data class GridPrefs(val cols: Int, val rows: Int, val gap: Int) {
    companion object {
        val DEFAULT = GridPrefs(cols = 4, rows = 6, gap = 8)
    }
}
