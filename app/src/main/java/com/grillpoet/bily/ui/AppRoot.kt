package com.grillpoet.bily.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.grillpoet.bily.BilyApp
import com.grillpoet.bily.nav.Navigator
import com.grillpoet.bily.nav.Screen
import com.grillpoet.bily.ui.screen.MainScreen
import com.grillpoet.bily.ui.screen.SearchScreen
import com.grillpoet.bily.ui.screen.SpaceScreen
import com.grillpoet.bily.ui.screen.VideoScreen
import com.grillpoet.bily.ui.vm.AppViewModelFactory
import com.grillpoet.bily.ui.vm.MainViewModel
import com.grillpoet.bily.ui.vm.SearchViewModel
import com.grillpoet.bily.ui.vm.SpaceViewModel
import com.grillpoet.bily.ui.vm.VideoViewModel

@OptIn(UnstableApi::class)
@Composable
fun AppRoot(
    mainVm: MainViewModel,
    searchVm: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val nav = remember { Navigator() }
    val context = LocalContext.current
    val container = remember { (context.applicationContext as BilyApp).container }
    val vmFactory = remember(container) { AppViewModelFactory(container) }

    // Android 13+ 通知权限
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = true) {
        if (!nav.pop()) {
            (context as? Activity)?.finishAndRemoveTask()
        }
    }

    when (val current = nav.current) {
        is Screen.Main -> MainScreen(
            modifier = modifier,
            viewModel = mainVm,
            onSearchClick = { nav.push(Screen.Search) },
            onVideoClick = { nav.push(Screen.Video(it)) }
        )

        is Screen.Search -> SearchScreen(
            modifier = modifier,
            viewModel = searchVm,
            onBack = { nav.pop() },
            onVideoClick = { nav.push(Screen.Video(it)) }
        )

        is Screen.Video -> key(current) {
            val vm: VideoViewModel = viewModel(factory = vmFactory)
            VideoScreen(
                modifier = modifier,
                bvid = current.bvid,
                viewModel = vm,
                onSpaceClick = { mid, name -> nav.push(Screen.Space(mid, name)) },
                onVideoClick = { nav.push(Screen.Video(it)) }
            )
        }

        is Screen.Space -> key(current) {
            val vm: SpaceViewModel = viewModel(factory = vmFactory)
            SpaceScreen(
                modifier = modifier,
                mid = current.mid,
                name = current.name,
                viewModel = vm,
                onBack = { nav.pop() },
                onVideoClick = { nav.push(Screen.Video(it)) }
            )
        }
    }
}
