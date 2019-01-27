package com.org.any.lyricview.util

import com.org.any.lyricview.model.LyricBean
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/***
 * 处理歌词
 */
object LyricHandle {


    fun loaderLyric(inputStream: InputStream): ArrayList<LyricBean> {
        var list = arrayListOf<LyricBean>()

        if (inputStream==null) {
            list.add(LyricBean(0, "加载歌词失败..."))
            return list
        }

        // File 实现
        BufferedReader(InputStreamReader(inputStream)).forEachLine{list.addAll(handleLine(it))}
        //排序
        list.sortBy {
            it.startTime
        }
        return list
    }


//    fun loaderFile(file: File): ArrayList<LyricBean> {
//        var list = arrayListOf<LyricBean>()
//
//        if (!file.exists()) {
//            list.add(LyricBean(0, "加载歌词失败..."))
//            return list
//        }
//
//        file.readLines().forEach { it: String ->
//            // 取出一行解析
//            list.addAll(handleLine(it))
//        }
//
//        //排序
//        list.sortBy {
//            it.startTime
//        }
//        return list
//    }

    /***
     * 处理一行数据
     * [01:23.78]
     * [01:23.78]只怕无法再有这种情怀
     * [01:23.78][02:53.78]只怕无法再有这种情怀
     *
     */
    private fun handleLine(str: String): List<LyricBean> {

        var listBeans = arrayListOf<LyricBean>()

        val listStr = str.split("]")

        // 最后一个是content

        val size = listStr.size

        if (listStr[1].isEmpty()) return listBeans

        //  size =3   遍历  0，1     2 是内容不处理   [0,2)

        for (index in 0 until size - 1) {
            listBeans.add(LyricBean(handleTime(listStr[index]), listStr.last()))
        }

        return listBeans
    }

    /***
     * [ti:不吐不快]
    [ar:张敬轩]
    [al:Urban Emotions]
     *
     * 处理时间转换
     * [01:23.78
     */
    private fun handleTime(str: String): Int {

        var hour = 0
        var minute: Int
        var second: Float

        try {
            var temp = str.substring(1, str.length)

            val list = temp.split(":")


            if (list.size == 3) {
                hour = list[0].toInt() * 60 * 60 * 1000
                minute = list[1].toInt() * 60 * 1000
                second = list[2].toFloat() * 1000
            } else {
                minute = list[0].toInt() * 60 * 1000
                second = list[1].toFloat() * 1000
            }
            return hour + minute + second.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

}