package com.zlr.mediapicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.pm.takephoto.LaunchActivity;
import com.pm.takephoto.TakePhotoActivity;
import com.pm.mediapicker.MediaPicker;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.app.ActivityCompat;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * @author pm
 * @date 2019/6/11
 * @email puming@zdsoft.cn
 */
public class MediaPickerDelegate implements PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {
    private static final String TAG = "MediaPickerDelegate";
    private final ImageResizer mImageResizer;
    private static final int REQUEST_CODE_CHOOSE_SINGLE = 1001;
    private static final int REQUEST_CODE_CHOOSE_MULTIPLE = 1002;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1003;

    private static final int GENERAL = 0;
    private static final int ID_CARD_FRONT = 1;
    private static final int ID_CARD_BACK = 2;
    private static final int HANDHELD = 3;
    private static final int BANK_CARD = 4;


    private Activity activity;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_MEDIA = 1000;
    private MethodChannel.Result mResult;
    private boolean mSingle;

    public MediaPickerDelegate(Activity activity) {
        this.activity = activity;
        final File externalFilesDirectory =
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        ExifDataCopier exifDataCopier = new ExifDataCopier();
        mImageResizer = new ImageResizer(externalFilesDirectory, exifDataCopier);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_CHOOSE_SINGLE) {
            handleChooseMediaResult(resultCode, intent);
        } else if (requestCode == REQUEST_CODE_CHOOSE_MULTIPLE) {
            handleChooseMediaResult(resultCode, intent);
        }
        return true;
    }

    private void handleChooseMediaResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(MediaPicker.EXTRA_SELECT_IMAGES);
            if (paths != null && !paths.isEmpty()) {
                if (mSingle && paths.size() == 1) {
                    mResult.success(paths.get(0));
                } else {
                    mResult.success(paths);
                }
            } else {
                mResult.error("error", "error", null);
            }
        } else {
            mResult.error("error", "error", null);
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (requestCode == REQUEST_CODE_MEDIA) {
            if (permissionGranted) {
                launchPickerActivity();
                return true;
            }
        }
        return false;
    }

    public void onLaunchPickerActivity(MethodCall call, MethodChannel.Result rawResult, boolean single) {
        mResult = rawResult;
        mSingle = single;
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_MEDIA);
            return;
        }
        launchPickerActivity();
    }

    public void onLaunchTakePhotoActivity(MethodCall call, MethodChannel.Result rawResult) {
        launchTakePhotoActivity(call);
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void launchPickerActivity() {
        Log.d(TAG, "launchPickerActivity: ");
        Intent intent = new Intent("com.zlr.supply.action.media");
        activity.startActivityForResult(intent, mSingle ? REQUEST_CODE_CHOOSE_SINGLE : REQUEST_CODE_CHOOSE_MULTIPLE);
//        MediaPicker.getInstance().start(activity, mSingle ? REQUEST_CODE_CHOOSE_SINGLE : REQUEST_CODE_CHOOSE_MULTIPLE);
//        presentPicker(5,true,new ArrayList<>(),new HashMap<>());
    }

    private void launchTakePhotoActivity(MethodCall call) {
        Intent intent = new Intent("com.zlr.supply.action.takephoto");
//        Intent intent = new Intent(activity, LaunchActivity.class);
        int type = call.argument("cameraType");
        switch (type) {
            case GENERAL:
                intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_GENERAL);
                break;
            case ID_CARD_FRONT:
                intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_ID_CARD_FRONT);
                break;
            case ID_CARD_BACK:
                intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_ID_CARD_BACK);
                break;
            case HANDHELD:
                intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_HANDHELD);
                break;
            case BANK_CARD:
                intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_BANK_CARD);
                break;
            default:
                throw new IllegalArgumentException("Invalid camera type: " + type);
        }
        activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }
}
