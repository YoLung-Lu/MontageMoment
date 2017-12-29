package com.example.luyolung.montage.moment.Utils;

/**
 * Created by luyolung on 28/12/2017.
 */

import android.app.ProgressDialog;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import bolts.Task;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;

import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvTranspose;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

public class MovieCreator {
    private final String mInputVideoPath;
    private final String mOutputVideoPath;
    private ArrayList<String> mImageList = new ArrayList<>();

    public MovieCreator(String inputFilePath, String filePath) {
        mInputVideoPath = inputFilePath;
        mOutputVideoPath = filePath;
    }

    public Task<String> generateMontageVideo() {
        return Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return mergeVideoAndImage();
            }
        });
    }

    private String mergeVideoAndImage() {
        File file = new File(mInputVideoPath);
        if (!file.exists()) {
            return "";
        }

        if (mImageList == null || mImageList.size() == 0) {
            mImageList = getDefaultImage();
        }

        // Get video time.
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(mInputVideoPath);
//        String inputVideoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        try {
            FrameGrabber grabber1 = new FFmpegFrameGrabber(mInputVideoPath);
//            long totalFrameCount = grabber1.getFrameNumber();
            long totalFrameCount = getFrameCount(mInputVideoPath);

            grabber1.start();

            //Log.e("WIDTH",grabber2.getSampleRate()+"");

            FrameRecorder recorder = new FFmpegFrameRecorder(mOutputVideoPath,
//                                                             grabber1.getImageWidth(), grabber1.getImageHeight(),
                                                             grabber1.getImageHeight(), grabber1.getImageWidth(),
                                                             grabber1.getAudioChannels());
            // Image -> frame converter
            OpenCVFrameConverter converter = getConverter();

            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber1.getFrameRate());
            recorder.setSampleRate(grabber1.getSampleRate());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setTimestamp(grabber1.getTimestamp());
            //recorder.setSampleFormat(grabber2.getSampleFormat());
            //recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.start();

            Frame frame1;
            int currentImage = 0;
            int currentFrame = 0; // Include image, audio, ...?
            int currentImageFrame = 0; // Only image frame.
            // Insert image by x frame.
            int x = 25;

            while ((frame1 = grabber1.grabFrame()) != null) {
                IplImage frameImage = converter.convertToIplImage(frame1);
                if (frameImage != null) {
                    // Frame is image frame.
                    IplImage rotatedImage = rotate(frameImage, 90);
                    // record rotated frame.
                    recorder.record(converter.convert(rotatedImage));
                    currentImageFrame++;
                } else {
                    // record original frame (audio).
                    recorder.record(frame1);
                }

                currentFrame++;

                // Insert image.
                if (currentImage < mImageList.size() &&
                    currentFrame >= totalFrameCount*(currentImage+1)/mImageList.size()) {

                    opencv_core.IplImage image = cvLoadImage(mImageList.get(currentImage));
                    Frame frame = converter.convert(image);
                    for (int i = 0; i < x; i++) {
                        recorder.record(frame);
                    }
                    currentImage++;
                }
            }

            recorder.stop();
            grabber1.stop();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return mOutputVideoPath;
    }

