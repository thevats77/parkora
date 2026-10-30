import SwiftUI
import GoogleSignIn
import Shared

/*
 * iPhone ENTRY POINT (Swift).
 * The whole app UI and logic are Kotlin (android/shared). Swift only:
 *   1. hands Kotlin the Google sign-in helper (GoogleSignInBridge.swift),
 *   2. shows Kotlin's MainViewController() full screen,
 *   3. passes the "come back from Google sign-in" link to GoogleSignIn.
 */
@main
struct iOSApp: App {
    init() {
        Platform.shared.googleSignIn = GoogleSignInBridge()
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                .onOpenURL { url in GIDSignIn.sharedInstance.handle(url) }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
