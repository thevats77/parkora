import UIKit
import GoogleSignIn
import Shared

/*
 * Google sign-in on iPhone, using Google's official GoogleSignIn SDK.
 * Kotlin (Platform.ios.kt) calls signIn(onResult:) when the user taps
 * "Continue with Google". We show Google's sign-in page and give Kotlin the
 * ID token, which Kotlin then uses to sign in to Firebase (same as on Android).
 */
class GoogleSignInBridge: IosGoogleSignIn {

    func signIn(onResult: @escaping (String?, String?, String?) -> Void) {
        guard let presenter = Self.topViewController() else {
            onResult(nil, nil, "Please try again.")
            return
        }
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as NSError? {
                let cancelled = error.code == GIDSignInError.canceled.rawValue
                onResult(nil, nil, cancelled ? "Sign-in was cancelled." : "Google sign-in failed: \(error.localizedDescription)")
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                onResult(nil, nil, "Google sign-in returned an unexpected result.")
                return
            }
            onResult(idToken, user.accessToken.tokenString, nil)
        }
    }

    func signOut() {
        GIDSignIn.sharedInstance.signOut()
    }

    /** The screen currently shown, so Google's page can open on top of it. */
    private static func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes.first { $0.activationState == .foregroundActive } as? UIWindowScene
        var top = scene?.windows.first { $0.isKeyWindow }?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
