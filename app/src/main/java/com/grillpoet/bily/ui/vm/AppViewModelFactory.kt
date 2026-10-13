package com.grillpoet.bily.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grillpoet.bily.di.AppContainer

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) ->
            MainViewModel(container.favoriteRepo, container.biliRepo) as T

        modelClass.isAssignableFrom(SearchViewModel::class.java) ->
            SearchViewModel(container.biliRepo) as T

        modelClass.isAssignableFrom(SpaceViewModel::class.java) ->
            SpaceViewModel(container.biliRepo) as T

        modelClass.isAssignableFrom(VideoViewModel::class.java) ->
            VideoViewModel(
                container.biliRepo,
                container.favoriteRepo,
                container.downloader
            ) as T

        else -> error("Unknown ViewModel: ${modelClass.name}")
    }
}
