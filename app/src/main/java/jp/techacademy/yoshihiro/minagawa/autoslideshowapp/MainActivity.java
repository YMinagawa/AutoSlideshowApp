package jp.techacademy.yoshihiro.minagawa.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Cursor mCursor;
    private int mNumImage = 0;
    private TextView mTextView;
    private Button mButton1, mButton2, mButton3;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private MyTimerTask mMyTimerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Android6.0以降
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //許可されているか確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo();
            } else {
                Log.d("ANDROID", "許可されていない");
                //許可されていないので許可ダイアログを表示
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }

        }else{
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された");
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(){
        //画像情報の取得
        ContentResolver resolver = getContentResolver();
        //カーソルクラスはデータベース上の検索結果を格納する
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
                null, //項目(null = 全項目)
                null, //フィルタ条件(null=フィルタなし)
                null, //フィルタ用のパラメーター
                null //ソート(null ソートなし)
        );

        if(mCursor.moveToFirst()) {
            do {
                mNumImage += 1;
            } while (mCursor.moveToNext());
        }

        Log.d("TEST", "NumImage : " + mNumImage);

        if(mCursor.moveToFirst()){

            mButton1 = (Button)findViewById(R.id.button1);
            mButton1.setOnClickListener(this);
            mButton2 = (Button)findViewById(R.id.button2);
            mButton2.setOnClickListener(this);
            mButton3 = (Button)findViewById(R.id.button3);
            mButton3.setOnClickListener(this);

            mTextView = (TextView)findViewById(R.id.textView);

            setImage();

        }

        //カーソルを使い終わったらcloseメソッドを呼び出す
        //mCursor.close();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1){
            goBackImage();
        }else if(v.getId() == R.id.button2){
            startSlideShow();
        }else if(v.getId() == R.id.button3){
            goForwardImage();
        }
    }

    //1つ前の画像に戻る処理
    private void goBackImage(){

        if(mCursor.isFirst()==false){
            mCursor.moveToPrevious();
        }else{
            mCursor.moveToLast();
        }

        setImage();
    }

    //スライドショー
    private void startSlideShow(){

        //再生
        if(mButton2.getText().toString().equals("再生")){
            mButton2.setText("停止");
            mButton1.setEnabled(false);
            mButton3.setEnabled(false);


/*            //1000ms毎に1枚ずつ次の画像へ
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goForwardImage();
                    mHandler.postDelayed(this, 2000);
                }
            }, 2000);*/

            if(mTimer==null){
                //TimerとTimerTaskはcancel()時に破棄されるのでインスタンスを作りなおす
                mMyTimerTask = new MyTimerTask();
                //デーモンスレッドのときはtrue
                mTimer = new Timer(true);
                //schedule(TimerTask, delay, period)
                mTimer.schedule(mMyTimerTask, 0, 2000);
            }


        //停止
        }else if(mButton2.getText().toString().equals("停止")){
            mButton2.setText("再生");
            mButton1.setEnabled(true);
            mButton3.setEnabled(true);

            if(mTimer !=null){
                mTimer.cancel();
                mTimer = null;
            }

//            mHandler.removeCallbacksAndMessages(null);

        }
    }

    //次の画像へ進む処理
    private void goForwardImage(){

        if(mCursor.isLast() == false){
            mCursor.moveToNext();
        }else{
            mCursor.moveToFirst();
        }

        setImage();
    }

    //画像と現在の枚数の表示
    private void setImage(){
        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

        mTextView.setText(mCursor.getPosition()+1 + "/" + mNumImage);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    goForwardImage();
                }
            });
        }
    }


}


