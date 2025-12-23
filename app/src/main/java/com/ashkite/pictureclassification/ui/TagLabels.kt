package com.ashkite.pictureclassification.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ashkite.pictureclassification.R

@Composable
fun displayTagName(tagType: String, tagName: String): String {
    return when (tagType) {
        "people" -> when (tagName) {
            "Person" -> stringResource(R.string.tag_people_auto)
            else -> tagName
        }
        "event" -> when (tagName) {
            "Travel/Tourism" -> stringResource(R.string.tag_event_travel)
            "Nature/Outdoor" -> stringResource(R.string.tag_event_nature)
            "City/Street" -> stringResource(R.string.tag_event_city)
            "Food/Restaurant" -> stringResource(R.string.tag_event_food)
            "Cafe/Dessert" -> stringResource(R.string.tag_event_cafe)
            "Party/Celebration" -> stringResource(R.string.tag_event_party)
            "Wedding" -> stringResource(R.string.tag_event_wedding)
            "Family/Kids" -> stringResource(R.string.tag_event_family)
            "Meeting/Work" -> stringResource(R.string.tag_event_work)
            "Performance/Stage" -> stringResource(R.string.tag_event_performance)
            "Sports/Fitness" -> stringResource(R.string.tag_event_sports)
            "Animals/Pets" -> stringResource(R.string.tag_event_animals)
            "Beach/Sea" -> stringResource(R.string.tag_event_beach)
            "Mountain/Hiking" -> stringResource(R.string.tag_event_mountain)
            "Night/Evening" -> stringResource(R.string.tag_event_night)
            else -> tagName
        }
        else -> tagName
    }
}
