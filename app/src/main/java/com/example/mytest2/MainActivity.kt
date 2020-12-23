package com.example.mytest2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.res.TypedArray
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    public val musicList = ArrayList<Music>()
    lateinit var musicNames: Array<String>
    lateinit var musicAuthors:Array<String>
    lateinit var musicImages:TypedArray
    lateinit var musicController : MusicService.MusicController
    var nowMusic = 0
    var isBofang = 0
    var currentState = "IDLE"
    val TAG = "MainActivity"

    private var nm: NotificationManager? = null
    private var contentViews: RemoteViews? = null
    private var notify: Notification? = null
    private val NOTIFICATION_ID = 123

    //自定义LrcView，用来展示歌词
    var mLrcView: LrcView? = null

    //更新歌词的频率，每秒更新一次
    private val mPalyTimerDuration = 1000

    //更新歌词的定时器
    private var mTimer: Timer? = null

    //更新歌词的定时任务
    private var mTask: TimerTask? = null

    lateinit var receiver: PlayMusicReceiver

    val notificationBuilder = NotificationCompat.Builder(this, "player")
            .setShowWhen(false)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)


    companion object{
        var duration = 0
        var currentDuration = 0
        //消息处理线程对象
        public val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //super.handleMessage(msg)
                var bundle = msg.data
                duration = bundle.getInt("duration")
                currentDuration = bundle.getInt("currentDuration")
            }
        }

    }

//pendingInent
    //remoteview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMusics()
        handlerSeekBar.post(task)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val adapter = MusicListAdapter(musicList, this)
        recyclerView.adapter = adapter

        Log.d(this::class.simpleName, "before init")
        initNotification()
        Log.d(this::class.simpleName, "after init")

        mLrcView=findViewById(R.id.lrcView);

        btn_show_list.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        musicName.text=musicNames[nowMusic]
        musicionName.text=musicAuthors[nowMusic]
        musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
        val intent = Intent(this,MusicService::class.java)


        val connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                musicController = service as MusicService.MusicController

            }
        }
        bindService(intent,connection, Context.BIND_AUTO_CREATE)
        
        //进度条
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            //滑动条变化的处理
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("progress = $progress")
                println("fromUser = $fromUser")
            }
            //开始滑动时处理
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("com.example.screenBrightnessTest.MyActivity.onStartTrackingTouch")
            }
            //停止滑动时处理
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                println("com.example.screenBrightnessTest.MyActivity.onStopTrackingTouch")
                var progress = seekBar.progress
                musicController.seekTo(progress)

            }
        })


        //其他按键
        btn_prev.setOnClickListener {
            if(nowMusic > 0) {
                nowMusic--
                musicName.text=musicNames[nowMusic]
                musicionName.text=musicAuthors[nowMusic]
                musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
                btn_play.setImageResource(R.drawable.ic_pause)
                isBofang = 1
                musicController.play(nowMusic)
                currentState = "IDLE"
                val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
                //解析歌词构造器
                //解析歌词构造器
                val builder: ILrcBuilder = DefaultLrcBuilder()
                //解析歌词返回LrcRow集合
                //解析歌词返回LrcRow集合
                var rows:List<LrcRow> = builder.getLrcRows(lrc)
                //Log.d("fuck",rows.toString())
                //将得到的歌词集合传给mLrcView用来展示
                mLrcView!!.setLrc(rows)
                contentViews?.setTextViewText(R.id.currentmusic,musicNames[nowMusic])
                contentViews?.setImageViewResource(R.id.playtag,musicImages.getResourceId(nowMusic,0))
                contentViews?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
                nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                btn_play.setImageResource(R.drawable.ic_pause)
            }
        }

        btn_next.setOnClickListener {
            if(nowMusic < musicNames.size - 1) {
                nowMusic++
                musicName.text=musicNames[nowMusic]
                musicionName.text=musicAuthors[nowMusic]
                musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
                btn_play.setImageResource(R.drawable.ic_pause)
                isBofang = 1
                musicController.play(nowMusic)
                currentState = "IDLE"

                Log.d("lrc_names",musicNames[nowMusic]+".lrc")
                val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
                //解析歌词构造器
                //解析歌词构造器
                val builder: ILrcBuilder = DefaultLrcBuilder()
                //解析歌词返回LrcRow集合
                //解析歌词返回LrcRow集合
                var rows:List<LrcRow> = builder.getLrcRows(lrc)
                //Log.d("fuck",rows.toString())
                //将得到的歌词集合传给mLrcView用来展示
                mLrcView!!.setLrc(rows)
                contentViews?.setTextViewText(R.id.currentmusic,musicNames[nowMusic])
                contentViews?.setImageViewResource(R.id.playtag,musicImages.getResourceId(nowMusic,0))
                contentViews?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
                nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                btn_play.setImageResource(R.drawable.ic_pause)
            }

        }

        btn_play.setOnClickListener {
            if(isBofang == 0) {
                btn_play.setImageResource(R.drawable.ic_pause)
                isBofang=1
                if(currentState.equals("IDLE")) {
                    musicController.play(nowMusic)
                } else {
                    musicController.continuePlay();
                }
                contentViews?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
                nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())

            }else {
                btn_play.setImageResource(R.drawable.ic_play_arrow_black)
                isBofang = 0
                musicController.pausePlay();
                currentState = "PAUSE"
                contentViews?.setImageViewResource(R.id.playandpause,R.drawable.ic_play_arrow_black)
                nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
            val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
            //解析歌词构造器
            //解析歌词构造器
            val builder: ILrcBuilder = DefaultLrcBuilder()
            //解析歌词返回LrcRow集合
            //解析歌词返回LrcRow集合
            var rows:List<LrcRow> = builder.getLrcRows(lrc)
            Log.d("fuck",rows.toString())
            //将得到的歌词集合传给mLrcView用来展示
            mLrcView!!.setLrc(rows)

        }

    }

