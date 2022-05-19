package com.alexsullivan.datacollor.database.entities

enum class Rating(val value: Int) {
    TERRIBLE(1),
    POOR(2),
    MEDIOCRE(3),
    GOOD(4),
    GREAT(5);

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

    companion object {
        fun fromValue(value: Int): Rating {
            return values().first { it.value == value }
        }
    }
}
