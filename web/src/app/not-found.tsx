import Link from "next/link";

export default function NotFound() {
  return (
    <div className="mx-auto max-w-md px-6 py-24 text-center">
      <p className="text-6xl font-bold">404</p>
      <p className="mt-2 text-lg">This page could not be found.</p>
      <Link href="/" className="mt-6 inline-flex h-11 items-center rounded-xl px-6 font-bold btn-yellow">Go home</Link>
    </div>
  );
}
