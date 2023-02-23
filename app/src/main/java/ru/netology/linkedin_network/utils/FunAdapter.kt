package ru.netology.linkedin_network.utils

import android.widget.TextView
import ru.netology.linkedin_network.dto.UserPreview
import ru.netology.linkedin_network.util.numbersToString

fun toText(speakerslist:  List<UserPreview>, listSpeakers: TextView) {
            if (speakerslist.size >= 2) {
                val userList =
                    "${speakerslist.first().name}\n and ${numbersToString(speakerslist.size - 1)}"
                listSpeakers.text = userList
            } else if (speakerslist.size == 1) {
                listSpeakers.text = speakerslist.first().name
            }
        }
