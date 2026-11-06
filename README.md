# Prank Text Changer

An Android proof-of-concept that displays replacement text as a visual overlay over matching visible text in other apps.

## Build
GitHub Actions builds a debug APK on every push to main. Open the Actions tab and download the `PrankTextChanger-debug-apk` artifact from a successful run.

## Use
1. Install the APK.
2. Open the app and add an original phrase and its replacement.
3. Open Accessibility settings and enable Prank Text Changer.
4. Test in an app that exposes readable accessibility text.
5. Disable the service in Accessibility settings to stop overlays.

## Limitations and privacy
This is a visual overlay, not a modification of other apps' stored data. It cannot reliably change text rendered into images/canvas, protected screens, or apps that hide accessibility text. Overlay placement can vary with scrolling and dynamic layouts. The service reads visible accessibility text locally to apply user-defined rules; it does not transmit data.
