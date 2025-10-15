package com.example.diaryapp.model

enum class SortOption(val displayName: String, val sqlOrderBy: String) {
    DATE_NEWEST("Date (Newest First)", "createdAt DESC"),
    DATE_OLDEST("Date (Oldest First)", "createdAt ASC"),
    NAME_A_Z("Name (A-Z)", "title COLLATE NOCASE ASC"),
    NAME_Z_A("Name (Z-A)", "title COLLATE NOCASE DESC");
    
    companion object {
        fun fromOrdinal(ordinal: Int): SortOption = values().getOrElse(ordinal) { DATE_NEWEST }
    }
}