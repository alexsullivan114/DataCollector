package com.alexsullivan.datacollor.database.entities

enum class Rating {
    TERRIBLE,
    POOR,
    MEDIOCRE,
    GOOD,
    GREAT;

    fun increment(): Rating {
        return when (this) {
            TERRIBLE -> POOR
            POOR -> MEDIOCRE
            MEDIOCRE -> GOOD
            GOOD -> GREAT
            GREAT -> GREAT
        }
    }

    fun decrement(): Rating {
        return when (this) {
            TERRIBLE -> TERRIBLE
            POOR -> TERRIBLE
            MEDIOCRE -> POOR
            GOOD -> MEDIOCRE
            GREAT -> GOOD
        }
    }
}
