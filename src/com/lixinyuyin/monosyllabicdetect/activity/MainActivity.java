package com.lixinyuyin.monosyllabicdetect.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.lixinyuyin.monosyllabicdetect.R;
import com.lixinyuyin.monosyllabicdetect.view.GridGraph;

public class MainActivity extends Activity implements OnClickListener {

    public static final int SAMPLE_RATE_HZ = 44104;
    public static final int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    public static final int SAMPLE_BIT = AudioFormat.ENCODING_PCM_16BIT;


    private int[] mVoiceFrequency = {125, 250, 500, 1000, 2000, 4000, 6000, 8000};// unit Hz
    private float mVolume = 0.5f;
    private float mVolumeStep = 0.5f;
    private float mVolumeMax = 10.0f;
    private float mVolumeMin = 0.5f;

    private int mBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_HZ, CHANNEL, SAMPLE_BIT);
    private AudioTrack audioTrack;

    private Button addButton;
    private Button minusButton;
    private Button playButton;
    private GridGraph gridGraph;

    private SinWavTask mWavTask;
    private int frequencyIndex = -1;

    private short maxAmp = Short.MAX_VALUE;
    private int mFrameLength = SAMPLE_RATE_HZ;
    private short[][] mData = new short[mVoiceFrequency.length][mFrameLength];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.main_activity);
        initView();
        initData();
        onClick(addButton);
    }

    private void initView() {
        addButton = (Button) findViewById(R.id.button_fre_add);
        minusButton = (Button) findViewById(R.id.button_fre_minus);
        playButton = (Button) findViewById(R.id.button_play);
        addButton.setOnClickListener(this);
        minusButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        gridGraph = (GridGraph) findViewById(R.id.gridgraph);
    }

    @SuppressLint("NewApi")
    private void initData() {
        audioTrack =
                new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_HZ, CHANNEL, SAMPLE_BIT,
                        mBufferSize * 2, AudioTrack.MODE_STREAM);
        audioTrack.setVolume(mVolume);
        audioTrack.play();
        calculateWavSignal();
    }

    private void calculateWavSignal() {
        for (int i = 0; i < mVoiceFrequency.length; i++) {
            double w = 2.0 * Math.PI * mVoiceFrequency[i] / SAMPLE_RATE_HZ;
            for (int j = 0; j < mFrameLength; j++) {
                mData[i][j] = (short) (maxAmp * Math.sin(w * j));
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fre_add:
                frequencyIndex++;
                changeFrequency();
                break;
            case R.id.button_fre_minus:
                frequencyIndex--;
                changeFrequency();
                break;
            case R.id.button_play:
                if (null != mWavTask) {
                    if (mWavTask.isPause) {
                        mWavTask.reStart();
                        playButton.setBackgroundResource(R.drawable.playing_icon);
                    } else {
                        mWavTask.pause();
                        playButton.setBackgroundResource(R.drawable.pausing_icon);
                    }
                }
                break;
            default:
                break;
        }

    }

    private void changeFrequency() {
        if (frequencyIndex < 0) {
            frequencyIndex = 0;
        } else if (frequencyIndex >= mVoiceFrequency.length) {
            frequencyIndex = mVoiceFrequency.length - 1;
        }
        if (null == mWavTask) {
            mWavTask = new SinWavTask(frequencyIndex);
            mWavTask.start();
        } else {
            mWavTask.setFrequency(frequencyIndex);
        }
    }

    @SuppressLint("NewApi")
    private void volumeAdd() {
        mVolume += mVolumeStep;
        if (mVolume > mVolumeMax) {
            mVolume = mVolumeMax;
        }
        if (null != audioTrack) {
            audioTrack.setVolume(mVolume);
        }

    }

    @SuppressLint("NewApi")
    private void volumeMinus() {
        mVolume -= mVolumeStep;
        if (mVolume < mVolumeMin) {
            mVolume = mVolumeMin;
        }
        if (null != audioTrack) {
            audioTrack.setVolume(mVolume);
        }
    }

    class SinWavTask extends Thread {

        private int mFrequnecyIndex = 0;
        private boolean isRunning = true;
        private boolean isPause = true;

        public SinWavTask(int index) {
            mFrequnecyIndex = index;
        }

        @Override
        public void run() {
            while (isRunning) {
                // 将波形数据分段送入 audioTrack ,使切换更加迅速灵敏
                // if (!isPause) {
                // audioTrack.write(mData[mFrequnecyIndex], 0, mData[mFrequnecyIndex].length);
                // }
                int frameLength = mData[mFrequnecyIndex].length / 8;
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 0, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 2 * frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 3 * frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 4 * frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 5 * frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 6 * frameLength, frameLength);
                }
                if (!isPause) {
                    audioTrack.write(mData[mFrequnecyIndex], 7 * frameLength, frameLength);
                }
            }
        }

        public void setFrequency(int index) {
            if (mFrequnecyIndex != index) {
                mFrequnecyIndex = index;
            }
        }

        public void close() {
            isRunning = false;
        }

        public void pause() {
            isPause = true;
        }

        public void reStart() {
            isPause = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mWavTask) {
            mWavTask.close();
        }
    }
}
