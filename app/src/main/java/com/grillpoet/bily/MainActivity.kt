package com.grillpoet.bily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import com.grillpoet.bily.ui.AppRoot
import com.grillpoet.bily.ui.theme.BilyTheme
import com.grillpoet.bily.ui.vm.AppViewModelFactory
import com.grillpoet.bily.ui.vm.MainViewModel
import com.grillpoet.bily.ui.vm.SearchViewModel

class MainActivity : ComponentActivity() {

    private val container by lazy { (application as BilyApp).container }
    private val vmFactory by lazy { AppViewModelFactory(container) }

    private val mainVm: MainViewModel by viewModels { vmFactory }
    private val searchVm: SearchViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BilyTheme {
                AppRoot(
                    mainVm = mainVm,
                    searchVm = searchVm,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.systemBars.union(WindowInsets.displayCutout)
                    )
                )
            }
        }
    }
}
