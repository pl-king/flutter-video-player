#import "FlutterpluginvideoplayerPlugin.h"
#if __has_include(<flutterpluginvideoplayer/flutterpluginvideoplayer-Swift.h>)
#import <flutterpluginvideoplayer/flutterpluginvideoplayer-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutterpluginvideoplayer-Swift.h"
#endif

@implementation FlutterpluginvideoplayerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterpluginvideoplayerPlugin registerWithRegistrar:registrar];
}
@end
