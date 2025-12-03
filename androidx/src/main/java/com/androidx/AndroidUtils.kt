package com.androidx

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * description:
 */
object AndroidUtils {
    private const val TAG = "AndroidUtils"

    fun getAppSettingsIntent(packageName: String?): Intent {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingsIntent.setData(Uri.fromParts("package", packageName, null))
        return settingsIntent
    }

    @JvmStatic
    fun isAndroid14(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE // 34
    }

    fun isAndroid13(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // 33
    }

    @JvmStatic
    fun isAndroid10(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }


    @JvmStatic
    fun getOSVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    @JvmStatic
    val isExternalStorageLegacy: Boolean
        /**
         * 注意：该API相对耗时，请勿在循环中调用
         *
         *
         * 案例：ImageLoader中存在cosur中使用了该api，由于该api速度慢，5000张图片读取耗时500ms，但是在读图处理循环时调用了该方法，也就是循环5000次，耗时从之前的500ms提升至9000ms
         *
         * @return
         */
        get() = isAndroid10() && Environment.isExternalStorageLegacy()

    @JvmStatic
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        if (permissions.size == 0) {
            LogUtils.w("hasPermissions check 0 permissions.")
            return true
        }
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default")

            // DANGER ZONE!!! Changing this will break the library.
            return true
        }

        // Null context may be passed if we have detected Low API (less than M) so getting
        // to this point with a null context should not be possible.
        requireNotNull(context) { "Can't check permissions for null context" }
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    // 原始的检查权限，不依赖第三方库
    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    // 是否应该显示权限解释对话框，如果返回true则表示之前拒绝过，FALSE 代表没有拒绝过
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        vararg permissions: String
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * 是否需要显示申请权限的原因
     *
     * @param fragment
     * @param permissions
     * @return
     */
    fun shouldShowRequestPermissionRationale(
        fragment: Fragment,
        vararg permissions: String
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        for (permission in permissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun getExpectPermissions(mediaType: ExpectMediaType): Array<String> {
        return if (mediaType == ExpectMediaType.IMAGE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else if (mediaType == ExpectMediaType.VIDEO) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }
    }

    /**
     * 用户是否选择了只显示部分图片/视频
     *
     * @param context
     * @return
     */
    fun shouldShowVisualTips(context: Context, mediaType: ExpectMediaType): Boolean {
        if (isAndroid14()) {
            val mediaPermissions = getExpectPermissions(mediaType)
            val mediaPermissionsGranted = hasPermissions(context, *mediaPermissions)
            val visualPermissionGranted =
                hasPermissions(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            LogUtils.d("shouldShowVisualTips mediaPermissionsGranted: $mediaPermissionsGranted")
            LogUtils.d("shouldShowVisualTips visualPermissionGranted: $visualPermissionGranted")
            return !mediaPermissionsGranted && visualPermissionGranted
        } else {
            return false
        }
    }


    /**
     * 是否已经有基础权限，这会根据版本进行判断，最终会检查不同版本的最基本权限即可
     *
     * @param context
     * @param permissions
     * @return
     */
    fun hasBasicPermissionsForImageAndVideo(
        context: Context,
        permissions: Array<String>
    ): Boolean {
        if (isAndroid14()) {
            val permissionList: MutableList<String> = permissions.toMutableList()
            // 检查是否包含 READ_MEDIA_VISUAL_USER_SELECTED 权限
            if (permissionList.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                val visualGranted =
                    hasPermissions(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                if (visualGranted) {
                    // 移除 READ_MEDIA_VIDEO 和 READ_MEDIA_IMAGES 权限
                    permissionList.remove(Manifest.permission.READ_MEDIA_VIDEO)
                    permissionList.remove(Manifest.permission.READ_MEDIA_IMAGES)
                    val leftPermissions = permissionList.toTypedArray()
                    return if (leftPermissions.size > 0) {
                        hasPermissions(context, *leftPermissions)
                    } else {
                        true
                    }
                } else {
                    // 移除 READ_MEDIA_VISUAL_USER_SELECTED 权限，看看其他权限是否是同意的
                    permissionList.remove(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    val leftPermissions = permissionList.toTypedArray()
                    return hasPermissions(context, *leftPermissions)
                }
            } else {
                return hasPermissions(context, *permissions)
            }
        } else {
            // 低于安卓14的版本
            return hasPermissions(context, *permissions)
        }
    }

    fun hasReadVideoPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasReadImagePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasReadAudioPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // 是否是64位的设备
    fun is64BitDevice(): Boolean {
        return Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    }


    enum class ExpectMediaType {
        IMAGE,
        VIDEO,
        IMAGE_AND_VIDEO
    }
}
