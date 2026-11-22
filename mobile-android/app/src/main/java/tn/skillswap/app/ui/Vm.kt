package tn.skillswap.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Crée (ou retrouve après une rotation d'écran) un ViewModel construit à la main.
 * L'état survit donc aux changements de configuration, contrairement à `remember`.
 */
@Composable
inline fun <reified VM : ViewModel> vmDe(crossinline creer: () -> VM): VM =
    viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = creer() as T
    })
