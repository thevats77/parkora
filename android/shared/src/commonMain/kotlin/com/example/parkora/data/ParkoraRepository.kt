package com.example.parkora.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.parkora.platform.GoogleSignInException
import com.example.parkora.platform.Platform
import com.example.parkora.utils.BookingCalculator
import com.example.parkora.utils.nowMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/*
 * PARKORA REPOSITORY  (the "data layer" of the app)
 * --------------------------------------------------
 * This single object is the ONLY place that reads or changes app data.
 * Screens call its functions, e.g. ParkoraRepository.createBooking(spot, from, to).
 *
 * WHERE DATA LIVES (live, online):
 *   - Firebase Authentication: users sign in with their Google account.
 *   - Cloud Firestore: collections users, parking_spots, bookings, notifications.
 *     The website (parkora-tan.vercel.app) uses the SAME database, so a spot listed
 *     on the phone appears on the website instantly and vice-versa.
 *
 * HOW SCREENS GET UPDATES:
 *   We register "snapshot listeners". Every time data changes in Firestore (by anyone),
 *   the listener receives the new documents and we put them into Compose state lists
 *   (mutableStateListOf). Screens that read these lists redraw automatically.
 *
 * ONE CODE FOR ANDROID AND IPHONE: this file is in the shared module and uses the
 * "GitLive" Firebase library, which calls the official Firebase SDK of each platform.
 *
 * FUNCTIONS THAT CHANGE DATA are "suspend" functions (they wait for the internet).
 * Screens call them inside   scope.launch { ... }   and each one returns an error
 * message String, or null when it worked.
 *
 * SECURITY: the rules in firebase/firestore.rules run on Google's servers, so even a
 * modified app cannot e.g. approve someone else's booking or open the admin functions.
 */
object ParkoraRepository {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    /** Running live-update jobs (one per Firestore query); cancelled on logout. */
    private val listeners = mutableListOf<Job>()

    // Firestore collection names (same on the website)
    private const val USERS = "users"
    private const val SPOTS = "parking_spots"
    private const val BOOKINGS = "bookings"
    private const val NOTIFICATIONS = "notifications"

    // ---------- Live app data (observable by Compose) ----------
    val parkingSpots = mutableStateListOf<ParkingSpot>()
    /** My bookings + bookings for my spots. For the admin: ALL bookings. */
    val bookings = mutableStateListOf<Booking>()
    val notifications = mutableStateListOf<AppNotification>()
    /** Only filled for the admin (Admin Portal → Users). */
    val allUsers = mutableStateListOf<User>()

    /** The signed-in user, or null. */
    var currentUser by mutableStateOf<User?>(null)
        private set

    /** Shown on the Login screen if signing in worked but loading the profile failed. */
    var loginError by mutableStateOf<String?>(null)
        private set

    /** True while the app is still finding out whether someone is signed in (shows a loader). */
    var isCheckingLogin by mutableStateOf(true)
        private set

    /** True until the first list of parking spots has arrived from the server. */
    var isLoadingSpots by mutableStateOf(true)
        private set

    /** False while FirebaseConfig.kt still contains the placeholder values. */
    val isConfigured: Boolean get() = !FirebaseConfig.API_KEY.startsWith("PASTE")

    // =====================================================================
    // Start-up
    // =====================================================================

    /** Called once at start-up (MainActivity on Android, MainViewController on iPhone). */
    fun init() {
        if (::db.isInitialized) return
        if (!isConfigured) {
            isCheckingLogin = false
            return
        }
        val options = FirebaseOptions(
            applicationId = Platform.firebaseAppId,   // Android and iOS apps have different ids
            apiKey = Platform.firebaseApiKey,
            projectId = FirebaseConfig.PROJECT_ID,
            gcmSenderId = FirebaseConfig.SENDER_ID
        )
        val app = Firebase.initialize(Platform.appContext, options)
        auth = Firebase.auth(app)
        db = Firebase.firestore(app)

        // Runs now (with the saved login, if any) and every time someone signs in or out
        scope.launch {
            auth.authStateChanged.collect { onLoginChanged(it) }
        }
    }

    private suspend fun onLoginChanged(firebaseUser: FirebaseUser?) {
        stopListeners()
        if (firebaseUser == null) {
            currentUser = null
            isCheckingLogin = false
            return
        }
        try {
            loginError = null
            currentUser = loadOrCreateUser(firebaseUser)
            startListeners(currentUser!!)
        } catch (e: Exception) {
            println("Parkora: could not load the user profile: $e")
            loginError = "Signed in, but your profile could not be loaded: ${e.message}"
            currentUser = null
            auth.signOut()
        }
        isCheckingLogin = false
    }

