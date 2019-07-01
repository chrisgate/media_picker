
package android.takephoto.src.main.java.com.pm.takephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pm.takephoto.utils.DimensionUtil;
import com.pm.takephoto.utils.ImageUtil;
import com.pm.takephoto.utils.ScreenUtils;
import com.pm.takephoto.widget.MaskView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.IntDef;

/**
 * 负责，相机的管理。同时提供，裁剪遮罩功能。
 */
public class CameraView extends FrameLayout {
    private static final String TAG = "CameraView";

    /**
     * 照相回调
     */
    public interface OnTakePictureCallback {
        void onPictureTaken(Bitmap bitmap);
    }

    /**
     * 垂直方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_HORIZONTAL = 90;
    /**
     * 水平翻转方向 {@link #setOrientation(int)}
     */
    public static final int ORIENTATION_INVERT = 270;

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    public @interface Orientation {

    }

    private CameraViewTakePictureCallback cameraViewTakePictureCallback = new CameraViewTakePictureCallback();

    private ICameraControl cameraControl;

    /**
     * 相机预览View
     */
    private View displayView;
    /**
     * 身份证，银行卡，等裁剪用的遮罩
     */
    private MaskView maskView;

    /**
     * 用于显示提示证 "请对齐身份证正面" 之类的
     */
    private ImageView hintView;
    private TextView hintTextView;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    public void setOrientation(@Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraControl = new Camera2Control(getContext());
        } else {
            cameraControl = new Camera1Control(getContext());
        }
        displayView = cameraControl.getDisplayView();
        addView(displayView);

        maskView = new MaskView(getContext());
        addView(maskView);

        hintView = new ImageView(getContext());
        addView(hintView);

