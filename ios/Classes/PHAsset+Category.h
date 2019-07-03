//
//  PHAsset+Category.h
//  Pods-Runner
//
//  Created by zdsoft on 2019/6/19.
//

#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface PHAsset (Category)

@property(nonatomic, strong, readonly) NSString *originalFilename;


@end

NS_ASSUME_NONNULL_END
