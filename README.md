
# @wavyapp/react-native-sum-up

## iOS

Copy those lines in your Podfile, in the `target 'x' do` section

```
  post_install do |installer|
    sum_up_missing_header = Pod::Executable.execute_command(
      'node', [
        '-p',
        'require.resolve(
          "@wavyapp/react-native-sum-up/ios/ISHCashierWebService.h",
          {paths: [process.argv[1]]},
        )',
        __dir__
      ]).strip
    Pod::UI.info "Installing SumUpSDK missing header #{sum_up_missing_header}".green
    FileUtils.cp(sum_up_missing_header, "Pods/SumUpSDK/SumUpSDK.xcframework/ios-arm64/SumUpSDK.framework/Headers/")
	end
```

## Usage
```javascript
import {
  authenticate,
  checkout,
  isLoggedIn,
  logout,
  preferences,
  prepareForCheckout,
} from '@wavyapp/react-native-sum-up';
```

## Test transactions
You need to set up a SumUp test profile, make sure you enable the payment scopes under the section `Restricted scopes` in the OAuth2 page
Then generate an affiliate key, associate your app Bundle ID/package name to the affiliate key and give this key to the `authenticate` method