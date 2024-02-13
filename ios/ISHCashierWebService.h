//
//  ISHCashierWebService.h
//  ReactNativeSumUp
//
//  Created by Sébastien Vray on 31/05/2024.
//

#ifndef ISHCashierWebService_h
#define ISHCashierWebService_h

@interface ISHCashierWebService : NSObject
+(ISHCashierWebService *) defaultService;
-(NSString *) accessToken;
@end

#endif /* ISHCashierWebService_h */