open fun getFromAssets(fileName: String?): String {
    try {
        val inputReader = InputStreamReader(resources.assets.open(fileName!!))
        val bufReader = BufferedReader(inputReader)
        var line:String?=""

//        var line = ""
        var result = ""
//        while(bufReader.readLine() != null){
//            line = bufReader.readLine()
//            Log.d("trimlyc",line)
//            if(line.trim().equals(""))
//                continue;
//            result += line + "\r\n";
//        }

//        do{
//            line = bufReader.readLine();
//            if(line.trim().equals(""))
//                continue;
//            Log.d("trimlyc",line)
//            result += line + "\r\n";
//        }while(bufReader.readLine()!=null)

        while(true) {
            line = bufReader.readLine()
            if(line == null){
                break
            }
            if(line.trim().equals(""))
                continue;
            Log.d("trimlyc",line)
            result += line + "\r\n";

        }

//        while((line = bufReader.readLine()) != null){
//            if(line.trim().equals(""))
//                continue;
//            result += line + "\r\n";
//        }

        return result
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}


    private fun initMusics() {
        musicNames = resources.getStringArray(R.array.musicNames)
        musicAuthors = resources.getStringArray(R.array.authors)
        musicImages = resources.obtainTypedArray(R.array.musicImages)
        for(i in 0 until musicNames.size) {
            musicList.add(Music(musicNames[i],musicAuthors[i],musicImages.getResourceId(i,0)))
        }

    }

    //通过列表选择改变音乐

    fun changeMusic( position :Int) {
        //Log.d("testtest",position.toString())
        nowMusic = position
        musicName.text=musicNames[nowMusic]
        //Log.d("testtest",musicNames[nowMusic])
        musicionName.text=musicAuthors[nowMusic]
        musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
        if(isBofang == 1) {
            musicController.play(position)
        }
        currentState = "IDLE"
        btn_play.setImageResource(R.drawable.ic_pause)
        contentViews?.setTextViewText(R.id.currentmusic,musicNames[nowMusic])
        contentViews?.setImageViewResource(R.id.playtag,musicImages.getResourceId(nowMusic,0))
        contentViews?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
        nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
        val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
        //解析歌词构造器
        //解析歌词构造器
        val builder: ILrcBuilder = DefaultLrcBuilder()
        //解析歌词返回LrcRow集合
        //解析歌词返回LrcRow集合
        var rows:List<LrcRow> = builder.getLrcRows(lrc)
        //Log.d("fuck",rows.toString())
        //将得到的歌词集合传给mLrcView用来展示
        mLrcView!!.setLrc(rows)
    }


    private val handlerSeekBar = Handler()
    private val task: Runnable = object : Runnable {
        override fun run() {
            handlerSeekBar.postDelayed(this, 500) //设置延迟时间
            seekBar.max = duration
            seekBar.progress = currentDuration
            mLrcView?.seekLrcToTime(currentDuration.toLong())

            //总时长
            var minute = duration /1000 / 60
            var second = duration /1000 % 60
            var strMinte = ""
            var strSecond = ""
            if(minute < 10) {
                strMinte = "0"+ minute
            }else {
                strMinte = minute.toString()
            }

            if(second < 10) {
                strSecond = "0" + second
            }else {
                strSecond = second.toString()
            }
            allTime.setText(strMinte + ":" + strSecond)

            //已播放时长
            minute = currentDuration /1000 / 60
            second = currentDuration /1000 % 60
            if(minute < 10) {
                strMinte = "0"+ minute
            }else {
                strMinte = minute.toString()
            }

            if(second < 10) {
                strSecond = "0" + second
            }else {
                strSecond = second.toString()
            }
            nowTime.setText(strMinte + ":" + strSecond)


        }
    }

    private fun initNotification() {

        //NotificationManager的获取
        nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("player", "播放器",
                NotificationManager.IMPORTANCE_DEFAULT)
        nm!!.createNotificationChannel(channel)

        val mainIntent = Intent(this@MainActivity, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this@MainActivity, 0, mainIntent, 0)

        val tmp = RemoteViews(packageName, R.layout.notification)
        contentViews = RemoteViews(packageName, R.layout.notification)
//        notify!!.contentView = contentViews
        tmp.setOnClickPendingIntent(R.id.playtag, pi)
        tmp.setOnClickPendingIntent(R.id.currentmusic, pi)
        //上一首图标添加点击监听
        val previousButtonIntent = Intent("ACTION_PRE_SONG")
        val pendPreviousButtonIntent = PendingIntent.getBroadcast(this, 0, previousButtonIntent, 0)
        tmp.setOnClickPendingIntent(R.id.pre, pendPreviousButtonIntent)
        //播放/暂停添加点击监听
        val playPauseButtonIntent = Intent("ACTION_PLAY_AND_PAUSE")
        val playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseButtonIntent, 0)
        tmp.setOnClickPendingIntent(R.id.playandpause, playPausePendingIntent)
        //下一首图标添加监听
        val nextButtonIntent = Intent("ACTION_NEXT_SONG")
        val pendNextButtonIntent = PendingIntent.getBroadcast(this, 0, nextButtonIntent, 0)
        tmp.setOnClickPendingIntent(R.id.next, pendNextButtonIntent)

        contentViews = tmp
        notificationBuilder.setContent(contentViews)

        val intentFilter =  IntentFilter().apply {
            addAction("ACTION_NEXT_SONG")
            addAction("ACTION_PRE_SONG")
            addAction("ACTION_EXIT")
            addAction("ACTION_PLAY_AND_PAUSE")
        }
        receiver = PlayMusicReceiver()
        registerReceiver(receiver, intentFilter)


        nm!!.notify(NOTIFICATION_ID, notificationBuilder.build()) //调用notify方法后即可显示通知

    }

    inner class PlayMusicReceiver :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d("broadcastreceiver",action.toString())
            Log.d(TAG, "action = $action")
            if (action.equals("ACTION_NEXT_SONG")) {
                if(nowMusic < musicNames.size - 1) {
                    nowMusic++
                    musicName.text=musicNames[nowMusic]
                    musicionName.text=musicAuthors[nowMusic]
                    musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
                    btn_play.setImageResource(R.drawable.ic_pause)
                    val t = contentViews
                    Log.d("testconvert",musicNames[nowMusic])
                    t?.setTextViewText(R.id.currentmusic,musicNames[nowMusic])
                    t?.setImageViewResource(R.id.playtag,musicImages.getResourceId(nowMusic,0))
                    t?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
                    isBofang = 1
                    musicController.play(nowMusic)
                    currentState = "IDLE"
                    nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                    val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
                    //解析歌词构造器
                    //解析歌词构造器
                    val builder: ILrcBuilder = DefaultLrcBuilder()
                    //解析歌词返回LrcRow集合
                    //解析歌词返回LrcRow集合
                    var rows:List<LrcRow> = builder.getLrcRows(lrc)
                    //Log.d("fuck",rows.toString())
                    //将得到的歌词集合传给mLrcView用来展示
                    mLrcView!!.setLrc(rows)

                }
            } else if (action.equals("ACTION_PRE_SONG") ) {
                if(nowMusic > 0) {
                    nowMusic--
                    musicName.text=musicNames[nowMusic]
                    musicionName.text=musicAuthors[nowMusic]
                    musicImage.setImageResource(musicImages.getResourceId(nowMusic,0))
                    btn_play.setImageResource(R.drawable.ic_pause)
                    val t = contentViews
                    t?.setTextViewText(R.id.currentmusic,musicNames[nowMusic])
                    t?.setImageViewResource(R.id.playtag,musicImages.getResourceId(nowMusic,0))
                    t?.setImageViewResource(R.id.playandpause,R.drawable.ic_pause)
                    isBofang = 1
                    musicController.play(nowMusic)
                    currentState = "IDLE"
                    nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                    val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
                    //解析歌词构造器
                    //解析歌词构造器
                    val builder: ILrcBuilder = DefaultLrcBuilder()
                    //解析歌词返回LrcRow集合
                    //解析歌词返回LrcRow集合
                    var rows:List<LrcRow> = builder.getLrcRows(lrc)
                    //Log.d("fuck",rows.toString())
                    //将得到的歌词集合传给mLrcView用来展示
                    mLrcView!!.setLrc(rows)
                }
            } else if (action.equals("ACTION_PLAY_AND_PAUSE")) {
                if(isBofang == 0) {
                    btn_play.setImageResource(R.drawable.ic_pause)
                    val t = contentViews
                    t?.setImageViewResource(R.id.playandpause, R.drawable.ic_pause)
                    isBofang=1
                    if(currentState.equals("IDLE")) {
                        musicController.play(nowMusic)
                    } else {
                        musicController.continuePlay();
                    }
                    nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                    val lrc: String = getFromAssets(musicNames[nowMusic]+".lrc")
                    //解析歌词构造器
                    //解析歌词构造器
                    val builder: ILrcBuilder = DefaultLrcBuilder()
                    //解析歌词返回LrcRow集合
                    //解析歌词返回LrcRow集合
                    var rows:List<LrcRow> = builder.getLrcRows(lrc)
                    //Log.d("fuck",rows.toString())
                    //将得到的歌词集合传给mLrcView用来展示
                    mLrcView!!.setLrc(rows)

                }else {
                    btn_play.setImageResource(R.drawable.ic_play_arrow_black)
                    val t = contentViews
                    t?.setImageViewResource(R.id.playandpause, R.drawable.ic_play_arrow_black)
                    isBofang = 0
                    musicController.pausePlay();
                    currentState = "PAUSE"
                    nm!!.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            }
        }
    }






}