//    private Frame rotate(Frame frame) {
//        IplImage
//        return null;
//    }

    private IplImage rotate(IplImage IplSrc, int angle) {
        IplImage img= IplImage.create(IplSrc.height(), IplSrc.width(), IplSrc.depth(), IplSrc.nChannels());
        cvTranspose(IplSrc, img);
        cvFlip(img, img, angle);
        return img;
    }

    private int getFrameCount(String inputVideoPath) {
        // TODO: There are image frame and audio frames.
        try {
            FrameGrabber grabber1 = new FFmpegFrameGrabber(inputVideoPath);
            grabber1.start();
            int frameCount = 0;

            while (grabber1.grabFrame() != null) {
                frameCount++;
            }
            grabber1.stop();
            return frameCount;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private OpenCVFrameConverter getConverter() {
        return new OpenCVFrameConverter() {
            @Override
            public Frame convert(Object o) {
                return null;
            }

            @Override
            public Object convert(Frame frame) {
                return null;
            }
        };
    }

    private String mergeVideoAndImageTest(String inputVideoPath, final ArrayList<String> imagePath) {
        File file = new File(inputVideoPath);
        if (!file.exists()) {
            return "";
        }
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(inputVideoPath);
//        String inputVideoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long timeInMicroSecond = getFrameCount(inputVideoPath);
//        timeInMicroSecond = 275;

        try {
            FrameGrabber grabber1 = new FFmpegFrameGrabber(inputVideoPath);
            FrameRecorder recorder = new FFmpegFrameRecorder(mOutputVideoPath,
                                                             grabber1.getImageWidth(), grabber1.getImageHeight(),
                                                             grabber1.getAudioChannels());
            OpenCVFrameConverter converter = getConverter();
            grabber1.start();

            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber1.getFrameRate());
            recorder.setSampleRate(grabber1.getSampleRate());
            recorder.setAudioChannels(0);
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.setTimestamp(grabber1.getTimestamp());
            //recorder.setSampleFormat(grabber2.getSampleFormat());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.start();

            Frame frame1;
            int currentImage = 0;
            int currentFrame = 0;
            // Insert image by x frame.
            int x = 25;

            while ((frame1 = grabber1.grabFrame()) != null) {
                recorder.record(frame1);
                currentFrame++;

                // Insert image.
                if (currentFrame >= timeInMicroSecond*(currentImage+1)/imagePath.size()) {
                    opencv_core.IplImage image = cvLoadImage(imagePath.get(currentImage));
                    Frame frame = converter.convert(image);
                    for (int i = 0; i < x; i++) {
                        recorder.record(frame);
                    }
                    currentImage++;
                }
            }

            recorder.stop();
            grabber1.stop();

            //Log.e(LOG_TAG, "recorder initialize success");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String createMovie() {
        File folder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String path = folder.getAbsolutePath();
        String videoFilePath = path + "/Videos/film_first.mp4";
        String audioFilePath = path + "/music.mp3";
        return createMovie(videoFilePath, audioFilePath);
    }

    private String createMovie(String videoFilePath, String audioFilePath) {
        File folder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String path = folder.getAbsolutePath();
//        String videoFilePath = path + "/Videos/film_first.mp4";
//        String audioFilePath = path + "/music.mp3";

        //File file_folder = new File(path + "/Camera");
        //File[] files = file_folder.listFiles();

        try {

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(videoFilePath, 1280, 720);
            OpenCVFrameConverter converter = new OpenCVFrameConverter() {
                @Override
                public Frame convert(Object o) {
                    return null;
                }

                @Override
                public Object convert(Frame frame) {
                    return null;
                }
            };

            //recorder.setAudioCodec(avcodec.AV_CODEC_ID_AMR_NB);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);

            recorder.setVideoBitrate(10 * 1024 * 1024);
            recorder.setFrameRate(1);
            recorder.setVideoQuality(0);
            //recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFormat("mp4");

            recorder.start();

            int time = 1;
            int number_images = 3;
            for (int i = 0; i < number_images; i++) {
                opencv_core.IplImage image = cvLoadImage(path + "/Obama/obama" + i + ".jpg");
                for (int j = 0; j < time; j++) {
                    Frame frame = converter.convert(image);
                    recorder.record(frame);
                }
            }

            recorder.stop();
            mergeAudioAndVideo(videoFilePath, audioFilePath, mOutputVideoPath);

        } catch (Exception e) {
            Log.e("problem", "problem", e);
        }
        return /*videoFilePath;*/mOutputVideoPath;
    }

    private boolean mergeAudioAndVideo(String videoPath, String audioPath, String outPut)
        throws Exception {
        boolean isCreated = true;
        File file = new File(videoPath);
        if (!file.exists()) {
            return false;
        }
        try {
            FrameGrabber grabber1 = new FFmpegFrameGrabber(videoPath);
            FrameGrabber grabber2 = new FFmpegFrameGrabber(audioPath);

            grabber1.start();
            grabber2.start();

            //Log.e("WIDTH",grabber2.getSampleRate()+"");

            FrameRecorder recorder = new FFmpegFrameRecorder(outPut,
                                                             grabber1.getImageWidth(), grabber1.getImageHeight(),
                                                             grabber2.getAudioChannels());

            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber1.getFrameRate());
            //recorder.setSampleFormat(grabber2.getSampleFormat());
            recorder.setSampleRate(grabber2.getSampleRate());
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_MPEG4);
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.start();

            Frame frame1, frame2 = null;

            while ((frame1 = grabber1.grabFrame()) != null ||
                (frame2 = grabber2.grabFrame()) != null) {

                recorder.record(frame1);
                recorder.record(frame2);

            }

            recorder.stop();
            grabber1.stop();
            grabber2.stop();

            //Log.e(LOG_TAG, "recorder initialize success");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isCreated;

    }

    public void setImageList(ArrayList<String> imageList) {
        mImageList = imageList;
    }

    public ArrayList<String> getDefaultImage() {
        ArrayList<String> imageList = new ArrayList<>();

        File folder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String path = folder.getAbsolutePath();
        int number_images = 3;
        for (int j = 0; j < number_images; j++) {
            imageList.add(path + "/Obama/obama" + j + ".jpg");
        }
        return imageList;
    }
}