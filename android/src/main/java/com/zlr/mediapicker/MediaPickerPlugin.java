package com.zlr.mediapicker;

import com.pm.mediapicker.MediaPicker;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * @author pm
 * @date 2019/6/10
 * @email puming@zdsoft.cn
 */
public class MediaPickerPlugin implements MethodChannel.MethodCallHandler {
    private MediaPickerDelegate mDelegate;

    private final PluginRegistry.Registrar mRegistrar;

    public static void registerWith(PluginRegistry.Registrar registrar) {
        if (registrar.activity() == null) {
            // When a background flutter view tries to register the plugin, the registrar has no activity.
            // We stop the registration process as this plugin is foreground only.
            return;
        }
        MethodChannel channel =
                new MethodChannel(registrar.messenger(), "media_picker");
        MediaPickerPlugin instance = new MediaPickerPlugin(registrar);
        channel.setMethodCallHandler(instance);
    }

    private MediaPickerPlugin(PluginRegistry.Registrar registrar) {
        this.mRegistrar = registrar;
        mDelegate = new MediaPickerDelegate(registrar.activity());
        registrar.addActivityResultListener(mDelegate);
        registrar.addRequestPermissionsResultListener(mDelegate);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (mRegistrar.activity() == null) {
            result.error("no_activity", "media_picker plugin requires a foreground activity.", null);
            return;
        }
        if (methodCall.method.equals("pickMedias")) {
            int maxSelectCount = methodCall.argument("maxSelectCount");
            MediaPicker.getInstance().setMaxCount(maxSelectCount).setSingleType(true).showCamera(true);
            mDelegate.onLaunchPickerActivity(methodCall, result, false);
        } else if (methodCall.method.equals("pickMedia")) {
            MediaPicker.getInstance().setMaxCount(1).setSingleType(true).showCamera(true);
            mDelegate.onLaunchPickerActivity(methodCall, result, true);
        } else if (methodCall.method.equals("takePhoto")) {
            mDelegate.onLaunchTakePhotoActivity(methodCall, result);
        }
    }
}
