#import "MediaPickerPlugin.h"
#import "ZLPhotoBrowser.h"
#import <Photos/Photos.h>
#import "PHAsset+Category.h"

@implementation MediaPickerPlugin {
    NSDictionary *_arguments;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"media_picker"
            binaryMessenger:[registrar messenger]];
  MediaPickerPlugin* instance = [[MediaPickerPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    UIApplication *app = UIApplication.sharedApplication;
    FlutterViewController *controller = (FlutterViewController *)app.delegate.window.rootViewController;
    
    _arguments = call.arguments;
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
        
    } else if ([@"imagePicker" isEqualToString:call.method]) { //选择照片
        NSInteger maxSelectCount = 1;  //默认选择1张
        if ([_arguments objectForKey:@"maxSelectCount"]) {
            maxSelectCount = [[_arguments objectForKey:@"maxSelectCount"] integerValue];
        }
        
        ZLPhotoActionSheet *actionSheet = [[ZLPhotoActionSheet alloc] init];
        actionSheet.configuration.allowTakePhotoInLibrary = NO;
        actionSheet.configuration.allowSelectOriginal = NO;
        actionSheet.configuration.allowEditImage = NO;
        actionSheet.configuration.editAfterSelectThumbnailImage = YES;
        actionSheet.configuration.languageType = 1;
        actionSheet.configuration.navBarColor = [UIColor whiteColor];
        actionSheet.configuration.navTitleColor = [UIColor blackColor];
        actionSheet.configuration.bottomBtnsNormalTitleColor = kRGB(51, 135, 251);
        actionSheet.configuration.statusBarStyle = UIStatusBarStyleDefault;
        actionSheet.configuration.maxSelectCount = maxSelectCount;
        actionSheet.sender = controller;
        
        @zl_weakify(self);
        [actionSheet setSelectImageBlock:^(NSArray<UIImage *> * _Nullable images, NSArray<PHAsset *> * _Nonnull assets, BOOL isOriginal) {
            NSMutableArray *results = [NSMutableArray array];
            for (UIImage *image in images) {
                NSString *path = [weak_self saveImage:image];
                [results addObject:path];
            }
            result(results);
        }];
        [actionSheet showPhotoLibrary];
    } else if ([@"showCamera" isEqualToString:call.method]) { //打开照相机
        //照相机剪裁类型（默认没有剪裁框）
        ZLCropType cropType = ZLCropTypeNone;
        if ([_arguments objectForKey:@"cropType"]) {
            cropType = [[_arguments objectForKey:@"cropType"] integerValue];
        }
        
        ZLCustomCamera *camera = [[ZLCustomCamera alloc] init];
        camera.allowRecordVideo = NO;
        camera.cropType = cropType;
                
        @zl_weakify(self);
        camera.doneBlock = ^(UIImage *image, NSURL *videoUrl) {
            NSString *path= [weak_self saveImage:image];
            result(path);
        };
        [controller showDetailViewController:camera sender:nil];
    } else {
        result(FlutterMethodNotImplemented);
    }
}


// 保存图片
- (NSString *)saveImage: (UIImage *)image {
    BOOL saveAsPNG = [self hasAlpha:image];
    NSData *data =
    saveAsPNG ? UIImagePNGRepresentation(image) : UIImageJPEGRepresentation(image, 1.0);
    NSString *fileExtension = saveAsPNG ? @"image_picker_%@.png" : @"image_picker_%@.jpg";
    NSString *guid = [[NSProcessInfo processInfo] globallyUniqueString];
    NSString *tmpFile = [NSString stringWithFormat:fileExtension, guid];
    NSString *tmpDirectory = NSTemporaryDirectory();
    NSString *tmpPath = [tmpDirectory stringByAppendingPathComponent:tmpFile];
    
    if ([[NSFileManager defaultManager] createFileAtPath:tmpPath contents:data attributes:nil]) {
        return tmpPath;
    }
    return @"";
}

// Returns true if the image has an alpha layer
- (BOOL)hasAlpha:(UIImage *)image {
    CGImageAlphaInfo alpha = CGImageGetAlphaInfo(image.CGImage);
    return (alpha == kCGImageAlphaFirst || alpha == kCGImageAlphaLast ||
            alpha == kCGImageAlphaPremultipliedFirst || alpha == kCGImageAlphaPremultipliedLast);
}


@end
