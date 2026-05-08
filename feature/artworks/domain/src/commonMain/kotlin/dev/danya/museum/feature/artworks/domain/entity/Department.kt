package dev.danya.museum.feature.artworks.domain.entity

enum class Department(val id: Int, val resourceKey: String) {
    AMERICAN_DECORATIVE_ARTS(1, "department_american_decorative_arts"),
    ANCIENT_WEST_ASIAN_ART(3, "department_ancient_west_asian_art"),
    ARMS_AND_ARMOR(4, "department_arms_and_armor"),
    ARTS_OF_AFRICA_OCEANIA_AMERICAS(5, "department_arts_of_africa_oceania_americas"),
    ASIAN_ART(6, "department_asian_art"),
    THE_CLOISTERS(7, "department_the_cloisters"),
    THE_COSTUME_INSTITUTE(8, "department_the_costume_institute"),
    DRAWINGS_AND_PRINTS(9, "department_drawings_and_prints"),
    EGYPTIAN_ART(10, "department_egyptian_art"),
    EUROPEAN_PAINTINGS(11, "department_european_paintings"),
    EUROPEAN_SCULPTURE_AND_DECORATIVE_ARTS(12, "department_european_sculpture_and_decorative_arts"),
    GREEK_AND_ROMAN_ART(13, "department_greek_and_roman_art"),
    ISLAMIC_ART(14, "department_islamic_art"),
    THE_ROBERT_LEHMAN_COLLECTION(15, "department_the_robert_lehman_collection"),
    MEDIEVAL_ART(17, "department_medieval_art"),
    MUSICAL_INSTRUMENTS(18, "department_musical_instruments"),
    PHOTOGRAPHS(19, "department_photographs"),
    MODERN_ART(21, "department_modern_art");

    companion object {
        fun fromId(id: Int): Department? = entries.find { it.id == id }
    }
}