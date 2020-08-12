package com.snakelord.soundrecorder;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.telecom.RemoteConference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.snakelord.soundrecorder.R;
import com.snakelord.soundrecorder.RecorderVisualizerView;
import com.snakelord.soundrecorder.recorder.Complex;
import com.snakelord.soundrecorder.recorder.FFT;
import com.snakelord.soundrecorder.recorder.SoundRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public final class RecordFragment extends Fragment {

    private ImageButton startRecordImageButton;
    private ImageButton stopRecordImageButton;
    private Chronometer chronometer;
    private SoundRecorder soundRecorder;
    private static final String CHRONOMETER_STATE = "Chronometer state";
    private static final String IS_RECORD_STARTED = "Record state";

    private Handler handler = new Handler();
    private RecorderVisualizerView visualizerView;
    public static final int REPEAT_INTERVAL = 0;
    //m chart
    public static CombinedChart mChart;
    //Audio record
    public AudioRecord audioRecord = null;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.txt";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public int bufferSize = 0;
//    Complex[] fftTempArray;
//    Complex[] fftArray;
    public static double[] absNormalizedSignal = null;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreChronometer(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View recordScreen = inflater.inflate(R.layout.fragment_record_screen, container, false);
        visualizerView = (RecorderVisualizerView)recordScreen.findViewById(R.id.visualizer);
        mChart = (CombinedChart) recordScreen.findViewById(R.id.FFT);
        chronometer = recordScreen.findViewById(R.id.chronometer);
        startRecordImageButton = recordScreen.findViewById(R.id.start_recording);
        stopRecordImageButton = recordScreen.findViewById(R.id.stop_recording);
        setButtonsClickListener();
        //Audio record
        bufferSize = AudioRecord.getMinBufferSize
                (RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;
        Log.i("FFFFFBUFFER", String.valueOf(bufferSize));
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        //Chart
        mChart.getDescription().setEnabled(false);
//        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);
//        mChart.setOnChartValueSelectedListener(this);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);



        return recordScreen;
    }

    private void initRecorder() {
        if (soundRecorder == null) soundRecorder = new SoundRecorder(getContext());
    }

    private void setButtonsClickListener() {
        startRecordImageButton.setOnClickListener(v -> startRecord());
        stopRecordImageButton.setOnClickListener(v -> stopRecord());
    }

    private void restoreChronometer(Bundle savedState) {
        if (savedState != null) {
            if (savedState.getBoolean(IS_RECORD_STARTED)) {
                chronometer.setBase(savedState.getLong(CHRONOMETER_STATE));
                chronometer.start();
                startRecordImageButton.setEnabled(false);
            }
        }
    }

    private void startRecord() {
        initRecorder();
        if ( soundRecorder.startRecording()) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            audioRecord.startRecording();

            startRecordImageButton.setClickable(false);
            handler.post(updateVisualizer);

        }
    }

    private void stopRecord() {
        if (isSoundRecorderCreated()) {

            soundRecorder.stopRecording();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();

            audioRecord.stop();
            mChart.setData(null);
            mChart.invalidate();
            //reset the raw graph
            visualizerView.clear();         //clear
            visualizerView.invalidate();    //reset
            handler.removeCallbacks(updateVisualizer);

            startRecordImageButton.setClickable(true);
        }
    }

    private boolean isSoundRecorderCreated() {
        return soundRecorder != null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CHRONOMETER_STATE, chronometer.getBase());
        if (soundRecorder != null)
            outState.putBoolean(IS_RECORD_STARTED, soundRecorder.isRecordStarted());
    }

    @Override
    public void onDestroy() {
        if (isSoundRecorderCreated()) soundRecorder.releaseRecorder();
        super.onDestroy();
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (soundRecorder.isRecordStarted()) // if we are already recording
            {
                float x = soundRecorder.recorder.getMaxAmplitude();
                visualizerView.addAmplitude(x); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView
                handler.postDelayed(this, REPEAT_INTERVAL);
                writeAudioDataToFile();
            }
        }
    };


    //Audio:
    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        int read = 0;
        if (SoundRecorder.recordStarted) {
                read = audioRecord.read(data, 0, bufferSize);
                if (read > 0) {
                    absNormalizedSignal = calculateFFT(data);
                }
                CombinedData dataLine = new CombinedData();
                LineData lineDatas = new LineData();
                lineDatas.addDataSet((ILineDataSet) dataChart());
                dataLine.setData(lineDatas);
                mChart.setData(dataLine);
                mChart.invalidate();
        }
    }
    private static DataSet dataChart() {

        LineData d = new LineData();
        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < 512; index++) {
            entries.add(new Entry(index, (float) absNormalizedSignal[index]));
        }

        LineDataSet set = new LineDataSet(entries, "Request Ots approved");
        set.setColor(Color.GREEN);
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.GREEN);
        set.setCircleRadius(5f);
        set.setFillColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);
        return set;
    }
    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();

        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public double[] calculateFFT(byte[] signal)
    {
        final int mNumberOfFFTPoints =1024;
        double mMaxFFTSample;

        double temp;

        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        y = FFT.fft(complexSignal); // --> Here I use FFT class

        mMaxFFTSample = 0.0;
        double mPeakPos = 0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }
        return absSignal;

    }
}
