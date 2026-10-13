package com.grillpoet.bily.ui.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.grillpoet.bily.R

/**
 * 自定义图标入口。
 * 用法与 Icons.Rounded.X 一致，例如：
 *   imageVector = AppIcons.StarBorder
 */
object AppIcons {

    val StarBorder: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_star_border)

    val FileDownload: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_file_download)

    val FileUpload: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_file_upload)

    val Download: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_download)

    val Fullscreen: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_fullscreen)

    val VideoLibrary: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_video_library)
}