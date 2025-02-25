package com.drdisagree.colorblendr.utils

import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.drdisagree.colorblendr.utils.AppUtil.readJsonFileFromAssets
import org.json.JSONArray

fun parseContributors(): ArrayList<AboutAppModel> {
    val excludedContributors = ArrayList<String>().apply {
        add("Mahmud0808")
        add("crowdin-bot")
        add("dependabot")
    }

    val contributorsList = ArrayList<AboutAppModel>()
    val jsonStr = readJsonFileFromAssets("contributors.json")
    val jsonArray = try {
        JSONArray(jsonStr)
    } catch (e: Exception) {
        JSONArray()
    }

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("login")

        if (excludedContributors.contains(name)) continue // Skip the excluded contributors

        val picture = jsonObject.getString("avatar_url")
        val commitsUrl = "https://github.com/Mahmud0808/ColorBlendr/commits?author=$name"
        val contributions = jsonObject.getInt("contributions")

        contributorsList.add(
            AboutAppModel(
                name,
                appContext.resources.getString(R.string.total_contributions, contributions),
                commitsUrl,
                picture
            )
        )
    }

    return contributorsList
}

fun parseTranslators(): ArrayList<AboutAppModel> {
    val excludedContributors = ArrayList<String>().apply {
        add("DrDisagree")
    }

    val contributorsList = ArrayList<AboutAppModel>()
    val jsonStr = readJsonFileFromAssets("translators.json")
    val jsonArray = try {
        JSONArray(jsonStr)
    } catch (e: Exception) {
        JSONArray()
    }

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("name").replace(Regex("\\s*\\(.*\\)"), "")
        val username = jsonObject.getString("username")

        if (excludedContributors.contains(username)) continue // Skip the excluded contributors

        val picture = jsonObject.getString("picture")
        val languagesArray = jsonObject.getJSONArray("languages")
        val languagesList = ArrayList<String>()
        for (j in 0 until languagesArray.length()) {
            languagesList.add(languagesArray.getJSONObject(j).getString("name"))
        }
        val languages = languagesList.joinToString(", ")
        val url = "https://crowdin.com/profile/$username"

        contributorsList.add(AboutAppModel(name, languages, url, picture))
    }

    return contributorsList
}