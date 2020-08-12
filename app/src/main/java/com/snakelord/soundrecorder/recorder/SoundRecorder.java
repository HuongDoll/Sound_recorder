package com.snakelord.soundrecorder.recorder;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
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
    public MediaRecorder recorder;
    private WorkingFolder recordsPath;
    private Context context;
    private String recordName;
    public static boolean recordStarted = false;
    private static final String DATE_PATTERN = "dd-M-yyyy hh:mm:ss";
    private static final String RECORD_NAME = "/Record_";
    private static final String RECORD_FORMAT = ".mp3";
    private static final int RECORD_BIT_RATE = 128000;
    private static final int RECORD_SAMPLING_RATE = 44100;
    //Audio record
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
                //Audio start:
//                audioRecord.startRecording();
//                recordingThread = new Thread(new Runnable() {
//
//                    public void run() {
//                        writeAudioDataToFile();
//                    }
//                },"AudioRecorder Thread");

//                recordingThread.start();
            } catch (IllegalStateException e) {
                showErrorToast();
                WorkWithFiles.removeRecord(recordName);
                recordStarted = false;
            }
        }
        return isRecordStarted();
    }
////Audio:
//private void writeAudioDataToFile() {
//    byte data[] = new byte[bufferSize];
//    String filename = getTempFilename();
//    FileOutputStream os = null;
//
//    try {
//        os = new FileOutputStream(filename);
//    } catch (FileNotFoundException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//
//    int read = 0;
//
//    if (null != os) {
//        while (recordStarted) {
//            read = audioRecord.read(data, 0, bufferSize);
//            //Log.i("KKKKKKLL",String.valueOf(data[0]));
//
//            if (read > 0) {
//                absNormalizedSignal = calculateFFT(data);
//            }
//            CombinedData dataLine = new CombinedData();
//            LineData lineDatas = new LineData();
//            lineDatas.addDataSet((ILineDataSet) dataChart());
//
//            dataLine.setData(lineDatas);
//
////        xAxis.setAxisMaximum(data.getXMax() + 0.25f);
//
//            RecordFragment.mChart.setData(dataLine);
//            RecordFragment.mChart.invalidate();
//
////            String test = new String("[");
////            for (int i = 0; i < 512; i++) {
////                Log.v("FFFFFINDEX", String.valueOf(i));
////                test = test.concat(String.valueOf(absNormalizedSignal[i]));
////                test = test.concat(",");
////            }
////            test = test.concat("]");
////            Log.v("FFFFFUP", test);
//
////            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
////                try {
////                    os.write(data);
////
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////               }
//            //viet du lieu vao file o day
//        }
//
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//    private static DataSet dataChart() {
//
//        LineData d = new LineData();
//
//        ArrayList<Entry> entries = new ArrayList<Entry>();
//
//        for (int index = 0; index < 12; index++) {
//            entries.add(new Entry(index, (float) absNormalizedSignal[index]));
//        }
//
//        LineDataSet set = new LineDataSet(entries, "Request Ots approved");
//        set.setColor(Color.GREEN);
//        set.setLineWidth(2.5f);
//        set.setCircleColor(Color.GREEN);
//        set.setCircleRadius(5f);
//        set.setFillColor(Color.GREEN);
//        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        set.setDrawValues(true);
////        set.setValueTextSize(10f);
////        set.setValueTextColor(Color.GREEN);
//
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        d.addDataSet(set);
//
//        return set;
//    }

//    private String getTempFilename(){
//        String filepath = Environment.getExternalStorageDirectory().getPath();
//
//        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
//
//        if(!file.exists()){
//            file.mkdirs();
//        }
//
//        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
//
//        if(tempFile.exists())
//            tempFile.delete();
//        Log.i("DMDM", file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
//        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
//    }
//    public double[] calculateFFT(byte[] signal)
//    {
//        final int mNumberOfFFTPoints =1024;
//        double mMaxFFTSample;
//
//        double temp;
//
//        Complex[] y;
//        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
//        double[] absSignal = new double[mNumberOfFFTPoints/2];
//
//        for(int i = 0; i < mNumberOfFFTPoints; i++){
//            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
//            complexSignal[i] = new Complex(temp,0.0);
//        }
//
//        y = FFT.fft(complexSignal); // --> Here I use FFT class
//
//        mMaxFFTSample = 0.0;
//        double mPeakPos = 0;
//        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
//        {
//            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
//            if(absSignal[i] > mMaxFFTSample)
//            {
//                mMaxFFTSample = absSignal[i];
//                mPeakPos = i;
//            }
//        }
////        String test = "";
////        for (int i =0; i < 512; i++)
////        {
////            test = test.concat(String.valueOf(absSignal[i]));
////            test.concat("/");
////        }
////        Log.v("FFFFF",test);
//        return absSignal;
//
//    }
//end
    public void stopRecording() { 
        if (isRecordStarted()) {

            recorder.stop();
            showStopToast();
            recordStarted = false;
//            audioRecord.stop();
//            recordingThread.destroy();
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
//        audioRecord.release();
        recorder.release();
        recorder = null;
    }
}
