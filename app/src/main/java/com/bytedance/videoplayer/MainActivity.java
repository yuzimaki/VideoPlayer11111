package com.bytedance.videoplayer;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    private VideoPlayerijk ijkplayer;
    private SeekBar seekBar;
    private long videoLength;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            long time = data.getLong("progress");
            //Log.d("fordebug", "handleMessage: " + time);
            seekBar.setProgress((int) time);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ijkplayer = findViewById(R.id.ijkPlayer);

        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }

        ijkplayer.setListener(new VideoPlayerListener(){
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                super.onPrepared(iMediaPlayer);
                iMediaPlayer.pause();
                videoLength = iMediaPlayer.getDuration();
                seekBar.setMax((int)videoLength);
            }
        });
        ijkplayer.setVideoResource(R.raw.bytedance);

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkplayer.start();
            }
        });


        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ijkplayer.pause();
            }
        });

        findViewById(R.id.buttonFull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        seekBar = findViewById(R.id.seekbar);
        seekBar.setProgress(0);
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    ijkplayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    Bundle data = new Bundle();
                    long time = ijkplayer.getCurrentPosition();
                    data.putLong("progress", time);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(ijkplayer.isPlaying())
            ijkplayer.stop();
        IjkMediaPlayer.native_profileEnd();
    }
}