    /** First sign-in creates the users/{uid} document from the Google profile. */
    private suspend fun loadOrCreateUser(firebaseUser: FirebaseUser): User {
        val ref = db.collection(USERS).document(firebaseUser.uid)
        val snapshot = ref.get()
        if (snapshot.exists) return snapshot.toUser()

        val email = firebaseUser.email ?: ""
        val newUser = User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: email.substringBefore("@"),
            email = email,
            phone = "",
            location = "Dehmi Kalan, Jaipur",
            createdAt = nowMillis(),
            profileImagePath = firebaseUser.photoURL,
            role = if (email.equals(FirebaseConfig.ADMIN_EMAIL, ignoreCase = true)) User.ROLE_ADMIN else User.ROLE_USER
        )
        ref.set(newUser.toMap())
        return newUser
    }

    /** Starts live updates from Firestore for the signed-in user. */
    private fun startListeners(user: User) {
        // My own user document (so a block by the admin or a profile change shows at once)
        listen(db.collection(USERS).document(user.id).snapshots) { doc ->
            if (doc.exists) currentUser = doc.toUser()
        }

        // All parking spots (everyone may see every listing)
        listen(db.collection(SPOTS).snapshots) { snap ->
            parkingSpots.replaceWith(snap.documents.map { it.toParkingSpot() })
            isLoadingSpots = false
        }

        if (user.isAdmin) {
            // The admin sees every booking and every user
            listen(db.collection(BOOKINGS).snapshots) { snap ->
                bookings.replaceWith(snap.documents.map { it.toBooking() })
                updateFinishedBookings()
            }
            listen(db.collection(USERS).snapshots) { snap ->
                allUsers.replaceWith(snap.documents.map { it.toUser() }.sortedByDescending { it.createdAt })
            }
        } else {
            // Normal users: bookings I made + bookings for my spots (two queries, merged)
            var mine = emptyList<Booking>()
            var forMySpots = emptyList<Booking>()
            fun merge() {
                bookings.replaceWith((mine + forMySpots).distinctBy { it.id })
                scope.launch { updateFinishedBookings() }
            }
            listen(db.collection(BOOKINGS).where { "userId" equalTo user.id }.snapshots) { snap ->
                mine = snap.documents.map { it.toBooking() }; merge()
            }
            listen(db.collection(BOOKINGS).where { "ownerId" equalTo user.id }.snapshots) { snap ->
                forMySpots = snap.documents.map { it.toBooking() }; merge()
            }
        }

        listen(db.collection(NOTIFICATIONS).where { "userId" equalTo user.id }.snapshots) { snap ->
            notifications.replaceWith(snap.documents.map { it.toNotification() })
        }
    }

    /** Collects a live Firestore stream until logout (errors such as "no permission" are logged). */
    private fun <T> listen(updates: Flow<T>, onEach: suspend (T) -> Unit) {
        listeners += scope.launch {
            updates.catch { println("Parkora: live update failed: $it") }.collect { onEach(it) }
        }
    }

    private fun stopListeners() {
        listeners.forEach { it.cancel() }
        listeners.clear()
        parkingSpots.clear(); bookings.clear(); notifications.clear(); allUsers.clear()
        isLoadingSpots = true
    }

    private fun <T> MutableList<T>.replaceWith(items: List<T>) {
        clear(); addAll(items)
    }

    // =====================================================================
    // Error handling
    // =====================================================================

    /** Runs a database action and turns any failure into a friendly message (null = success). */
    private suspend fun attempt(action: suspend () -> Unit): String? {
        if (!isConfigured) return "Firebase is not set up yet (see FirebaseConfig.kt)."
        return try {
            withTimeout(15_000) { action() }
            null
        } catch (e: TimeoutCancellationException) {
            "Could not reach the server. Please check your internet connection."
        } catch (e: BookingClashException) {
            "This spot is already booked for part of that time. Please choose another time."
        } catch (e: FirebaseFirestoreException) {
            when {
                e.cause is BookingClashException -> "This spot is already booked for part of that time. Please choose another time."
                e.code == FirestoreExceptionCode.PERMISSION_DENIED -> "You are not allowed to do that."
                e.code == FirestoreExceptionCode.UNAVAILABLE -> "No internet connection. Please try again."
                else -> "Something went wrong: ${e.message}"
            }
        } catch (e: Exception) {
            if (generateSequence<Throwable>(e) { it.cause }.any { it is BookingClashException })
                "This spot is already booked for part of that time. Please choose another time."
            else e.message ?: "Something went wrong. Please try again."
        }
    }

    /** A new random document id (like Firestore's automatic ids). */
    private fun newId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars.random() }.joinToString("")
    }

    private class BookingClashException : Exception("Booking time clash")

    private fun newNotification(userId: String, title: String, message: String): Pair<String, Map<String, Any?>> {
        val ref = db.collection(NOTIFICATIONS).document(newId())
        return ref.id to AppNotification(ref.id, userId, title, message, false, nowMillis()).toMap()
    }

    private fun blockedMessage(): String? =
        if (currentUser?.isBlocked == true) "Your account has been blocked by the admin." else null

    // =====================================================================
    // Sign in with Google
    // =====================================================================

    /**
     * Shows the "Sign in with Google" screen of the phone (Android: account sheet,
     * iPhone: Google's sign-in page), then signs in to Firebase with that Google account.
     */
    suspend fun signInWithGoogle(): String? {
        if (!isConfigured) return "Firebase is not set up yet (see FirebaseConfig.kt)."
        return try {
            val tokens = Platform.signInWithGoogle()
            auth.signInWithCredential(GoogleAuthProvider.credential(tokens.idToken, tokens.accessToken))
            null // the auth listener above now loads the user and starts live updates
        } catch (e: GoogleSignInException) {
            e.message
        } catch (e: Exception) {
            "Google sign-in failed: ${e.message}"
        }
    }

    suspend fun logout() {
        if (!isConfigured) return
        auth.signOut()
        // Forget the chosen account so the account picker shows again next time
        runCatching { Platform.clearGoogleSignIn() }
    }

    // =====================================================================
    // Profile
    // =====================================================================

    suspend fun updateProfile(name: String, phone: String, location: String): String? = attempt {
        val user = currentUser ?: error("Please sign in first")
        db.collection(USERS).document(user.id)
            .update("name" to name.trim(), "phone" to phone.trim(), "location" to location.trim())
    }

    suspend fun updateLocation(location: String): String? = attempt {
        val user = currentUser ?: error("Please sign in first")
        db.collection(USERS).document(user.id).update("location" to location.trim())
    }

    /** [imageData] is a small JPEG as "data:image/jpeg;base64,..." (see ImageUtils.kt). */
    suspend fun updateProfilePhoto(imageData: String): String? = attempt {
        val user = currentUser ?: error("Please sign in first")
        db.collection(USERS).document(user.id).update("profileImagePath" to imageData)
    }

    // =====================================================================
    // Parking spots
    // =====================================================================

    /** Spots shown on the Home screen: available spots, newest first. */
    fun latestSpots(): List<ParkingSpot> =
        parkingSpots.filter { it.isAvailable }.sortedByDescending { it.createdAt }

    fun getSpot(spotId: String): ParkingSpot? = parkingSpots.find { it.id == spotId }

    fun mySpots(): List<ParkingSpot> =
        parkingSpots.filter { it.ownerId == currentUser?.id }.sortedByDescending { it.createdAt }

    /** Saves a new spot from the 4-step listing form. */
    suspend fun addParkingSpot(spot: ParkingSpot): String? {
        blockedMessage()?.let { return it }
        return attempt {
            val user = currentUser ?: error("Please sign in first")
            val ref = db.collection(SPOTS).document(newId())
            val full = spot.copy(
                id = ref.id, ownerId = user.id, ownerName = user.name,
                isVerified = false, createdAt = nowMillis(), bookedSlots = emptyList()
            )
            ref.set(full.toMap())
        }
    }

    suspend fun setSpotAvailability(spotId: String, available: Boolean): String? = attempt {
        db.collection(SPOTS).document(spotId).update("isAvailable" to available)
    }

    /** Deletes a spot, but only if nobody has an upcoming booking for it. */
    suspend fun deleteSpot(spotId: String): String? {
        val now = nowMillis()
        val hasActive = getSpot(spotId)?.bookedSlots?.any { it.endMillis > now } == true ||
            bookings.any { it.spotId == spotId && isOngoing(it) }
        if (hasActive) return "This spot has active bookings. Turn it off instead, or wait until they finish."
        return attempt { db.collection(SPOTS).document(spotId).delete() }
    }

    // =====================================================================
    // Bookings
    // =====================================================================

    fun isOngoing(booking: Booking): Boolean =
        booking.status == BookingStatus.PENDING || booking.status == BookingStatus.APPROVED

    /** Bookings made BY the signed-in user. */
    fun myBookings(): List<Booking> =
        bookings.filter { it.userId == currentUser?.id }.sortedBy { it.checkInMillis }

    /** Bookings made by OTHER people for the signed-in user's spots. */
    fun bookingRequestsForMe(): List<Booking> =
        bookings.filter { it.ownerId == currentUser?.id }.sortedByDescending { it.createdAt }

    /**
     * Creates a PENDING booking.
     * The double-booking check and the save happen inside ONE Firestore transaction:
     * Firestore re-reads the spot, checks its bookedSlots, and only then saves. If two
     * people book the same time at the same moment, only one of them succeeds.
     */
    suspend fun createBooking(spot: ParkingSpot, checkInMillis: Long, checkOutMillis: Long): String? {
        val user = currentUser ?: return "Please sign in first"
        blockedMessage()?.let { return it }
        if (spot.ownerId == user.id) return "You cannot book your own parking spot"
        if (!spot.isAvailable || !spot.availableForRent) return "This spot is not available for booking right now"
        if (checkInMillis < nowMillis() - 60_000) return "Check-in time cannot be in the past"
        if (checkOutMillis <= checkInMillis) return "Check-out must be after check-in"
        val summary = BookingCalculator.calculate(checkInMillis, checkOutMillis, spot.pricePerHour, spot.pricePerDay)
            ?: return "Please choose a valid check-in and check-out time"
        if (summary.totalHours > 30 * 24) return "A booking can be at most 30 days long"

        return attempt {
            val spotRef = db.collection(SPOTS).document(spot.id)
            val bookingRef = db.collection(BOOKINGS).document(newId())
            val booking = Booking(
                id = bookingRef.id, spotId = spot.id, parkingNumber = spot.parkingNumber,
                spotImage = spot.imagePaths.firstOrNull() ?: "demo_parking_1",
                userId = user.id, userName = user.name, ownerId = spot.ownerId,
                checkInMillis = checkInMillis, checkOutMillis = checkOutMillis,
                billedUnits = summary.billedUnits, billedUnitName = summary.unitName,
                unitPrice = summary.unitPrice, totalPrice = summary.totalPrice,
                status = BookingStatus.PENDING, createdAt = nowMillis()
            )
            val (notificationId, notification) = newNotification(
                spot.ownerId, "New booking request", "${user.name} wants to book your spot ${spot.parkingNumber}."
            )
            db.runTransaction {
                val fresh = get(spotRef).toParkingSpot()
                val clash = fresh.bookedSlots.any { checkInMillis < it.endMillis && checkOutMillis > it.startMillis }
                if (clash) throw BookingClashException()
                val slots = fresh.bookedSlots + BookedSlot(booking.id, checkInMillis, checkOutMillis)
                update(spotRef, "bookedSlots" to slots.map { it.toMap() })
                set(bookingRef, booking.toMap())
                set(db.collection(NOTIFICATIONS).document(notificationId), notification)
            }
        }
    }

    /** The slot of a booking, exactly as it is stored inside the spot (used to remove it). */
    private fun slotOf(b: Booking) = BookedSlot(b.id, b.checkInMillis, b.checkOutMillis).toMap()

    /** A user can cancel a pending/approved booking before its check-in time. */
    fun canCancel(booking: Booking): Boolean =
        isOngoing(booking) && booking.checkInMillis > nowMillis()

    suspend fun cancelBooking(bookingId: String): String? {
        val booking = bookings.find { it.id == bookingId } ?: return "Booking not found"
        if (!canCancel(booking)) return "This booking can no longer be cancelled"
        return attempt {
            val (nid, notification) = newNotification(booking.ownerId, "Booking cancelled",
                "${booking.userName} cancelled the booking for ${booking.parkingNumber}.")
            db.batch()
                .update(db.collection(BOOKINGS).document(booking.id), "status" to BookingStatus.CANCELLED.name)
                .update(db.collection(SPOTS).document(booking.spotId), "bookedSlots" to FieldValue.arrayRemove(slotOf(booking)))
                .set(db.collection(NOTIFICATIONS).document(nid), notification)
                .commit()
        }
    }

    /** Owner approves or rejects a pending booking request. */
    suspend fun respondToBooking(bookingId: String, approve: Boolean): String? {
        val booking = bookings.find { it.id == bookingId } ?: return "Booking not found"
        if (booking.status != BookingStatus.PENDING) return "This request has already been answered"
        val newStatus = if (approve) BookingStatus.APPROVED else BookingStatus.REJECTED
        val word = newStatus.label.lowercase()
        return attempt {
            val (nid, notification) = newNotification(booking.userId, "Booking $word",
                "Your booking for ${booking.parkingNumber} has been $word by the owner.")
            val batch = db.batch()
                .update(db.collection(BOOKINGS).document(booking.id), "status" to newStatus.name)
                .set(db.collection(NOTIFICATIONS).document(nid), notification)
            if (!approve) {
                batch.update(db.collection(SPOTS).document(booking.spotId), "bookedSlots" to FieldValue.arrayRemove(slotOf(booking)))
            }
            batch.commit()
        }
    }

    /**
     * Moves bookings whose check-out time has passed into "Past":
     * APPROVED -> COMPLETED, PENDING -> EXPIRED. Only bookings this user may change.
     */
    private suspend fun updateFinishedBookings() {
        val me = currentUser ?: return
        val now = nowMillis()
        val finished = bookings.filter {
            it.checkOutMillis < now && isOngoing(it) && (it.userId == me.id || it.ownerId == me.id || me.isAdmin)
        }
        if (finished.isEmpty()) return
        attempt {
            val batch = db.batch()
            finished.forEach {
                val status = if (it.status == BookingStatus.APPROVED) BookingStatus.COMPLETED else BookingStatus.EXPIRED
                batch.update(db.collection(BOOKINGS).document(it.id), "status" to status.name)
            }
            batch.commit()
        }
    }

    // =====================================================================
    // Notifications (in-app messages stored in Firestore)
    // =====================================================================

    fun myNotifications(): List<AppNotification> = notifications.sortedByDescending { it.createdAt }

    fun hasUnreadNotifications(): Boolean = notifications.any { !it.isRead }

    fun markMyNotificationsRead() {
        val unread = notifications.filter { !it.isRead }
        if (unread.isEmpty()) return
        scope.launch {
            attempt {
                val batch = db.batch()
                unread.forEach { batch.update(db.collection(NOTIFICATIONS).document(it.id), "isRead" to true) }
                batch.commit()
            }
        }
    }

    // =====================================================================
    // Admin Portal (the security rules allow these only for the admin account)
    // =====================================================================

    suspend fun adminSetSpotVerified(spotId: String, verified: Boolean): String? = attempt {
        db.collection(SPOTS).document(spotId).update("isVerified" to verified)
    }

    /** Removes a listing and cancels its upcoming bookings (the renters are notified). */
    suspend fun adminDeleteSpot(spotId: String): String? = attempt {
        val spot = getSpot(spotId) ?: error("Spot not found")
        val batch = db.batch().delete(db.collection(SPOTS).document(spotId))
        bookings.filter { it.spotId == spotId && isOngoing(it) }.forEach { b ->
            batch.update(db.collection(BOOKINGS).document(b.id), "status" to BookingStatus.CANCELLED.name)
            val (nid, n) = newNotification(b.userId, "Booking cancelled",
                "The listing ${spot.parkingNumber} was removed by the Parkora admin, so your booking was cancelled.")
            batch.set(db.collection(NOTIFICATIONS).document(nid), n)
        }
        batch.commit()
    }

    suspend fun adminCancelBooking(bookingId: String): String? = attempt {
        val b = bookings.find { it.id == bookingId } ?: error("Booking not found")
        val batch = db.batch()
            .update(db.collection(BOOKINGS).document(b.id), "status" to BookingStatus.CANCELLED.name)
            .update(db.collection(SPOTS).document(b.spotId), "bookedSlots" to FieldValue.arrayRemove(slotOf(b)))
        listOf(b.userId, b.ownerId).distinct().forEach { uid ->
            val (nid, n) = newNotification(uid, "Booking cancelled by admin",
                "The booking for ${b.parkingNumber} (${b.userName}) was cancelled by the Parkora admin.")
            batch.set(db.collection(NOTIFICATIONS).document(nid), n)
        }
        batch.commit()
    }

    suspend fun adminSetUserBlocked(userId: String, blocked: Boolean): String? = attempt {
        db.collection(USERS).document(userId).update("isBlocked" to blocked)
    }

    /** Adds (or refreshes) the 4 demo listings near Manipal University Jaipur. */
    suspend fun adminAddMujDemoSpots(): String? = attempt {
        val admin = currentUser ?: error("Please sign in first")
        val batch = db.batch()
        MujDemoSpots.spots(admin.id).forEach { spot ->
            // merge = keep any existing bookedSlots instead of wiping them
            batch.set(db.collection(SPOTS).document(spot.id), spot.toMap() - "bookedSlots", merge = true)
        }
        batch.commit()
    }
}
