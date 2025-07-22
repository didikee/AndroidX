package com.androidx.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri


/**
 * user author: didikee
 * create time: 2025/5/9 下午6:21
 * description:
 */
object TencentUtils {
    /****************
     *
     * 发起添加群流程。群号：GIF制作星(1044073194) 的 key 为： VdGC6hGVhELoVXwb1KzqzH6AETRNZzPV
     * 调用 joinQQGroup(VdGC6hGVhELoVXwb1KzqzH6AETRNZzPV) 即可发起手Q客户端申请加群 GIF制作星(1044073194)
     *
     * @param key 由官网生成的key,地址：https://qun.qq.com/#/handy-tool/join-group
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(activity: Activity, key: String): Boolean {
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(Intent().apply {
                setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key"))
            })
            return true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            return false
        }
    }

}