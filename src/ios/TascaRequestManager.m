//
//  TascaRequestManager.m
//  Tasca Test
//
//  Created by Adriano Tornatore on 29/08/17.
//  Copyright © 2017 App&Map Srls. All rights reserved.
//

#import "TascaRequestManager.h"
#import "TascaDBHelper.h"
#import "TascaCommon.h"

@implementation TascaRequestManager

+ (TascaRequestManager*)sharedInstance
{
    static dispatch_once_t predicate = 0;
    __strong static id sharedObject = nil;
    //static id sharedObject = nil;  //if you're not using ARC
    dispatch_once(&predicate, ^{
        sharedObject = [[self alloc] init];
        //sharedObject = [[[self alloc] init] retain]; // if you're not using ARC
        
    });
    return sharedObject;
}


- (void)dealloc {
    // Should never be called, but just here for clarity really.
    
}

- (void)downloadItemsType:(NSString *)type lang:(NSString *)lang downloadImages:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock{
    
    __block double overallProgress = 0;
    float parts = 0;
    float step  = downloadImages==YES?2.0:1.0;
    __block int currentStep  = 0;


    AFJSONResponseSerializer *responseSerializer = [[AFJSONResponseSerializer alloc] init];
    [[AFHTTPSessionManager manager] setResponseSerializer:responseSerializer];

    [[AFHTTPSessionManager manager] GET:[TascaCommon getServerURLStringForPath:type
                                                                   andLanguage:lang
                                                                    lastUpdate:NO]
                             parameters:nil
                               progress:^(NSProgress * _Nonnull downloadProgress) {
                                   progressBlock(downloadProgress.fractionCompleted/step,0,step,currentStep);
                               }
                                success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {

                                    progressBlock(1/step,1,1,currentStep);
                                    currentStep+= 1;
                                    if(!responseObject || ![responseObject isKindOfClass:[NSArray class]]){
                                        finishBlock(responseObject);
                                        return;
                                    }
                                    NSArray *jsonArray = responseObject;

                                    __block int count = 0;

                                    for (NSDictionary *item in jsonArray) {
                                        if([type isEqualToString:WINES_PATH]) {
                                            [[TascaDBHelper sharedInstance] insertWine:item forLanguage:lang];
                                        } else if([type isEqualToString:ESTATES_PATH]) {
                                            [[TascaDBHelper sharedInstance] insertEstate:item forLanguage:lang];
                                        }
                                        count++;
                                        //                                        overallProgress += ((float)count/(float)jsonArray.count) * (1.0/(float)parts);
                                        //                                        progressBlock(overallProgress, count, jsonArray.count);
                                    }
                                    NSArray *imageURLStrings = [TascaCommon extractURLStringFromDictionary:jsonArray];
                                    if(downloadImages){

                                        int totalCount =[imageURLStrings count];
                                        count = 0;
                                        for (NSString *imageURLString in imageURLStrings) {
                                            [[TascaDBHelper sharedInstance] insertImage:@{
                                                                                          @"url":imageURLString,
                                                                                          @"local":@""
                                                                                          }];
                                            NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
                                            AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];

                                            NSURL *URL = [NSURL URLWithString:imageURLString];
                                            NSURLRequest *request = [NSURLRequest requestWithURL:URL];

                                            NSURLSessionDownloadTask *downloadTask = [manager downloadTaskWithRequest:request progress:^(NSProgress * _Nonnull downloadProgress) {

                                            } destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
                                                NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:YES error:nil];



                                                return [documentsDirectoryURL URLByAppendingPathComponent:[response suggestedFilename]];
                                            } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {

                                                count++;
                                                //If downloaded..
                                                if(error==nil && filePath!=nil && ![filePath isEqual:[NSNull null]]){
                                                    NSLog(@"File downloaded to: %@", filePath);
                                                    [[TascaDBHelper sharedInstance] updateImage:imageURLString data:@{
                                                                                                                      @"url":imageURLString,
                                                                                                                      @"local":filePath.absoluteString
                                                                                                                      }];
                                                } else {
                                                    NSLog(@"Download Error: %@", error);
                                                }
                                                float baseProgress= 0.5;
                                                baseProgress += ((double)count/(double)totalCount)/(double)step;
                                                progressBlock(baseProgress, count, totalCount,currentStep);
                                            }];
                                            [downloadTask resume];

                                        }
                                    }else{
                                        //Insert all images on db (as not downloaded...)
                                        for (NSString *imageURLString in imageURLStrings) {
                                            [[TascaDBHelper sharedInstance] insertImage:@{
                                                                                          @"url":imageURLString,
                                                                                          @"local":@""
                                                                                          }];
                                        }
                                        finishBlock(jsonArray);
                                    }

                                }
                                failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                                    finishBlock(error);
                                }];





}
- (void)downloadAll:(NSString *)lang downloadImages:(BOOL)downloadImages stepProgressBlock:(ProgressBlock)stepProgress currentProgressBlock:(ProgressBlock)currentProgress finishBlock:(CompletionBlock)finishBlock{
    NSInteger steps = downloadImages?3:2;
    NSInteger totalSteps = downloadImages?5:4;
    __block int currentStep = 0;
    if(currentProgress) {
        currentProgress(0, 0, steps, currentStep);
    }

    ProgressBlock progressoInterno = ^(float progress, int current, int total, int step) {
        int _step = currentStep + step;
        float _stepProgress = (float)_step/(float)totalSteps;
        float innerProgress = _stepProgress + (progress/(float)steps);
        if(currentProgress) {
            currentProgress(innerProgress,current,total,_step);
        }
        if(stepProgress) {
            //Qui ritorna > lo step corrente come valore.
            stepProgress(_step,current,total,step); //for this move only the step progress. reply only with the new cal step
        }
    };

    [self downloadWines:lang downloadImages:downloadImages currentProgressBlock:progressoInterno finishBlock:^(id obj) {
        currentStep=2; // force step to 2 Download estates.
        //        if(currentProgress) {
        //            currentProgress((float)currentStep/(float)totalSteps,currentStep,totalSteps, currentStep);
        //        }
        [self downloadEstates:lang downloadImage:downloadImages currentProgressBlock:progressoInterno finishBlock:^(id obj) {
            //finished prepare something...
            currentStep=4; //Force step to 4
            //            if(currentProgress) {
            //                currentProgress((float)currentStep/(float)totalSteps,currentStep,totalSteps, currentStep);
            //            }
            if(downloadImages==YES) {
                NSArray*notDownloaded = [[TascaDBHelper sharedInstance] getImagesNotDownloaded];
                if(notDownloaded.count>0) {
                    //Download all media...
                    NSMutableArray*map = [NSMutableArray new];
                    [notDownloaded enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                        [map addObject:obj[@"url"]];
                    }];
                    [self downloadAllMedia:map currentProgressBlock:progressoInterno finishBlock:^(id obj) {
                        if(currentProgress) {
                            currentProgress(1,1,1,currentStep); //not encrease step list, we are at the last one.
                        }
                        finishBlock([[TascaDBHelper sharedInstance] getImages]); //Return all images array.
                    }];
                    return;
                }
            }
            finishBlock([[TascaDBHelper sharedInstance] getImages]); //Return all images array.
        }];
    }];

}

