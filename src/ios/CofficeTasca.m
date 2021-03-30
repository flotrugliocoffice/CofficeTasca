/********* CofficeTasca.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <CoreLocation/CoreLocation.h>
#import "CofficeTascaInternal.h"
#import "CDVFile.h"

@interface CofficeTasca : CDVPlugin {
    // Member variables go here.
    CDVFile* _file;
}

@end

@implementation CofficeTasca
typedef enum {
    kArgumentsNotEnough

} ErrorTypes;

-(void)pluginInitialize {
    [super pluginInitialize];
    _file = [[CDVFile alloc] init];
    [_file pluginInitialize];
    [[CofficeTascaInternal sharedInstance] getWineByID:-1 forLanguage:@"it"]; //Fake load to force Database instance
}

- (void)getInfo:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)reverseGeocode:(CDVInvokedUrlCommand*)command {

    BOOL valid = NO;
    if( command.arguments.count>=2) {
        double latitude = [[command argumentAtIndex:0 withDefault:@(0)] doubleValue];
        double longitude =[[command argumentAtIndex:1 withDefault:@(0)] doubleValue];
        [self.commandDelegate runInBackground:^{


            CLLocation*location = [[CLLocation alloc] initWithLatitude:latitude longitude:longitude];
            CLGeocoder*geocoder = [[CLGeocoder alloc] init];
            [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray<CLPlacemark *> * _Nullable placemarks, NSError * _Nullable error) {
                if(error == nil) {
                    if(placemarks.count>0) {
                        CLPlacemark*placemark = [placemarks objectAtIndex:0];
                        NSDictionary*dict = @{
                                              @"countryCode": (placemark.ISOcountryCode) ? placemark.ISOcountryCode : @"",
                                              @"countryName": (placemark.country) ? placemark.country : @"",
                                              @"postalCode": placemark.postalCode ? placemark.postalCode: @"",
                                              @"administrativeArea": placemark.administrativeArea ? placemark.administrativeArea : @"",
                                              @"subAdministrativeArea": placemark.subAdministrativeArea ? placemark.subAdministrativeArea :  @"",
                                              @"locality": placemark.locality ? placemark.locality : @"",
                                              @"subLocality": placemark.subLocality ? placemark.subLocality : @"",
                                              @"thoroughfare": placemark.thoroughfare ? placemark.thoroughfare : @"",
                                              @"subThoroughfare": placemark.subThoroughfare ? placemark.subThoroughfare : @""
                                              };
                        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];
                        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                    }
                } else {
                    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsDictionary:@{}];
                    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

                }
            }];
        }];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Expected two non-empty double arguments."];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)requestPermissions :(CDVInvokedUrlCommand*) command {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ALL GRANTED"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

-(void) responseWithError:(CDVInvokedUrlCommand*)command andReason:(ErrorTypes)reason {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR  messageAsInt:reason];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
-(void) responseWithProgress:(CDVInvokedUrlCommand*)command andProgressOne:(float)progress andProgressIndex:(int)progressIndex {
    NSMutableDictionary*dicRespo = [NSMutableDictionary dictionaryWithDictionary:@{@"type":@"progress"}];
    if(progressIndex>0) {
        dicRespo[[NSString stringWithFormat:@"progress%d",progressIndex]] = [NSNumber numberWithFloat:progress];
    } else {
        dicRespo[@"progress"] = [NSNumber numberWithFloat:progress];
    }
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK  messageAsDictionary:dicRespo];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)getWineById:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:1 withDefault:@""];
    long _id = [[command argumentAtIndex:1 withDefault:@(0)] longValue];

    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil; //[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ALL GRANTED"];
        NSDictionary* dic = [[CofficeTascaInternal sharedInstance] getWineByID:_id forLanguage:_lang];
        if(dic!=nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR  messageAsDictionary:dic];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)getEstateById:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    long _id = [[command argumentAtIndex:1 withDefault:@(0)] longValue];
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil; //[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ALL
        NSDictionary* dic = [[CofficeTascaInternal sharedInstance] getEstateByID:_id forLanguage:_lang];
        if(dic!=nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR  messageAsDictionary:dic];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}

-(void)clearWines:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        int res = [[CofficeTascaInternal sharedInstance] clearWines];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)clearEstates:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        int res = [[CofficeTascaInternal sharedInstance] clearEstates];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}

-(void)clearTastes:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        int res = [[CofficeTascaInternal sharedInstance] clearTastes];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}

-(void)clearCache:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        int res = [[CofficeTascaInternal sharedInstance] clearCache];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}

-(void)clearAll:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        int res = [[CofficeTascaInternal sharedInstance] clearAll];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)downloadAll:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""] ;
    BOOL downloadImages = NO;
    if(command.arguments.count>1) {
        downloadImages = [[command argumentAtIndex:1 withDefault:@"0"] boolValue];
    }
    [self.commandDelegate runInBackground:^{
        [[CofficeTascaInternal sharedInstance] downloadAll:_lang downloadImages:downloadImages stepProgressBlock:^(float progress, int current, int total, int step) {
            [self responseWithProgress:command andProgressOne:step andProgressIndex:1];
        } currentProgressBlock:^(float progress, int current, int total,int step) {
            [self responseWithProgress:command andProgressOne:progress andProgressIndex:2];
        } finishBlock:^(id obj) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    }];
}

-(void)downloadAllMedia:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }

    NSArray*list = [[command argumentAtIndex:0 withDefault:@[]] array];
    [self.commandDelegate runInBackground:^{
        [[CofficeTascaInternal sharedInstance] downloadAllMedia:list currentProgressBlock:^(float progress, int current, int total,int step) {
            [self responseWithProgress:command andProgressOne:progress andProgressIndex:-1];
        } finishBlock:^(id obj) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    }];
}

-(void)getImageByUrl:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _string =[command argumentAtIndex:0 withDefault:@""];
    BOOL useCordovaPath = NO;
    BOOL overrideCache = NO;
    if(command.arguments.count>1) {
        useCordovaPath = [[command argumentAtIndex:1 withDefault:@"0"] boolValue];
    }
    if(command.arguments.count>2) {
        overrideCache = [[command argumentAtIndex:2 withDefault:@"0"] boolValue];
    }

    [self.commandDelegate runInBackground:^{
        NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:YES error:nil];
        NSString* res = [[CofficeTascaInternal sharedInstance] getImageByUrl:_string finishBlock:^(id obj) {
            if(obj == nil) {
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsBool:NO];
                [pluginResult setKeepCallbackAsBool:NO];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                return;
            }
            NSString *local = obj;
            if(useCordovaPath) {
                NSDictionary*fileD = [_file makeEntryForURL:[NSURL URLWithString:obj]];
                if(fileD) {
                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:fileD];
                    [pluginResult setKeepCallbackAsBool:NO];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                } else {
                    [_file getFile:nil];
                    NSString* tmpL = [local substringFromIndex:documentsDirectoryURL.absoluteString.length];
                    fileD = [_file makeEntryForPath:tmpL fileSystemName:@"root" isDirectory:NO];
                    if(fileD) {
                        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:fileD];
                        [pluginResult setKeepCallbackAsBool:NO];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }
                }
            }
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:local];
            [pluginResult setKeepCallbackAsBool:NO];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:res];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)downloadWines:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    [self.commandDelegate runInBackground:^{
        [[CofficeTascaInternal sharedInstance] downloadWines:_lang downloadImages:NO currentProgressBlock:^(float progress, int current, int total,int step) {
            [self responseWithProgress:command andProgressOne:progress andProgressIndex:-1];
        } finishBlock:^(id obj) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        }];
    }];


}

-(void)getWinesTypeAhead:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    NSString* _term =[command argumentAtIndex:1 withDefault:@""];
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getWinesTypeAhead:_term forLanguage:_lang];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)getWines:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    NSString* _term = @"";
    int _limit = -1;
    int _offset = -1;
    NSString* _sort = @"";
    BOOL _sortDir = NO;

    if(command.arguments.count>=2) {
        _term  = [command argumentAtIndex:1 withDefault:@""];
    }
    if(command.arguments.count>=3) {
        _limit  = [[command argumentAtIndex:2 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=4) {
        _offset  = [[command argumentAtIndex:3 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=5) {
        _sort  = [command argumentAtIndex:4 withDefault:@""];
    }
    if(command.arguments.count>=6) {
        _sortDir  = [[command argumentAtIndex:5 withDefault:@(NO)] boolValue];
    }
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getWines:_term forLanguage:_lang limit:_limit offset:_offset sort:_sort sortDirection:_sortDir];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)downloadEstates:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    [self.commandDelegate runInBackground:^{
        [[CofficeTascaInternal sharedInstance] downloadEstates:_lang downloadImages:NO currentProgressBlock:^(float progress, int current, int total,int step) {
            [self responseWithProgress:command andProgressOne:progress andProgressIndex:-1];
        } finishBlock:^(id obj) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        }];
    }];

}

-(void)getEstatesTypeAhead:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    NSString* _term =[command argumentAtIndex:1 withDefault:@""];
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getEstatesTypeAhead:_term forLanguage:_lang];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)getEstates:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:0 withDefault:@""];
    NSString* _term = @"";
    int _limit = -1;
    int _offset = -1;
    NSString* _sort = @"";
    BOOL _sortDir = NO;

    if(command.arguments.count>=2) {
        _term  = [command argumentAtIndex:1 withDefault:@""];
    }
    if(command.arguments.count>=3) {
        _limit  = [[command argumentAtIndex:2 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=4) {
        _offset  = [[command argumentAtIndex:3 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=5) {
        _sort  = [command argumentAtIndex:4 withDefault:@""];
    }
    if(command.arguments.count>=6) {
        _sortDir  = [[command argumentAtIndex:5 withDefault:@(NO)] boolValue];
    }
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getEstates:_term forLanguage:_lang limit:_limit offset:_offset sort:_sort sortDirection:_sortDir];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)getTasteById:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _lang =[command argumentAtIndex:1 withDefault:@""];
    long _id = [[command argumentAtIndex:1 withDefault:@(0)] longValue];

    //[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ALL GRANTED"];
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        NSDictionary* dic = [[CofficeTascaInternal sharedInstance] getTasteByID:_id forLanguage:_lang];
        if(dic!=nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR  messageAsDictionary:dic];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}


-(void)insertTaste:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSDictionary* tasteObj = [command argumentAtIndex:0 withDefault:@{}];
    [self.commandDelegate runInBackground:^{
        long res = [[CofficeTascaInternal sharedInstance] insertTaste:tasteObj];
        CDVPluginResult* pluginResult = nil;
        if(res>0) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:(int)res];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:(int)res];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)editTaste:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<2) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    long _id = [[command argumentAtIndex:0 withDefault:@(0)] longValue];
    NSDictionary* tasteObj = [command argumentAtIndex:1 withDefault:@{}];
    [self.commandDelegate runInBackground:^{
        BOOL res = [[CofficeTascaInternal sharedInstance] editTaste:_id data:tasteObj];
        CDVPluginResult* pluginResult = nil;
        if(res) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:res];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsBool:res];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

}
-(void)deleteTaste:(CDVInvokedUrlCommand*)command {
    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    long _id = [[command argumentAtIndex:0 withDefault:@(0)] longValue];
    [self.commandDelegate runInBackground:^{
        BOOL res = [[CofficeTascaInternal sharedInstance] deleteTaste:_id];
        CDVPluginResult* pluginResult = nil;
        if(res) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:res];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsBool:res];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];


}
-(void)getTastesTypeAhead:(CDVInvokedUrlCommand*)command {

    if( command.arguments.count<1) {
        //Error
        [self responseWithError:command andReason:kArgumentsNotEnough];
        return;
    }
    NSString* _term =[command argumentAtIndex:0 withDefault:@""];
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getTastesTypeAhead:_term];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}
-(void)getTastes:(CDVInvokedUrlCommand*)command {

    NSString* _term = @"";
    int _limit = -1;
    int _offset = -1;
    NSString* _sort = @"";
    BOOL _sortDir = NO;
    if(command.arguments.count>=1) {
        _term  = [command argumentAtIndex:0 withDefault:@""];
    }
    if(command.arguments.count>=2) {
        _limit  = [[command argumentAtIndex:1 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=3) {
        _offset  = [[command argumentAtIndex:2 withDefault:@(-1)] intValue];
    }
    if(command.arguments.count>=4) {
        _sort  = [command argumentAtIndex:3 withDefault:@""];
    }
    if(command.arguments.count>=5) {
        _sortDir  = [[command argumentAtIndex:4 withDefault:@(NO)] boolValue];
    }
    [self.commandDelegate runInBackground:^{
        NSArray* res = [[CofficeTascaInternal sharedInstance] getTastes:_term limit:_limit offset:_offset sort:_sort sortDirection:_sortDir];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:res];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)getImageMap:(CDVInvokedUrlCommand*)command {

    BOOL useCordovaPath = NO;
    if(command.arguments.count>0) {
        useCordovaPath = [[command argumentAtIndex:0 withDefault:@"0"] boolValue];
    }


    [self.commandDelegate runInBackground:^{
        NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:YES error:nil];
        NSArray* res = [[CofficeTascaInternal sharedInstance] getMediaMap];
        NSMutableDictionary*res2 = [NSMutableDictionary new];
        [res enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            id local = @"";
            if(obj[@"local"] && [TascaCommon nsStringIsNull:obj[@"local"]]==NO) {
                if(useCordovaPath == YES) {
                    NSDictionary*fileD = [_file makeEntryForURL:[NSURL URLWithString:obj[@"local"]]];
                    if(fileD) {
                        local = fileD;
                    } else {
                        NSString* tmpL = [obj[@"local"] substringFromIndex:documentsDirectoryURL.absoluteString.length];
                        fileD = [_file makeEntryForPath:tmpL fileSystemName:@"root" isDirectory:NO];
                        if(fileD) {
                            local = fileD;
                        }
                    }
                } else {
                    local =obj[@"local"];
                }
            }
            res2[obj[@"url"]] = local;
        }];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:res2];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}


@end
