package com.org.any.lyricview

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger


class MainActivity : AppCompatActivity() ,AnkoLogger {

    private var myBinder: LyricService.MyBinder?=null

    private val myService:MyServiceConnection by lazy { MyServiceConnection() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var song ="陈慧娴 - 千千阙歌"

        lyric_v.setData(song)

        lyric_v.setPlayerSeek {
            myBinder?.playSeekTo(it)
        }

        var myIntent= Intent(this,LyricService::class.java)

        myIntent.putExtra("song",song)

        // 绑定服务
        bindService(myIntent,myService, Service.BIND_AUTO_CREATE)

        // 开启服务 就会调用
        startService(myIntent)

        cmd_btn.setOnClickListener {
            myBinder?.cmdP()
        }

    }

    fun updateProgress(progress:Int){
        lyric_v.updateProgress(progress)
    }

    fun setDuration(duration:Int){
        lyric_v.setDuration(duration)
    }

    fun updateBtn(isPlay:Boolean){
        cmd_btn.text = if(isPlay) "暂停" else "播放"
    }

    inner  class MyServiceConnection : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            myBinder = service as LyricService.MyBinder

            myBinder?.let {

                it.setChangeListener {
                    updateProgress(it)
                }

                it.setDurationListener {
                    setDuration(it)
                }

                it.setChangBtn {
                    updateBtn(it)
                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        // 断开绑定后，要停止UI进度更新
        unbindService(myService)
        //停止服务
//        stopService()
    }

}
