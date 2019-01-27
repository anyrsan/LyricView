package com.org.any.lyricview

import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.org.any.lyricview.iservice.IService
import java.io.File

class LyricService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    private var changListener: ((progress: Int) -> Unit)? = null

    private var durationListener: ((duration: Int) -> Unit)? = null

    private var changBtn: ((isPlay: Boolean) -> Unit)? = null

    companion object {
        private const val MESSAGE_WHAT = 0
    }

    private var myHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MESSAGE_WHAT) {
                notificationUpdateUI()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }


    override fun onCreate() {
        super.onCreate()
        println("onCreate...")
    }


    fun notificationUpdateUI() {
        val progress: Int = mediaPlayer?.currentPosition ?: 0
        // 发送给activity
        changListener?.invoke(progress)
        // 并且通过handle 继续调用
        myHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, 50)
    }


    private fun changePlay() {
        changBtn?.let {
            it(mediaPlayer?.isPlaying ?: false)
        }
    }


    private fun setDuration() {
        //发送给 activity
        val duration: Int = mediaPlayer?.duration ?: 0
        durationListener?.let { it(duration) }
    }

    private fun play(song: String) {
        // 释放旧的
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.let {
//            var file = File("sdcard/lyricData", "$song.mp3")
           var fileN: AssetFileDescriptor = assets.openFd("$song.mp3")
            it.setDataSource(fileN.fileDescriptor,fileN.startOffset,fileN.length)
            it.prepareAsync()
            it.setOnPreparedListener {
                setDuration()
                it.start()
                changePlay()
                notificationUpdateUI()
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //可以通过intent 接收来自activity中的数据
        var song = intent?.getStringExtra("song")
        song?.let {
            play(it)
        }
        println("onStartCommand... $song")
        return START_NOT_STICKY
    }




    inner class MyBinder : Binder(), IService {

        fun setChangeListener(changListener: ((progress: Int) -> Unit)?) {
            this@LyricService.changListener = changListener
        }


        fun setDurationListener(durationListener: ((duration: Int) -> Unit)?) {
            this@LyricService.durationListener = durationListener
        }


        fun setChangBtn(changBtn: ((isPlay: Boolean) -> Unit)?) {
            this@LyricService.changBtn = changBtn
        }


        override fun cmdP() {
            if (isPlaying()) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
            changePlay()
        }


        override fun isPlaying(): Boolean {
            return mediaPlayer?.isPlaying ?: false
        }

        /**
         * 同步进度
         */
        fun playSeekTo(progress: Int) {
            mediaPlayer?.seekTo(progress)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        myHandler.removeMessages(MESSAGE_WHAT)
        println("onDestroy...")
    }
}