        hintTextView = new TextView(getContext());
        hintTextView.setTextColor(Color.WHITE);
        addView(hintTextView);
    }

    public void start() {
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        cameraControl.stop();
        setKeepScreenOn(false);
    }


    public void takePicture(final File file, final OnTakePictureCallback callback) {
        cameraViewTakePictureCallback.file = file;
        cameraViewTakePictureCallback.callback = callback;
        cameraControl.takePicture(cameraViewTakePictureCallback);
    }

    public void setMaskType(@MaskView.MaskType int maskType) {
        maskView.setMaskType(maskType);
        maskView.setVisibility(VISIBLE);
        hintView.setVisibility(VISIBLE);
        hintTextView.setVisibility(INVISIBLE);

        int hintResourceId = R.drawable.bd_ocr_hint_align_id_card;
        switch (maskType) {
            case MaskView.MASK_TYPE_ID_CARD_FRONT:
                hintResourceId = R.drawable.bd_ocr_hint_align_id_card;
                break;
            case MaskView.MASK_TYPE_ID_CARD_BACK:
                hintResourceId = R.drawable.bd_ocr_hint_align_id_card_back;
                break;
            case MaskView.MASK_TYPE_BANK_CARD:
                hintResourceId = R.drawable.bd_ocr_hint_align_bank_card;
                break;
            case MaskView.MASK_TYPE_HANDHELD:
                hintView.setVisibility(VISIBLE);
                hintTextView.setText("将手持身份证至于此区域进行拍摄");
                hintTextView.setVisibility(VISIBLE);
                break;
            case MaskView.MASK_TYPE_NONE:
            default:
                maskView.setVisibility(INVISIBLE);
                hintView.setVisibility(INVISIBLE);
                hintTextView.setVisibility(INVISIBLE);
                break;
        }

        hintView.setImageResource(hintResourceId);
    }

    public Rect getMaskRect(){
        return maskView.getFrameRect();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        displayView.layout(left, 0, right, bottom - top);
        maskView.layout(left, 0, right, bottom - top);

        int hintViewWidth = DimensionUtil.dpToPx(150);
        int hintViewHeight = DimensionUtil.dpToPx(25);

        int hintViewLeft = (getWidth() - hintViewWidth) / 2;
        int hintViewTop = maskView.getFrameRect().bottom + DimensionUtil.dpToPx(16);

        hintView.layout(hintViewLeft, hintViewTop, hintViewLeft + hintViewWidth, hintViewTop + hintViewHeight);
    }

    /**
     * 拍摄后的照片。需要进行裁剪。有些手机（比如三星）不会对照片数据进行旋转，而是将旋转角度写入EXIF信息当中，
     * 所以需要做旋转处理。
     *
     * @param outputFile 写入照片的文件。
     * @param imageFile  原始照片。
     * @param rotation   照片exif中的旋转角度。
     * @return 裁剪好的bitmap。
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Bitmap crop(File outputFile, File imageFile, int rotation) {
        Log.d(TAG, "crop: outputFile = " + outputFile.getAbsolutePath());
        Log.d(TAG, "crop: imageFile = " + imageFile.getAbsolutePath());
        try {
            // BitmapRegionDecoder不会将整个图片加载到内存。
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageFile.getAbsolutePath(), true);

            Rect previewFrame = cameraControl.getPreviewFrame();
            Log.d(TAG, "crop: previewFrame=" + previewFrame);

            int width = rotation % 180 == 0 ? decoder.getWidth() : decoder.getHeight();
            int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();

            Rect frameRect = maskView.getFrameRect();
//            Rect frameRect = new Rect(54,406,1026,1033);
            Log.d(TAG, "crop: frameRect=" + frameRect);

            int left = width * frameRect.left / maskView.getWidth();
            int top = height * frameRect.top / maskView.getHeight();
            int right = width * frameRect.right / maskView.getWidth();
            int bottom = height * frameRect.bottom / maskView.getHeight();

            // 高度大于图片
            if (previewFrame.top < 0) {
                // 宽度对齐。
                int adjustedPreviewHeight = previewFrame.height() * getWidth() / previewFrame.width();
                int topInFrame = ((adjustedPreviewHeight - frameRect.height()) / 2)
                        * getWidth() / previewFrame.width();
                int bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2) * getWidth()
                        / previewFrame.width();

                // 等比例投射到照片当中。
                top = topInFrame * height / previewFrame.height();
                bottom = bottomInFrame * height / previewFrame.height();
            } else {
                // 宽度大于图片
                if (previewFrame.left < 0) {
                    // 高度对齐
                    int adjustedPreviewWidth = previewFrame.width() * getHeight() / previewFrame.height();
                    int leftInFrame = ((adjustedPreviewWidth - maskView.getFrameRect().width()) / 2) * getHeight()
                            / previewFrame.height();
                    int rightInFrame = ((adjustedPreviewWidth + maskView.getFrameRect().width()) / 2) * getHeight()
                            / previewFrame.height();

                    // 等比例投射到照片当中。
                    left = leftInFrame * width / previewFrame.width();
                    right = rightInFrame * width / previewFrame.width();
                }
            }

            Rect region = new Rect();
            region.left = left;
            region.top = top;
            region.right = right;
            region.bottom = bottom;

            // 90度或者270度旋转
            if (rotation % 180 == 90) {
                int x = decoder.getWidth() / 2;
                int y = decoder.getHeight() / 2;

                int rotatedWidth = region.height();
                int rotated = region.width();

                // 计算，裁剪框旋转后的坐标
                region.left = x - rotatedWidth / 2;
                region.top = y - rotated / 2;
                region.right = x + rotatedWidth / 2;
                region.bottom = y + rotated / 2;
                region.sort();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            //            options.inPreferredConfig = Bitmap.Config.RGB_565;

            // 最大图片大小。
            int maxPreviewImageSize = 2560;
            int size = Math.min(decoder.getWidth(), decoder.getHeight());
            size = Math.min(size, maxPreviewImageSize);

            options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = Math.max(options.outWidth, options.outHeight);
            options.inTargetDensity = size;

            Bitmap bitmap = decoder.decodeRegion(region, options);

            if (rotation != 0) {
                // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                if (bitmap != rotatedBitmap) {
                    // 有时候 createBitmap会复用对象
                    bitmap.recycle();
                }
                bitmap = rotatedBitmap;
            }

            try {
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (cameraViewTakePictureCallback.thread != null) {
            cameraViewTakePictureCallback.thread.quit();
        }
    }

    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {

        private File file;
        private OnTakePictureCallback callback;

        HandlerThread thread = new HandlerThread("cropThread");
        Handler handler;

        {
            thread.start();
            handler = new Handler(thread.getLooper());
        }

        @Override
        public void onPictureTaken(final byte[] data) {
            Log.d(TAG, "onPictureTaken: data=" + data.length);
            Log.d(TAG, "onPictureTaken: thread name= " + Thread.currentThread().getName());
            if (maskView.getMaskType() == MaskView.MASK_TYPE_NONE) {
                final int rotation = ImageUtil.getOrientation(data);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //只保存图片尺寸大小，不保存图片到内存
                options.inJustDecodeBounds = false;
                //缩放比例
                options.inSampleSize = 2;
                // 根据拍照所得的数据创建位图
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length, options);
                Matrix matrix = new Matrix();
                //旋转90度,照出来的图片初始是横着的
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                callback.onPictureTaken(bitmap);
            } else {

                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int rotation = ImageUtil.getOrientation(data);
                            final File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), "jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                            fileOutputStream.write(data);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = crop(file, tempFile, rotation);
                                    callback.onPictureTaken(bitmap);
                                    boolean deleted = tempFile.delete();
                                    if (!deleted) {
                                        tempFile.deleteOnExit();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
                callback.onPictureTaken(handleImage(data));
            }
        }
    }

    private Bitmap handleImage(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只保存图片尺寸大小，不保存图片到内存
        options.inJustDecodeBounds = false;
        //缩放比例
        options.inSampleSize = 2;
        // 根据拍照所得的数据创建位图
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                data.length, options);
        int width = options.outWidth;
        int height = options.outHeight;
        Log.d(TAG, "run: width=" + width);
        Log.d(TAG, "run: height=" + height);
        Log.d(TAG, "handleImage: w=" + bitmap.getWidth());
        Log.d(TAG, "handleImage: h=" + bitmap.getHeight());
        Log.d(TAG, "handleImage: left=" + maskView.getLeft());
        Log.d(TAG, "handleImage: top=" + maskView.getTop());
        Log.d(TAG, "handleImage: width=" + maskView.getWidth());
        Log.d(TAG, "handleImage: height=" + maskView.getHeight());
        Matrix matrix = new Matrix();
        //旋转90度,照出来的图片初始是横着的
        matrix.setRotate(90);
        //适配设置
        float rateH = (float) bitmap.getHeight() / ScreenUtils.getScreenWidth(getContext());
        float rateW = (float) bitmap.getWidth() / ScreenUtils.getScreenHeight(getContext());
        Rect rect = maskView.getFrameRect();
        Log.d(TAG, "onPictureTaken: rateW=" + rateW);
        float marginRight = (float) rect.left * rateH + 0.5f;
        float marginTop = (float) rect.top * rateW + 0.5f;
        float cropBitmapWidth = (float) (rect.bottom - rect.top) * rateW + 0.5f;
        float cropBitmapHeight = (float) (rect.right - rect.left) * rateH + 0.5f;
        //创建裁剪之后的bitmap，原图片不能直接操作
//        int x = (int) (width - marginRight - cropBitmapWidth);
        int x = (int) (marginTop);
        int y = (int) (marginRight);
        int w = (int) (cropBitmapWidth);
        int h = (int) (cropBitmapHeight);
        bitmap = Bitmap.createBitmap(bitmap, x, y, w, h, matrix, true);
        Log.d(TAG, "handleImage: rw=" + bitmap.getWidth());
        Log.d(TAG, "handleImage: rh=" + bitmap.getHeight());
        return bitmap;
    }
}
