package com.example.simplelite;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int PICK_FROM_ALBUM = 1;
    private File tempFile;
    private boolean isPermission = true;
    private static final int PICK_FROM_CAMERA = 2;
    private static final String TAG = "blackjin";

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    int rgb[] = new int[720 * 720];
    char[] output_real = new char[] {'ㄱ', 'ㄴ', 'ㄷ'};
    float max = 0.0f;       //가장 큰 확률의 값을 저장해 줄 변수
    int max_index = 0;      //가장 큰 확률의 index를 저장해 줄 변수

    String stringPath = "/sdcard/samplevideo.3gp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonStartCameraPreview = (Button)findViewById(R.id.button3);
        Button buttonStopCameraPreview = (Button)findViewById(R.id.button4);
        tedPermission();

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceView.getHolder().setFixedSize(720, 720);
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        buttonStartCameraPreview.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!previewing) {
                    camera = Camera.open();
                    if(camera != null) {
                        try {
                            camera.setPreviewDisplay(surfaceHolder);
                            camera.setDisplayOrientation(90);
                            camera.startPreview();

                            previewing = true;
                            if(previewing) {
                                camera.setPreviewCallback(new Camera.PreviewCallback() {

                                    @Override
                                    public void onPreviewFrame(byte[] data, Camera camera) {
                                        //이 부분에서 실시간으로 영상을 처리해 줄 코드를 넣으면 된다.
                                        //int frameHeight = camera.getParameters().getPreviewSize().height;
                                        //int frameWidth = camera.getParameters().getPreviewSize().width;
                                        // number of pixels//transforms NV21 pixel data into RGB pixels
                                        // convertion
                                        decodeYUV420SP(data, 720, 720);
                                        /*
                                        SurfaceView의 Camera를 이용한 영상의 색상코드는 RGB값이 아닌 YUV값이다.
                                        YUV포맷의 값을 RGB포맷으로 변환해주는 메소드이다.
                                        기존의 rgb배열에는 YUV포맷으로 값이 저장되어 있지만, 해당 메소드를 사용해 RGB포맷의 값으로 변환해준다.
                                        */
                                        /*for (int i = 0; i < rgb.length; i++)
                                            Log.d("rgb", Integer.toString(rgb[i]));*/
                                        int pixel2 = rgb[250000];
                                        float test_value = (pixel2 & 0xff) / (float) 255;
                                        //****************Log.d("test", Float.toString(test_value));
                                        //bytes_img[0][y][x][z] =(pixel & 0xff) / (float) 255;

                                        /*char[] output_real = new char[] {'ㄱ', 'ㄴ', 'ㄷ'};   //output배열과 매치시킬 실제 결과값 배열
                                        try {
                                            //모델파일을 불러오는 구문
                                            float[][][][] bytes_img = new float[1][720][720][3];
                                            /*실시간 영상을 분석하여 rgb값으로 바꿔 저장한 rgb배열은 1차원 배열이다.
                                            * 모델과 비교하기 위해서는 4차원 배열로 변환해 주어야 한다.
                                            * 그를 위한 배열 선언부분이다.

                                            for (int z = 0; z < 3; z++) {     //1차원 형태의 rgb배열을 4차원 형태로 변환해 저장해주는 반복문
                                                for (int y = 0; y < 720; y++) {
                                                    for(int x = 0; x < 720; x++) {
                                                        int pixel = rgb[x + 720 * y];
                                                        bytes_img[0][y][x][z] =(pixel & 0xff) / (float) 255;
                                                    }
                                                }
                                            }

                                            float[][] output = new float[1][3];  //출력결과 3개의 확률들을 저장해 줄 배열 선언
                                            tflite.run(bytes_img, output);    //모델파일 실행 출력결과는 output배열에 저장

                                            float max = 0.0f;       //가장 큰 확률의 값을 저장해 줄 변수
                                            int max_index = 0;      //가장 큰 확률의 index를 저장해 줄 변수
                                            for(int i=0; i<output[0].length; i++) {      //output 배열의 최댓값을 구해주는 반복문
                                                if(max < output[0][i]) {
                                                    max = output[0][i];     //가장 크게 나온 확률의 값을 저장해줌
                                                    max_index = i;          //가장 크게 나온 확률의 index를 저장해줌
                                                }
                                            }

                                            max = max*100;      //초기 확률값이 0~1사이의 실수이므로 100을 곱해줌으로써 0~100사이로 값 조정
                                            Float.toString(max);      //TextView에 띄워줄 것이기 때문에 String으로 형변환
                                            TextView tv = findViewById(R.id.result_0);
                                            tv.setText("이 사진은" + output_real[max_index] + "입니다. [확률 : " + max + "]");     //TextView의 내용 변경

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }*/
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        buttonStopCameraPreview.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camera != null && previewing) {
                    camera.stopPreview();
                    previewing = false;
                    //camera.release();
                    camera = null;
                }
            }
        });

        //앨범에서 이미지를 가져올 때 버튼
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission) goToAlbum();
                else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });

        //카메라에서 이미지를 가져올 때 버튼
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission)  takePhoto();
                else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });

        class NewRunnable implements Runnable {

            @Override
            public void run() {

                while (true) {
                    //char[] output_real = new char[] {'ㄱ', 'ㄴ', 'ㄷ'};   //output배열과 매치시킬 실제 결과값 배열
                    try {
                        try {
                            Interpreter tflite = getTfliteInterpreter("test_720.tflite");
                            max = 0.0f;       //가장 큰 확률의 값을 저장해 줄 변수
                            max_index = 0;      //가장 큰 확률의 index를 저장해 줄 변수
                            //모델파일을 불러오는 구문
                            float[][][][] bytes_img = new float[1][720][720][3];
                            /*실시간 영상을 분석하여 rgb값으로 바꿔 저장한 rgb배열은 1차원 배열이다.
                             * 모델과 비교하기 위해서는 4차원 배열로 변환해 주어야 한다.
                             * 그를 위한 배열 선언부분이다.*/
                            Log.d("test_line1", "first_line");
                            for (int z = 0; z < 3; z++) {     //1차원 형태의 rgb배열을 4차원 형태로 변환해 저장해주는 반복문
                                for (int y = 0; y < 720; y++) {
                                    for (int x = 0; x < 720; x++) {
                                        int pixel = rgb[x + 720 * y];
                                        bytes_img[0][x][y][z] = (pixel & 0xff) / (float) 255;
                                    }
                                }
                            }

                            float[][] output = new float[1][3];  //출력결과 3개의 확률들을 저장해 줄 배열 선언
                            tflite.run(bytes_img, output);    //모델파일 실행 출력결과는 output배열에 저장
                            Log.d("test_line2", "second_line");
                            //float max = 0.0f;       //가장 큰 확률의 값을 저장해 줄 변수
                            //int max_index = 0;      //가장 큰 확률의 index를 저장해 줄 변수
                            for (int i = 0; i < output[0].length; i++) {      //output 배열의 최댓값을 구해주는 반복문
                                if (max < output[0][i]) {
                                    max = output[0][i];     //가장 크게 나온 확률의 값을 저장해줌
                                    max_index = i;          //가장 크게 나온 확률의 index를 저장해줌
                                }
                            }

                            //max = max*100;      //초기 확률값이 0~1사이의 실수이므로 100을 곱해줌으로써 0~100사이로 값 조정
                            Float.toString(max);      //TextView에 띄워줄 것이기 때문에 String으로 형변환
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    TextView tv = findViewById(R.id.result_0);
                                    tv.setText("이 사진은" + output_real[max_index] + "입니다. [확률 : " + max + "]");     //TextView의 내용 변경
                                }
                            });
                            Log.d("test_line3", "third_line");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        NewRunnable nr = new NewRunnable() ;
        Thread t = new Thread(nr) ;
        t.start() ;

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    //앨범에서 이미지 가져오기
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage2();

        }
    }

    private void check(Bitmap originalBm) {
        char[] output_real = new char[] {'ㄱ', 'ㄴ', 'ㄷ'};   //output배열과 매치시킬 실제 결과값 배열
        try {
            int width = originalBm.getWidth();
            int height = originalBm.getHeight();
            float[][][][] bytes_img = new float[1][720][720][3];

            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 720; y++) {
                    for(int x = 0; x < 720; x++) {
                        int pixel = originalBm.getPixel(x, y);
                        bytes_img[0][y][x][z] =(pixel & 0xff) / (float) 255;
                    }
                }
            }

            Interpreter tflite = getTfliteInterpreter("test_720.tflite");

            float[][] output = new float[1][3];
            tflite.run(bytes_img, output);

            Log.d("predict", Arrays.toString(output[0]));

            float max = 0.0f;       //가장 큰 확률의 값을 저장해 줄 변수
            int max_index = 0;      //가장 큰 확률의 index를 저장해 줄 변수
            for(int i=0; i<output[0].length; i++) {      //output 배열의 최댓값을 구해주는 반복문
                if(max < output[0][i]) {
                    max = output[0][i];     //가장 크게 나온 확률의 값을 저장해줌
                    max_index = i;          //가장 크게 나온 확률의 index를 저장해줌
                }
            }

            max = max*100;      //초기 확률값이 0~1사이의 실수이므로 100을 곱해줌으로써 0~100사이로 값 조정
            Float.toString(max);      //TextView에 띄워줄 것이기 때문에 String으로 형변환
            TextView tv = findViewById(R.id.result_0);
            tv.setText("이 사진은" + output_real[max_index] + "입니다. [확률 : " + max + "]");     //TextView의 내용 변경

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //이미지 넣기
    private void setImage() {

        ImageView imageView = findViewById(R.id.imageView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        int width = 720; // 축소시킬 너비
        int height = 720; // 축소시킬 높이
        float bmpWidth = originalBm.getWidth();
        float bmpHeight = originalBm.getHeight();

        if (bmpWidth > width) {
            // 원하는 너비보다 클 경우의 설정
            float mWidth = bmpWidth / 100;
            float scale = width/ mWidth;
            bmpWidth *= (scale / 100);
            bmpHeight *= (scale / 100);
        } else if (bmpHeight > height) {
            // 원하는 높이보다 클 경우의 설정
            float mHeight = bmpHeight / 100;
            float scale = height/ mHeight;
            bmpWidth *= (scale / 100);
            bmpHeight *= (scale / 100);
        }
        bmpWidth += 1;
        bmpHeight += 1;
        Bitmap resizedBmp = Bitmap.createScaledBitmap(originalBm, (int) bmpWidth, (int) bmpHeight, true);
        Log.d("test", Integer.toString(resizedBmp.getWidth()));
        Log.d("test", Integer.toString(resizedBmp.getHeight()));
        originalBm = rotateImage(resizedBmp, 90);
        imageView.setImageBitmap(originalBm);
        //imageView.setImageBitmap(rotateImage(originalBm, 90));
        //int pixel = originalBm.getPixel(14, 14);
        //Log.d("getPixel", Integer.toUnsignedString(pixel, 16));   //결과값 : ff293f3d

        check(originalBm);

    }

    private void setImage2() {

        ImageView imageView = findViewById(R.id.imageView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        //imageView.setImageBitmap(originalBm);
        imageView.setImageBitmap(rotateImage(originalBm, 90));
        //int pixel = originalBm.getPixel(14, 14);
        //Log.d("getPixel", Integer.toUnsignedString(pixel, 16));   //결과값 : ff293f3d

        check(originalBm);

    }

    //이미지 회전시키는 함수
    public Bitmap rotateImage(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    //카메라에서 이미지 가져오기
    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "{package name}.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    //카메라에서 찍은 사진을 저장할 파일 만들기
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    void decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        // Pulled directly from:
        // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                r = 0;
        else if (r > 262143)
                r = 262143;
                if (g < 0)
                g = 0;
        else if (g > 262143)
                g = 262143;
                if (b < 0)
                b = 0;
        else if (b > 262143)
                b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

}