- (void)downloadAllMedia:(NSArray *)array currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock{
    if(!array) {
        if(finishBlock) {
            finishBlock(@[]);
        }
    }
    if(array.count==0) {
        if(finishBlock) {
            finishBlock(@[]);
        }
    }

    __block long count = 0;
    __block NSMutableArray* data=[NSMutableArray new];
    long total = array.count;
    for (NSString*url in array) {
        [self downloadMedia:url disableCache:NO finishBlock:^(id obj) {
            count+=1;
            if(progressBlock) {
                progressBlock((float)count/(float)total,count,total,0);
            }
            //
            NSString*local = [TascaCommon nsStringIsNull:obj]==YES?@"":obj;
            [data addObject:@{@"url":url,@"local":local}]; //Create again structure.
            if(count==total) {
                if(finishBlock) {
                    finishBlock(data);
                }
            }
        }];
    }
}
-(void)downloadMedia:(NSString*)url disableCache:(BOOL)disableCache finishBlock:(CompletionBlock)finishBlock{
    if(disableCache == NO) {
        NSDictionary* exists =[[TascaDBHelper sharedInstance] getImageByUrl:url];
        if(exists) {
            NSString*local= @"";
            if(exists[@"local"] && [TascaCommon nsStringIsNull:exists[@"local"]]==NO) {
                local = exists[@"local"];
            }
            if(local.length>0) {
                finishBlock(local);
                return;
            }
        }
    }
    [[TascaDBHelper sharedInstance] replaceImage:url data:@{
                                                            @"url":url,
                                                            @"local":@""
                                                            }];
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];

    NSURL *URL = [NSURL URLWithString:url];
    NSURLRequest *request = [NSURLRequest requestWithURL:URL];

    NSURLSessionDownloadTask *downloadTask = [manager downloadTaskWithRequest:request progress:^(NSProgress * _Nonnull downloadProgress) {

    } destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
        NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:YES error:nil];



        return [documentsDirectoryURL URLByAppendingPathComponent:[response suggestedFilename]];
    } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {

        if(error==nil && filePath!=nil && ![filePath isEqual:[NSNull null]]){
            NSLog(@"File downloaded to: %@", filePath);
            [[TascaDBHelper sharedInstance] updateImage:url data:@{
                                                                   @"url":url,
                                                                   @"local":filePath.absoluteString
                                                                   }];
            finishBlock(filePath.absoluteString);
            return;
        } else {
            NSLog(@"Download Error: %@", error);
        }
        finishBlock(nil);

    }];
    [downloadTask resume];
}

