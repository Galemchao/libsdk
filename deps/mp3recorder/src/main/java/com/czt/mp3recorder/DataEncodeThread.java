package com.czt.mp3recorder;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class DataEncodeThread extends HandlerThread implements AudioRecord.OnRecordPositionUpdateListener {
    private StopHandler mHandler;
    private static final int PROCESS_STOP = 1;
    private byte[] mMp3Buffer;
    private FileOutputStream mFileOutputStream;
    private RecorderStateListener mStateListener;
    private String mFileName;
    private boolean mStoped;

    public boolean isStoped(){
        return mStoped;
    }

    private static class StopHandler extends Handler {

        private DataEncodeThread encodeThread;
        private RecorderStateListener listener;
        private String filename;
        private Double mDuration;

        public StopHandler(Looper looper, DataEncodeThread encodeThread, RecorderStateListener listener, String filename) {
            super(looper);
            this.encodeThread = encodeThread;
            this.listener = listener;
            this.filename = filename;
            this.mDuration=0.00;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROCESS_STOP) {
                //处理缓冲区中的数据
                while (encodeThread.processData() > 0) ;
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null);
                encodeThread.flushAndRelease();
                getLooper().quit();
                if (listener != null) {
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("filename", filename);
                    data.put("duration", mDuration);
                    listener.onRecorderState("stoped", data);
                }
                encodeThread.mStoped = true;
            }
        }
    }

    /**
     * Constructor
     *
     * @param filename   file
     * @param bufferSize bufferSize
     * @throws FileNotFoundException file not found
     */
    public DataEncodeThread(String filename, int bufferSize) throws FileNotFoundException {
        super("DataEncodeThread");
        this.mFileName = filename;
        File f = new File(filename);
        if (f.exists()) {
            Log.i("ddd.pdk", "DataEncodeThread: 文件存在，需要删除：" + filename);
            if (!f.delete()) {
                Log.i("ddd.pdk", "DataEncodeThread: 删除成功：" + filename);
            }
        }
        this.mFileOutputStream = new FileOutputStream(filename);
        mMp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
        mStoped = false;
    }

    @Override
    public synchronized void start() {
        super.start();
        mHandler = new StopHandler(getLooper(), this, mStateListener, mFileName);
    }

    private void check() {
        if (mHandler == null) {
            throw new IllegalStateException();
        }
    }

    public void sendStopMessage(double duration) {
        check();
        mHandler.mDuration = duration;
        mHandler.sendEmptyMessage(PROCESS_STOP);
    }

    public Handler getHandler() {
        check();
        return mHandler;
    }

    public void setmStateListener(RecorderStateListener mStateListener) {
        this.mStateListener = mStateListener;
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
        // Do nothing
    }

    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        processData();
    }

    /**
     * 从缓冲区中读取并处理数据，使用lame编码MP3
     *
     * @return 从缓冲区中读取的数据的长度
     * 缓冲区中没有数据时返回0
     */
    private int processData() {
        if (mTasks.size() > 0) {
            Task task = mTasks.remove(0);
            short[] buffer = task.getData();
            int readSize = task.getReadSize();
            int encodedSize = LameUtil.encode(buffer, buffer, readSize, mMp3Buffer);
            if (encodedSize > 0) {
                try {
                    mFileOutputStream.write(mMp3Buffer, 0, encodedSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return readSize;
        }
        return 0;
    }

    /**
     * Flush all data left in lame buffer to file
     */
    private void flushAndRelease() {
        //将MP3结尾信息写入buffer中
        final int flushResult = LameUtil.flush(mMp3Buffer);
        if (flushResult > 0) {
            try {
                mFileOutputStream.write(mMp3Buffer, 0, flushResult);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                LameUtil.close();
            }
        }
    }

    private List<Task> mTasks = Collections.synchronizedList(new ArrayList<Task>());

    public void addTask(short[] rawData, int readSize) {
        mTasks.add(new Task(rawData, readSize));
    }

    private class Task {
        private short[] rawData;
        private int readSize;

        public Task(short[] rawData, int readSize) {
            this.rawData = rawData.clone();
            this.readSize = readSize;
        }

        public short[] getData() {
            return rawData;
        }

        public int getReadSize() {
            return readSize;
        }
    }
}
