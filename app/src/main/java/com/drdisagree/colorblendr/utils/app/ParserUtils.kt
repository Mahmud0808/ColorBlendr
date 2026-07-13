package com.drdisagree.colorblendr.utils.app

import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.drdisagree.colorblendr.utils.app.AppUtil.readJsonFileFromAssets
import org.json.JSONArray
import java.util.Locale

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

    val translators = ArrayList<Pair<AboutAppModel, Int>>()
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
        val translated = jsonObject.optInt("translated")
        val languagesArray = jsonObject.getJSONArray("languages")
        val languagesList = ArrayList<String>()
        for (j in 0 until languagesArray.length()) {
            languagesList.add(languagesArray.getJSONObject(j).getString("name"))
        }
        val words = appContext.resources.getQuantityString(
            R.plurals.total_words_translated,
            translated,
            String.format(Locale.getDefault(), "%,d", translated)
        )
        val languages = "${languagesList.joinToString(", ")} ($words)"
        val url = "https://crowdin.com/profile/$username"

        translators.add(AboutAppModel(name, languages, url, picture) to translated)
    }

    translators.sortByDescending { it.second }
    return ArrayList(translators.map { it.first })
}