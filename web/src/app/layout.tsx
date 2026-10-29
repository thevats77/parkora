import type { Metadata, Viewport } from "next";
import { Providers } from "./providers";
import "./globals.css";

/*
 * ROOT LAYOUT – wraps every page.
 * It loads the global styles and puts every page inside <Providers>
 * (the data store + toast messages) with the top navigation and the
 * mobile bottom bar around it.
 */
export const metadata: Metadata = {
  title: { default: "Parkora – Smart Peer-to-Peer Parking", template: "%s · Parkora" },
  description: "Find, book and list parking spaces near you. PBL project – Smart Peer-to-Peer Parking & Booking.",
  icons: { icon: "/favicon.svg" },
};

export const viewport: Viewport = { themeColor: "#FED700" };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-dvh">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
