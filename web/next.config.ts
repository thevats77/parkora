import type { NextConfig } from "next";

/*
 * output: "export" turns the site into plain HTML/CSS/JS files in the `out` folder
 * when you run `npm run build`. That folder can be hosted on any static host
 * (Vercel, Netlify, GitHub Pages) – no server is needed because all data is
 * kept in the browser (see src/lib/store.tsx).
 */
const nextConfig: NextConfig = {
  output: "export",
  images: { unoptimized: true }, // required for static export
  trailingSlash: true,           // /parking/ -> /parking/index.html (works on every static host)
};

export default nextConfig;
