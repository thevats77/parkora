"use client";

import { MobileBar, ToastProvider, TopNav } from "@/components/layout";
import { ParkoraProvider } from "@/lib/store";

/** Client-side wrapper: data store, toasts, navigation bars. */
export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <ParkoraProvider>
      <ToastProvider>
        <TopNav />
        <main className="pb-28 md:pb-12">{children}</main>
        <MobileBar />
      </ToastProvider>
    </ParkoraProvider>
  );
}
