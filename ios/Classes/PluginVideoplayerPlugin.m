#import "PluginVideoplayerPlugin.h"
#if __has_include(<plugin_videoplayer/plugin_videoplayer-Swift.h>)
#import <plugin_videoplayer/plugin_videoplayer-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "plugin_videoplayer-Swift.h"
#endif

@implementation PluginVideoplayerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftPluginVideoplayerPlugin registerWithRegistrar:registrar];
}
@end
