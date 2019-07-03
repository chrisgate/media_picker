//
//  PHAsset+Category.m
//  Pods-Runner
//
//  Created by zdsoft on 2019/6/19.
//

#import "PHAsset+Category.h"

@implementation PHAsset (Category)

- (NSString *)originalFilename {
    NSString *fname;
    if (@available(iOS 9.0, *)) {
        PHAssetResource *resource = [PHAssetResource assetResourcesForAsset:self].firstObject;
        fname = resource.originalFilename;
    }
    
    if (fname == nil) {
        fname = [self valueForKey:@"filename"];
    }
    return fname;
}


@end
