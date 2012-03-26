package com.dracode.autotraffic.common.vedio;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @ClassName: PlayVedioActivity
 * @Description: 播放视频
 * @author Figo.Gu
 * @date 2011-03-07 上午9:45
 */
public class PlayVedioActivity extends Activity {
	
	private static final String TAG = PlayVedioActivity.class.getSimpleName();// 调试标签
	
	// 控件
	private VideoView mVideoView;
	private Uri mUri;
	private int mPositionWhenPaused = -1;
	private MediaController mMediaController;
	/** 媒体.*/
	private AudioManager audio;
	/** 加载进度条 .*/
	private ProgressDialog progDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_play_vedio);
		audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
		// 设置成全屏模式
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 强制为横屏
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 获取参数
		Bundle bundle = super.getIntent().getExtras();
		String vedioUrl = bundle.getString("vedioUrl");
		
		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("正在加载，请稍等...");
		progDialog.show();
		
		mVideoView = (VideoView) findViewById(R.id.videoView);
		// 视频文件
		mUri = Uri.parse(vedioUrl);
		// 创建媒体控制器
        mMediaController = new MediaController(this);
        mMediaController.show(0);
        mVideoView.setMediaController(mMediaController);
        // 开始播放
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				progDialog.dismiss();
			}
		});
        // 监听MediaPlayer上报的错误信息
        mVideoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				progDialog.dismiss();
				UserAppSession.showToastLong(PlayVedioActivity.this, "无法播放或播放出错！");
				return false;
			}
		});
        // Video播完的时候得到通知
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
	}
	// 开始
	public void onStart() {
    	// Play Video
    	mVideoView.setVideoURI(mUri);
    	mVideoView.start();

    	super.onStart();
    }
	// 暂停  
    public void onPause() {
    	mPositionWhenPaused = mVideoView.getCurrentPosition();
    	mVideoView.stopPlayback();
    	Log.d(TAG, "OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
    	Log.d(TAG, "OnStop: getDuration  = " + mVideoView.getDuration());

    	super.onPause();
    }

    public void onResume() {
    	if(mPositionWhenPaused >= 0) {
    		mVideoView.seekTo(mPositionWhenPaused);
    		mPositionWhenPaused = -1;
    	}

    	super.onResume();
    }

    /**
	 * 媒体音量的调节.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND	| AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND	| AudioManager.FLAG_SHOW_UI);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}