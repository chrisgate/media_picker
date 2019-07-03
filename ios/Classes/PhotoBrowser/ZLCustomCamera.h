//
//  ZLCustomCamera.h
//  CustomCamera
//
//  Created by long on 2017/6/26.
//  Copyright © 2017年 long. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZLDefine.h"


typedef NS_ENUM(NSInteger, ZLCropType) {
    ZLCropTypeNone = 0,     //无剪裁框
    ZLCropTypeIDFront = 1,  //身份证正面剪裁框
    ZLCropTypeIDBack = 2,   //身份证背面剪裁框
    ZLCropTypeIDHand = 3,   //手持身份证剪裁框
    ZLCropTypeDebitcard = 4 //借记卡(银行卡)剪裁框
};

@interface ZLCustomCamera : UIViewController

//是否允许拍照 默认YES
@property (nonatomic, assign) BOOL allowTakePhoto;
//是否允许录制视频 默认YES
@property (nonatomic, assign) BOOL allowRecordVideo;

//最大录制时长 默认15s
@property (nonatomic, assign) NSInteger maxRecordDuration;

//视频分辨率 默认 ZLCaptureSessionPreset1280x720
@property (nonatomic, assign) ZLCaptureSessionPreset sessionPreset;

//视频格式 默认 ZLExportVideoTypeMp4
@property (nonatomic, assign) ZLExportVideoType videoType;

//录制视频时候进度条颜色 默认 RGB(80, 180, 234)
@property (nonatomic, strong) UIColor *circleProgressColor;

//剪裁框类型（默认无剪裁框）
@property (nonatomic, assign) ZLCropType cropType;

/**
 确定回调，如果拍照则videoUrl为nil，如果视频则image为nil
 */
@property (nonatomic, copy) void (^doneBlock)(UIImage *image, NSURL *videoUrl);

@end
