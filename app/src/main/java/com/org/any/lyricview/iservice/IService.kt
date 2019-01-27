package com.org.any.lyricview.iservice

interface IService {

    /***
     * 暂停/播放
     */
    fun cmdP()

    fun isPlaying():Boolean
}