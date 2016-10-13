package pe.edu.idat.tarea;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

public class MainActivity extends Activity {

    private  static final int ACTION_TAKE_PHOTO_B = 1;
    private  static final int ACTION_TAKE_PHOTO_S = 2;
    private  static final int ACTION_TAKE_VIDEO = 3;
    //imagen
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    //video
    private static final String VIDEO_STORAGE_KEY = "viewvideo";
    private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "viewvideovisibility";
    private VideoView mVideoView;
    private Uri mVideoUri;

    private String mCurrenPhotoPaht;
    //nombre de que se inicia al guardar la foto
    private static final String JPEG_FILE_PREFIX = "IMG_";
    //formato en que guardamos la imagen
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorangeDirFactory =null;

    /* fotos  album en la aplicacion */
    private String getAlbumName(){
        return getString(R.string.album_name);
    }

    private File getAlbumDir(){
        File storageDir = null;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            storageDir = mAlbumStorangeDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null){
                if (!storageDir.mkdirs()){
                    if (!storageDir.exists()){
                        Log.d("CameraSample","fail to create directory");
                        return null;
                    }
                }
            }
        }else{
            Log.v(getString(R.string.album_name),"External storage is not mounted READ/WRITE");
        }
        return storageDir;
    }
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX+timeStamp+"_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName,JPEG_FILE_SUFFIX,albumF);
        return imageF;
    }
    private File setUpPhotoFile() throws IOException{
        File f=createImageFile();
        mCurrenPhotoPaht=f.getAbsolutePath();
        return f;
    }
    private void setPic(){
        int targetW= mImageView.getWidth();
        int targetH=mImageView.getHeight();

        BitmapFactory.Options bmOptions =  new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(mCurrenPhotoPaht,bmOptions);
        int photoW=bmOptions.outWidth;
        int photoH=bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0)||(targetH >0)){
            scaleFactor = Math.min(photoW/targetW,photoH/targetH);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrenPhotoPaht,bmOptions);

        mImageView.setImageBitmap(bitmap);

        mImageView.setImageBitmap(bitmap);
        mVideoUri = null;
        mImageView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrenPhotoPaht);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrenPhotoPaht = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrenPhotoPaht = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        mImageBitmap = (Bitmap) extras.get("data");
        mImageView.setImageBitmap(mImageBitmap);
        mVideoUri = null;
        mImageView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void handleBigCameraPhoto() {

        if (mCurrenPhotoPaht != null) {
            setPic();
            galleryAddPic();
            mCurrenPhotoPaht = null;
        }

    }

    private void handleCameraVideo(Intent intent) {
        mVideoUri = intent.getData();
        mVideoView.setVideoURI(mVideoUri);
        mImageBitmap = null;
        mVideoView.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }

    Button.OnClickListener mTakePicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };

    Button.OnClickListener mTakePicSOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
                }
            };

    Button.OnClickListener mTakeVidOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakeVideoIntent();
                }
            };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView1);
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mImageBitmap = null;
        mVideoUri = null;

        Button picBtn = (Button) findViewById(R.id.btnIntend);
        setBtnListenerOrDisable(
                picBtn,
                mTakePicOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        Button picSBtn = (Button) findViewById(R.id.btnIntendS);
        setBtnListenerOrDisable(
                picSBtn,
                mTakePicSOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        Button vidBtn = (Button) findViewById(R.id.btnIntendV);
        setBtnListenerOrDisable(
                vidBtn,
                mTakeVidOnClickListener,
                MediaStore.ACTION_VIDEO_CAPTURE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorangeDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorangeDirFactory = new BaseAlbumDirFactory();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case ACTION_TAKE_PHOTO_S: {
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                }
                break;
            } // ACTION_TAKE_PHOTO_S

            case ACTION_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    handleCameraVideo(data);
                }
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
        outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
        mVideoView.setVideoURI(mVideoUri);
        mVideoView.setVisibility(
                savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }


    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setBtnListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setText(
                    getText(R.string.cannot).toString() + " " + btn.getText());
            btn.setClickable(false);
        }
    }

}
