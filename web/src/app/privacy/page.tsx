import { PageHeader } from "@/components/layout";
import { CreamCard } from "@/components/ui";

export const metadata = { title: "Privacy Policy" };

const sections: [string, string][] = [
  ["What we store", "Your Google name, email and profile photo, the phone number and location you add, the parking spots you list, your bookings and in-app notifications."],
  ["Where it is stored", "In Google Firebase (Cloud Firestore), Parkora's online database. The website and the Parkora Android app use the same data."],
  ["Sign-in", "You sign in with Google. Parkora never sees or stores your Google password."],
  ["Photos and documents", "Parking photos are compressed and saved with your listing. For ownership documents only the file name is recorded."],
  ["Who can see your data", "Everyone can see parking listings. Spot owners see the name on bookings for their spots. Your phone number is not shown to other users. The Parkora admin can see all accounts, listings and bookings to keep the platform safe."],
  ["Deleting your data", "Contact the Parkora team and the admin will delete your account data."],
  ["Photo credits", "Photos of the MUJ demo listings come from Wikimedia Commons: Biswarup Ganguly (CC BY 3.0); Sranjanm2002 and LRBurdak (CC BY-SA 3.0); Tarunsamanta and Fredericknoronha (CC BY-SA 4.0). They illustrate the area and typical parking, not the exact spots."],
];

export default function PrivacyPage() {
  return (
    <>
      <PageHeader title="Privacy Policy" back />
      <div className="mx-auto max-w-2xl space-y-3.5 px-4 py-8">
        {sections.map(([title, body]) => (
          <CreamCard key={title} className="p-5">
            <h2 className="font-semibold">{title}</h2>
            <p className="mt-1 text-sm leading-6 text-muted">{body}</p>
          </CreamCard>
        ))}
      </div>
    </>
  );
}
