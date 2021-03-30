//
//  TascaCommon.h
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//

#import <Foundation/Foundation.h>


#ifdef DEBUG
#define TascaLog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
#else
#   define TascaLog(...)
#endif

#define BASE_URL @"https://www.tascadalmerita.it/"
#define API_PATH @"api/"
#define ESTATES_PATH @"tenute/"
#define WINES_PATH @"vini/"
#define ESTATES_LAST_MODIFIED_PATH @"tenuta/?last_modified=true"
#define WINES_LAST_MODIFIED_PATH @"vini/?last_modified=true"

#define DEFAULT_LANGUAGE @"en"
#define ACCEPTED_LANGUAGES @[@"it", @"en", @"de"]

#define VALID_IMAGE_EXTENSIONS @[@"png", @"jpg", @"jpeg", @"gif"]

#define ACF_FIELD @"acf"

//DB

#define ESTATES_TABLE @"estates"
#define TASTES_TABLE @"tastes"
#define WINES_TABLE @"wines"
#define IMAGES_TABLE @"images"

@interface TascaCommon : NSObject


// Utility
+(BOOL)nsStringIsNull:(id)string;
+(id) jsonObjectFromString:(NSString*)json;
+ (NSString*)jsonFromObject:(id)object;
+ (void)setLanguage:(NSString *)language;
+ (NSString *)getLanguage;
+ (NSString *)getLanguage:(NSString *)requestedLanguage;

// URLs
+ (NSString *)getBaseURLString;
+ (NSString *)getServerURLStringForPath:(NSString *)path andLanguage:(NSString *)lang;
+ (NSString *)getServerURLStringForPath:(NSString *)path andLanguage:(NSString *)lang lastUpdate:(BOOL)lastUpdate;
+ (NSArray *)extractURLStringFromDictionary:(NSDictionary *)dictionary;

// Paths

+(NSString*)getDatabaseName;
+(NSString*)getDownloadPath;

@end
