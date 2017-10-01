package to.msn.wings.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    ContentResolver resolver;
    Cursor cursor;

    Timer mTimer;
    Handler mHandler = new Handler();

    Button button1;
    Button button2;
    Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        resolver = getContentResolver();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                Log.d("ANDROID_TEST","許可済みです。");
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                Log.d("ANDROID_TEST","許可されていません。");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            Log.d("ANDROID_TEST","Andeoid5以下です。");
            getContentsInfo();
        }


        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("ANDROID_TEST",String.valueOf(cursor.moveToNext())); //質問①←この行がないと、②のif分岐でelseに入った瞬間にエラーが起きてしまう。③でも同じことが起きている。
                if (mTimer == null) {
                    if (cursor.moveToNext() == true){       //②
                        Log.d("ANDROID_TEST","1つ進もうとしている");
                        cursor.moveToNext();
                        showImage();
                        Log.d("ANDROID_TEST","1つ進めた");
                    } else {
                        Log.d("ANDROID_TEST", "最初に戻ろうとしている");
                        cursor.moveToFirst();
                        Log.d("ANDROID_TEST", "最初に戻れた");
                        showImage();
                    }
                } else{
                    Log.d("ANDROID_TEST","再生中なので押せません。");
                }
            }

        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimer == null) {
                    button2.setText("停止");
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d("ANDROID_TEST",String.valueOf(cursor.moveToNext()));  //③
                            if (cursor.moveToNext() == true){
                                Log.d("ANDROID_TEST","再生で進もうとする");
                                cursor.moveToNext();
                                Log.d("ANDROID_TEST","再生で進めた");
                            } else {
                                Log.d("ANDROID_TEST","最初に戻ろうとする");
                                cursor.moveToFirst();
                                Log.d("ANDROID_TEST","最初に戻れた");
                            }
                            mHandler.post(new Runnable() {
                            @Override
                                public void run() {
                                    showImage();
                                }
                            });
                        }
                    }, 2000, 2000);
                } else {
                    mTimer.cancel();
                    mTimer = null;
                    button2.setText("再生");
                    Log.d("ANDROID_TEST","再生を止めました。");
                }
            }
        });


        //質問④:進むボタンを押すときと、戻るボタンを押すときで、表示される画像が異なるのですが、なぜでしょうか。Galaryには全部で8枚写真がありますが、進む/戻るでそれぞれ3種類ずつでループが走っているようです...
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("ANDROID_TEST",String.valueOf(cursor.moveToPrevious()));
                if (mTimer == null) {
                    if (cursor.moveToPrevious() == true){
                        Log.d("ANDROID_TEST","1つ戻ろうとしている");
                        cursor.moveToPrevious();
                        showImage();
                        Log.d("ANDROID_TEST","1つ戻れた");
                    } else {
                        Log.d("ANDROID_TEST", "最後に戻ろうとしている");
                        cursor.moveToLast();
                        Log.d("ANDROID_TEST", "最後に戻れた");
                        showImage();
                    }
                } else{
                    Log.d("ANDROID_TEST","再生中なので押せません。");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                    Log.d("ANDROID_TEST","許可されました");
                } else {
                    Log.d("ANDROID_TEST","許可されませんでした。ボタンを押せなくします。");
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    button3.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    public void getContentsInfo() {
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
        cursor.moveToFirst();
        showImage();
    }

    public void showImage() {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}

