package com.example.mytest2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MusicService(): Service() {

    public val musicList = ArrayList<Music>()
    lateinit var musicNames: Array<String>
    lateinit var musicAuthors:Array<String>
    lateinit var musicImages:TypedArray
    lateinit  var  mediaPlayer : MediaPlayer
    private var timer = Timer();
    val musicController = MusicController()
    var index = 0
    var hasPrepared = false

    override fun onBind(intent: Intent): IBinder {
        return musicController
    }

    override fun onCreate() {
        mediaPlayer = MediaPlayer()
        initMusics()
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mediaPlayer == null) {
            return
        }
        if(mediaPlayer.isLooping) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    //添加计时器，设置进度条
    fun addTimer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                if(mediaPlayer == null||hasPrepared == false) return
                var duration = mediaPlayer.duration
                var currentDuration = mediaPlayer.currentPosition //播放进度
                var msg =  MainActivity.handler.obtainMessage()
                var bundle = Bundle()
                bundle.putInt("duration",duration)
                bundle.putInt("currentDuration",currentDuration)
                msg.data = bundle
                MainActivity.handler.sendMessage(msg)
            }
        }
        //开启计时任务后5ms，执行第一次任务，然后500ms执行一次
        timer.schedule(timerTask,10,500)
    }


    inner class MusicController() : Binder() {
        fun startPlay(position: Int) {
            index=position
            val assetManager = assets
            Log.d("MusicServicetest",position.toString())
            Log.d("MusicServicetest",musicList[position].name)
            val fd = assetManager.openFd(musicList[position].name+".mp3")
            mediaPlayer.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
            mediaPlayer.prepare()
            mediaPlayer.start()
            addTimer()
        }
        fun  play(position :Int) {
            if(mediaPlayer != null) {
                hasPrepared = false
                mediaPlayer.stop()
                mediaPlayer.reset()
                val assetManager = assets
                Log.d("MusicServicetest",position.toString())
                Log.d("MusicServicetest",musicList[position].name)
                val fd = assetManager.openFd(musicList[position].name+".mp3")
                mediaPlayer.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
                mediaPlayer.prepare()
                hasPrepared = true
                mediaPlayer.start()
                addTimer()
            }
        }

        fun pausePlay() {
            mediaPlayer.pause();
        }

        fun continuePlay() {
            mediaPlayer.start()
        }

        fun seekTo(progress :Int) {
            mediaPlayer.seekTo(progress)
        }

    }
    private fun initMusics() {
        musicNames = resources.getStringArray(R.array.musicNames)
        musicAuthors = resources.getStringArray(R.array.authors)
        musicImages = resources.obtainTypedArray(R.array.musicImages)
        for(i in 0 until musicNames.size) {
            musicList.add(Music(musicNames[i],musicAuthors[i],musicImages.getResourceId(i,0)))
        }

    }


}

