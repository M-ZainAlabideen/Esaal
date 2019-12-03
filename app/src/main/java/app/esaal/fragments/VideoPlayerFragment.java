package app.esaal.fragments;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import java.io.IOException;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;


public class VideoPlayerFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener
        , MediaController.MediaPlayerControl {
    public static FragmentActivity activity;
    public static VideoPlayerFragment fragment;
    private SessionManager sessionManager;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private MediaController mediaController;
    Handler handler;

    @BindView(R.id.fragment_video_player_sv_video)
    SurfaceView video;
    @BindView(R.id.fragment_video_player_iv_play)
    ImageView play;

    public static VideoPlayerFragment newInstance(FragmentActivity activity, String videoUrl) {
        fragment = new VideoPlayerFragment();
        VideoPlayerFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString("videoUrl", videoUrl);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_video_player, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(false, false, false, false, "", getString(R.string.questionsAndReplies));
        sessionManager = new SessionManager(activity);
        mediaPlayer = new MediaPlayer();
        // Set the media controller buttons
        handler = new Handler();

        vidHolder = video.getHolder();
        vidHolder.addCallback(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play.setVisibility(View.VISIBLE);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setVisibility(View.INVISIBLE);
                mediaPlayer.start();
            }
        });

        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mediaController != null) {
                    mediaController.show();
                }

                return false;
            }
        });

    }


    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {

        mediaController = new MediaController(activity);
        mediaPlayer.setDisplay(vidHolder);
        try {
            mediaPlayer.setDataSource(getArguments().getString("videoUrl"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(video);
        handler.post(new Runnable() {

            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    @Override
    public void start() {
        mediaPlayer.start();
        play.setVisibility(View.INVISIBLE);
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }
}

