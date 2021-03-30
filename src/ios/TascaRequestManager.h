//
//  TascaRequestManager.h
//  Tasca Test
//
//  Created by Adriano Tornatore on 29/08/17.
//  Copyright © 2017 App&Map Srls. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"

typedef void (^ ProgressBlock)(float progress, int current, int total);
typedef void (^ CompletionBlock)(id obj);


@interface TascaRequestManager : NSObject

+ (TascaRequestManager* )sharedInstance;

- (void)downloadAll:(NSString *)lang downloadImages:(BOOL)downloadImages stepProgressBlock:(ProgressBlock)stepProgress currentProgressBlock:(ProgressBlock)currentProgress finishBlock:(CompletionBlock)finishBlock;


- (void)downloadAllMedia:(NSArray *)array currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

-(void)downloadMedia:(NSString*)url disableCache:(BOOL)disableCache finishBlock:(CompletionBlock)finishBlock;

- (void)checkWinesLastUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock;

- (void)downloadWines:(NSString *)lang downloadImages:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

- (void)checkEstatesLastUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock;

- (void)downloadEstates:(NSString *)lang downloadImage:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

@end
