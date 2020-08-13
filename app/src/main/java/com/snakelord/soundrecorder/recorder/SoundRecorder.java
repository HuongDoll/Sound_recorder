package com.snakelord.soundrecorder.recorder;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.snakelord.soundrecorder.R;
import com.snakelord.soundrecorder.RecordFragment;
import com.snakelord.soundrecorder.workingFolder.WorkWithFiles;
import com.snakelord.soundrecorder.workingFolder.WorkingFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SoundRecorder {
    public static final String FFT_EX = ".txt";
    public MediaRecorder recorder;
    public static WorkingFolder recordsPath;
    private Context context;
    private String recordName;
    public static boolean recordStarted = false;
    private static final String DATE_PATTERN = "dd-M-yyyy hh:mm:ss";
    private static final String RECORD_NAME = "/Record_";
    private static final String RECORD_FORMAT = ".mp3";
    private static final int RECORD_BIT_RATE = 128000;
    private static final int RECORD_SAMPLING_RATE = 44100;
    //Audio record
    private Handler handler = new Handler();
    private Thread recordingThread = null;
    public AudioRecord audioRecord = null;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.txt";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public int bufferSize = 0;
    Complex[] fftTempArray;
    Complex[] fftArray;
    public static double[] absNormalizedSignal = null;
    public SoundRecorder(Context context) {
        this.context = context;
        recordsPath = new WorkingFolder(context);

    }

    private void initRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(RECORD_BIT_RATE);
        recorder.setAudioSamplingRate(RECORD_SAMPLING_RATE);
        recordName = generateRecordName();
        recorder.setOutputFile(recordName);
        //init Audiorecord
        bufferSize = AudioRecord.getMinBufferSize
                (RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;
        Log.i("FFFFFBUFFER", String.valueOf(bufferSize));
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        try {
            recorder.prepare();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRecordName() {
        return recordsPath.getFolder() + RECORD_NAME + new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date()) + RECORD_FORMAT;
    }


    public boolean startRecording() {
        if (!isRecordStarted()) {
            try {
                initRecorder();
                recorder.start();
                recordStarted = true;
                showStartToast();
            } catch (IllegalStateException e) {
                showErrorToast();
                WorkWithFiles.removeRecord(recordName);
                recordStarted = false;
            }
        }
        return isRecordStarted();
    }
    public void stopRecording() { 
        if (isRecordStarted()) {
            recorder.stop();
            showStopToast();
            recordStarted = false;
            audioRecord.stop();
        }
    }

    public boolean isRecordStarted() { return recordStarted; }

    private void showErrorToast() {
        Toast.makeText(context, R.string.illegal_exception_when_mic_used,Toast.LENGTH_SHORT).show();
    }
    private void showStopToast(){
        Toast.makeText(context, R.string.stop_record,Toast.LENGTH_SHORT).show();
    }
    private void showStartToast(){
        Toast.makeText(context, R.string.start_record, Toast.LENGTH_SHORT).show();
    }
    public void releaseRecorder() {
        recorder.reset();
        audioRecord.release();
        recorder.release();
        recorder = null;
    }
}
