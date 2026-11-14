package tn.skillswap.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import tn.skillswap.app.SkillSwapApp
import tn.skillswap.app.data.Repository

object Extras {
    const val EXCHANGE_ID = "tn.skillswap.EXCHANGE_ID"
    const val EXCHANGE_TITLE = "tn.skillswap.EXCHANGE_TITLE"
    const val SKILL_ID = "tn.skillswap.SKILL_ID"
    const val PAGE = "tn.skillswap.PAGE"
}

val Activity.repo: Repository get() = (application as SkillSwapApp).repository

fun Context.ouvrirLien(url: String) {
    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (_: ActivityNotFoundException) { Toast.makeText(this, "Aucune application pour ouvrir ce lien.", Toast.LENGTH_SHORT).show() }
}

fun Context.partagerTexte(sujet: String, texte: String) {
    val envoi = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, sujet).putExtra(Intent.EXTRA_TEXT, texte)
    startActivity(Intent.createChooser(envoi, "Partager via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

inline fun <reified T : Activity> Context.intentVers(vararg extras: Pair<String, Any>): Intent =
    Intent(this, T::class.java).apply {
        extras.forEach { (cle, valeur) ->
            when (valeur) {
                is Long -> putExtra(cle, valeur)
                is String -> putExtra(cle, valeur)
                is Int -> putExtra(cle, valeur)
            }
        }
    }
