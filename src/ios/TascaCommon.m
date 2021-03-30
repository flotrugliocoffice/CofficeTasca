//
//  TascaCommon.m
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//

#import "TascaCommon.h"





@implementation TascaCommon

const NSString *_databaseName = @"tascadb";

#pragma mark - URLs


+ (NSString *)getServerURLStringForPath:(NSString *)path andLanguage:(NSString *)lang{
    return [self getServerURLStringForPath:path andLanguage:lang lastUpdate:NO];
}

+ (NSString *)getServerURLStringForPath:(NSString *)path andLanguage:(NSString *)lang lastUpdate:(BOOL)lastUpdate{
    
    lang = [self getLanguage:lang];
    if ([lang isEqualToString:@"it"]) {
        lang = @"";
    }
    NSString *urlLanguage = [lang isEqualToString:@""] ? lang : [NSString stringWithFormat:@"%@/", lang];
    
    NSString *finalUrlString = [BASE_URL stringByAppendingFormat:@"%@%@%@%@", urlLanguage, API_PATH, path, (lastUpdate ? @"?last_modified=true" : @"")];
    
    TascaLog(@"api url: %@",finalUrlString);
    return finalUrlString;
    
}

+ (NSString *)getBaseURLString{
    return BASE_URL;
}

+ (NSArray *)extractURLStringFromDictionary:(id)item{
    /*
     public List<String> extractUrls(JSONObject json)
     {
     List<String> urls = new ArrayList<>();
     Iterator<String> keys = json.keys();
     while (keys.hasNext()) {
     String key = keys.next();
     if (json.optJSONArray(key) != null) {
     urls.addAll(extractUrls(json.optJSONArray(key)));
     } else if (json.optJSONObject(key) != null) {
     urls.addAll(extractUrls(json.optJSONObject(key)));
     } else {
     String testUrl = json.optString(key);
     if (URLUtil.isValidUrl(testUrl)) {
     urls.add(testUrl);
     }
     }
     }
     return urls;
     }

     */
    
    NSMutableArray *urlStrings = [NSMutableArray new];
    
    if ([item isKindOfClass:[NSArray class]]) {
        
        for (int i = 0; i < [(NSArray *)item count]; i++) {
            if ([item[i] isKindOfClass:[NSArray class]] || [item[i] isKindOfClass:[NSDictionary class]]) {
                [urlStrings addObjectsFromArray:[self extractURLStringFromDictionary:item[i]]];
            }else if([item[i] isKindOfClass:[NSString class]]){
                NSString *urlString = item[i];
                if([self validateUrl:urlString]){
                    [urlStrings addObject:urlString];
                }
            }
        }
        
    }else if([item isKindOfClass:[NSDictionary class]]){
       
        for (NSString *key in [(NSDictionary *)item allKeys]) {
            
            if ([item[key] isKindOfClass:[NSArray class]] || [item[key] isKindOfClass:[NSDictionary class]]) {
                
                [urlStrings addObjectsFromArray:[self extractURLStringFromDictionary:item[key]]];
           
            }else if([item[key] isKindOfClass:[NSString class]]){
            
                NSString *urlString = item[key];
               
                if([self validateImageUrl:urlString]){
                    [urlStrings addObject:urlString];
                }
            
            }
            
            
        }
    }
    
    
    return urlStrings;
}

+ (BOOL)validateImageUrl:(NSString *)urlString {

    return [urlString hasPrefix:@"http"] && [VALID_IMAGE_EXTENSIONS containsObject:urlString.pathExtension.lowercaseString];
    
}

+ (BOOL)validateUrl:(NSString *)urlString {
    
    return [urlString hasPrefix:@"http"];
    
    NSString *urlRegEx =
    @"(http|https)://((\\w)*|([0-9]*)|([-|_])*)+([\\.|/]((\\w)*|([0-9]*)|([-|_])*))+";
    NSPredicate *urlTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", urlRegEx];
    return [urlTest evaluateWithObject:urlString];
}

#pragma mark - Paths

+ (NSString *)getDatabaseName{
    return _databaseName;
}

+ (NSString *)getDownloadPath{
    return [NSString stringWithFormat:@"%@/Downloads",[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask, YES) objectAtIndex:0]];
}


#pragma mark - Utility

+ (NSString *)getLanguage{

    return [self getLanguage:nil];
}

+ (void)setLanguage:(NSString *)language{
 
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject:language forKey:@"defaultLanguage"];
    [defaults synchronize];
}

/**
 * return the request language if available, if not it will return the DEFAULT_LANGUAGE
 *
 * @return server request language string or DEFAULT_LANGUAGE
 */
+ (NSString *)getLanguage:(NSString *)requestedLanguage{
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    if (!requestedLanguage || requestedLanguage.length == 0) {
        
        requestedLanguage = [defaults stringForKey:@"defaultLanguage"];
        
    }
    
    if (!requestedLanguage || requestedLanguage.length == 0) {
        requestedLanguage = [[[NSBundle mainBundle] preferredLocalizations] objectAtIndex:0];

    }
    
    
    
    
    if (requestedLanguage && requestedLanguage.length > 0) {
        for (NSString *language in ACCEPTED_LANGUAGES) {
            
            if ([language isEqualToString:requestedLanguage]) {
                return language;
            }
        }
    }
    [defaults setObject:DEFAULT_LANGUAGE forKey:@"defaultLanguage"];
    [defaults synchronize];
    return DEFAULT_LANGUAGE;
}


+ (NSString *)jsonFromObject:(id)object{
    return [self jsonFromObject:object prettyPrinted:NO];
}
+ (NSString *)jsonFromObject:(id)object prettyPrinted:(BOOL)pretty{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:object
                                                       options:(pretty?NSJSONWritingPrettyPrinted:0) // Pass 0 if you don't care about the readability of the generated string
                                                         error:&error];
    
    NSString *jsonString = @"";
    if (! jsonData) {
        TascaLog(@"Got an error: %@", error);
    } else {
        jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    
    return jsonString;
}
+(id) jsonObjectFromString:(NSString*)json {
    NSError*error = nil;
    id jsonObject = [NSJSONSerialization JSONObjectWithData:[json dataUsingEncoding:NSUTF8StringEncoding]
                                                 options:0 error:&error];
    if(error==nil) {
        return jsonObject;
    }
    TascaLog(@"Got an error: %@", error);
    return nil;
}
+(BOOL)nsStringIsNull:(id)string {
    if (string == nil) return YES;
    if (!string || [string isKindOfClass:[NSNull class]]) return YES;
    if ([string isEqual:[NSNull null]]) return YES;
    if ([string isEqualToString:@""]) return YES;
    if([string isKindOfClass:[NSString class]]) {
        if([string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]].length==0) return YES;
    }
    return NO;
}
@end
