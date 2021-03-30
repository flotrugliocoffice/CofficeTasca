//
//  CofficeTascaInternal.h
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TascaRequestManager.h"
#import "TascaCommon.h"
#import "TascaDBHelper.h"



@interface CofficeTascaInternal : NSObject

+ (CofficeTascaInternal* )sharedInstance;


/* db methods */

- (NSDictionary *)getEstateByID:(long)estateID forLanguage:(NSString *)lang;

- (NSDictionary *)getTasteByID:(long)tasteID forLanguage:(NSString *)lang;

- (NSDictionary *)getWineByID:(long)wineID forLanguage:(NSString *)lang;


- (NSArray *)getEstatesTypeAhead:(NSString *)term forLanguage:(NSString *)lang;

- (NSArray *)getTastesTypeAhead:(NSString *)term;

- (NSArray *)getWinesTypeAhead:(NSString *)term forLanguage:(NSString *)lang;


- (NSArray *)getEstates:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;

- (NSArray *)getTastes:(NSString *)term limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;

- (NSArray *)getWines:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;


- (long)insertEstate:(NSDictionary *)estateDictionary;

- (long)insertTaste:(NSDictionary *)tasteDictionary;

- (long)insertWine:(NSDictionary *)wineDictionary;


- (BOOL)editEstate:(long)itemID data:(NSDictionary *)estateDictionary;

- (BOOL)editTaste:(long)itemID data:(NSDictionary *)tasteDictionary;

- (BOOL)editWine:(long)itemID data:(NSDictionary *)wineDictionary;


- (BOOL)deleteEstate:(long)itemID;

- (BOOL)deleteTaste:(long)itemID;

- (BOOL)deleteWine:(long)itemID;


- (int)clearEstates;

- (int)clearTastes;

- (int)clearWines;

- (int)clearImages;

- (int)clearCache;

- (int)clearAll;


/* api methods */


- (void)downloadAll:(NSString *)lang downloadImages:(BOOL)downloadImages stepProgressBlock:(ProgressBlock)stepProgress currentProgressBlock:(ProgressBlock)currentProgress finishBlock:(CompletionBlock)finishBlock;

- (void)downloadAllMedia:(NSArray *)array currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

- (void)checkWinesUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock;

- (void)downloadWines:(NSString *)lang downloadImages:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

- (void)checkEstatesUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock;

- (void)downloadEstates:(NSString *)lang downloadImages:(BOOL)downloadImages currentProgressBlock:(ProgressBlock)progressBlock finishBlock:(CompletionBlock)finishBlock;

- (NSArray*)getMediaMap;

- (NSString *)getImageByUrl:(NSString *)imageUrl finishBlock:(CompletionBlock)finishBlock;
- (NSString *)getImageByUrl:(NSString *)imageUrl overrideCache:(BOOL)overrideCache finishBlock:(CompletionBlock)finishBlock;

@end