- (void)checkWinesLastUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock{
    AFJSONResponseSerializer *responseSerializer = [[AFJSONResponseSerializer alloc] init];
    [[AFHTTPSessionManager manager] setResponseSerializer:responseSerializer];
    
    [[AFHTTPSessionManager manager] GET:[TascaCommon getServerURLStringForPath:WINES_PATH
                                                                   andLanguage:lang
                                                                    lastUpdate:YES]
                             parameters:nil
                               progress:^(NSProgress * _Nonnull downloadProgress) {
                                   
                               }
                                success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                                    
                                    NSDictionary *dict = responseObject;
                                    NSString *modifiedDateString = dict[@"modified"];
                                    
                                    NSComparisonResult comparisonResult = [[TascaDBHelper sharedInstance] compareLastUpdate:modifiedDateString lang:[TascaCommon getLanguage] table:WINES_TABLE];
                                    
                                    finishBlock([NSString stringWithFormat:@"%d",comparisonResult==NSOrderedSame]);
                                }
                                failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                                    finishBlock([NSString stringWithFormat:@"%d",NO]);
                                }];
}
- (void)downloadWines:(NSString *)lang downloadImages:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock{
    
    [self checkWinesLastUpdate:lang finishBlock:^(id obj) {
        if ([obj boolValue]==NO) {
            [self downloadItemsType:WINES_PATH
                               lang:lang
                     downloadImages:downloadImages
               currentProgressBlock:progressBlock
                        finishBlock:finishBlock];
        } else {
            finishBlock(nil);
        }
    }];
}
- (void)checkEstatesLastUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock{
    AFJSONResponseSerializer *responseSerializer = [[AFJSONResponseSerializer alloc] init];
    [[AFHTTPSessionManager manager] setResponseSerializer:responseSerializer];
    
    [[AFHTTPSessionManager manager] GET:[TascaCommon getServerURLStringForPath:ESTATES_PATH
                                                                   andLanguage:lang
                                                                    lastUpdate:YES]
                             parameters:nil
                               progress:^(NSProgress * _Nonnull downloadProgress) {
                                   
                               }
                                success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                                    
                                    NSDictionary *dict = responseObject;
                                    NSString *modifiedDateString = dict[@"modified"];
                                    
                                    NSComparisonResult comparisonResult = [[TascaDBHelper sharedInstance] compareLastUpdate:modifiedDateString lang:[TascaCommon getLanguage] table:ESTATES_TABLE];
                                    
                                    finishBlock([NSString stringWithFormat:@"%d",comparisonResult==NSOrderedSame]);
                                }
                                failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                                    finishBlock([NSString stringWithFormat:@"%d",NO]);
                                }];
}
- (void)downloadEstates:(NSString *)lang downloadImage:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock{
    [self checkEstatesLastUpdate:lang finishBlock:^(id obj) {
        if ([obj boolValue]==NO) {
            [self downloadItemsType:ESTATES_PATH
                               lang:lang
                     downloadImages:downloadImages
               currentProgressBlock:progressBlock
                        finishBlock:finishBlock];
        } else {
            finishBlock(nil);
        }
    }];
    
    
}
@end
