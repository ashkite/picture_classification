package com.ashkite.pictureclassification.data.ml

class EventLabelMapper {
    fun mapEvents(labels: List<LabelScore>): List<AutoTag> {
        val candidates = mutableMapOf<String, Float>()
        labels.filter { it.score >= EVENT_THRESHOLD }.forEach { label ->
            val normalized = normalize(label.label)
            EVENT_RULES.forEach { rule ->
                if (rule.keywords.any { normalized.contains(it) }) {
                    val best = candidates[rule.tag] ?: 0f
                    if (label.score > best) {
                        candidates[rule.tag] = label.score
                    }
                }
            }
        }

        val bestEvent = candidates.entries.maxByOrNull { it.value }
        return if (bestEvent != null) {
            listOf(AutoTag(type = TYPE_EVENT, name = bestEvent.key, score = bestEvent.value))
        } else {
            emptyList()
        }
    }

    fun mapPeople(labels: List<LabelScore>): List<AutoTag> {
        val best = labels.firstOrNull {
            it.score >= PEOPLE_THRESHOLD && PEOPLE_KEYWORDS.any { keyword ->
                normalize(it.label).contains(keyword)
            }
        }
        return if (best != null) {
            listOf(AutoTag(type = TYPE_PEOPLE, name = PEOPLE_TAG, score = best.score))
        } else {
            emptyList()
        }
    }

    private fun normalize(value: String): String {
        return value.lowercase().replace(Regex("[^a-z0-9 ]"), " ")
    }

    data class Rule(val tag: String, val keywords: List<String>)

    companion object {
        const val TYPE_EVENT = "event"
        const val TYPE_PEOPLE = "people"
        private const val EVENT_THRESHOLD = 0.85f
        private const val PEOPLE_THRESHOLD = 0.9f
        private const val PEOPLE_TAG = "Person"

        private val PEOPLE_KEYWORDS = listOf(
            "person",
            "man",
            "woman",
            "boy",
            "girl",
            "baby"
        )

        private val EVENT_RULES = listOf(
            Rule("Travel/Tourism", listOf("temple", "palace", "castle", "monument", "museum", "landmark")),
            Rule("Nature/Outdoor", listOf("forest", "waterfall", "lake", "field", "park", "valley")),
            Rule("City/Street", listOf("street", "city", "downtown", "skyscraper", "subway", "metro")),
            Rule("Food/Restaurant", listOf("restaurant", "food", "dish", "dining", "table")),
            Rule("Cafe/Dessert", listOf("cafe", "coffee", "dessert", "pastry")),
            Rule("Party/Celebration", listOf("party", "celebration", "balloon", "birthday")),
            Rule("Wedding", listOf("wedding", "bride", "groom")),
            Rule("Family/Kids", listOf("family", "child", "kids", "baby", "playground")),
            Rule("Meeting/Work", listOf("office", "meeting", "conference", "workstation")),
            Rule("Performance/Stage", listOf("concert", "stage", "theater", "performance")),
            Rule("Sports/Fitness", listOf("stadium", "gym", "sport", "basketball", "soccer")),
            Rule("Animals/Pets", listOf("dog", "cat", "pet", "animal")),
            Rule("Beach/Sea", listOf("beach", "seashore", "ocean", "coast")),
            Rule("Mountain/Hiking", listOf("mountain", "hiking", "trail", "hill")),
            Rule("Night/Evening", listOf("night", "nightscape", "city lights"))
        )
    }
}

data class AutoTag(
    val type: String,
    val name: String,
    val score: Float
)
