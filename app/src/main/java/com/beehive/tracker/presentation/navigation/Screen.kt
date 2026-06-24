package com.beehive.tracker.presentation.navigation

// Uygulama rotalarının tek kaynağı.
// String route'lar sabit; navigation argümanları {placeholder} formatında.
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")

    data class Map(val apiaryId: String = "{apiaryId}") : Screen("map/{apiaryId}") {
        fun buildRoute(id: String) = "map/$id"
    }

    data class HiveDetail(val hiveId: String = "{hiveId}") : Screen("hive/{hiveId}") {
        fun buildRoute(id: String) = "hive/$id"
    }

    // NoteEditor: hangi kovana not ekleneceğini hiveId argümanıyla alır
    data class NoteEditor(val hiveId: String = "{hiveId}") : Screen("note_editor/{hiveId}") {
        fun buildRoute(id: String) = "note_editor/$id"
    }
}
