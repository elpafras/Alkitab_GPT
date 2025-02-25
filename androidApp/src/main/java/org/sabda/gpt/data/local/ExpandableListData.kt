package org.sabda.gpt.data.local

import android.content.Context
import org.sabda.gpt.R

object ExpandableListData {
    fun prepareListData(context: Context): Pair<List<String>, Map<String, List<String>>> {
        val headers = listOf(
            R.string.situs2,
            R.string.aplikasi_5,
            R.string.komunitas
        ).map { context.getString(it) }

        val childData = headers.zip(
            listOf(
                listOf(
                    R.string.studi_alkitab,
                    R.string.alkitab_media
                ),
                listOf(
                    R.string.alkitab,
                    R.string.alkipedia,
                    R.string.tafsiran,
                    R.string.kamus_alkitab,
                    R.string.bot,
                    R.string.lain
                ),
                listOf(R.string.ai4cg)
            ).map { childIds ->
                childIds.map { context.getString(it) }
            }
        ).toMap()

        return headers to childData
    }
}