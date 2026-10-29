import type { ParkingSpot } from "./types";

/*
 * MUJ DEMO SPOTS – four sample listings around Manipal University Jaipur.
 * Same list as the Android app (android/.../data/MujDemoSpots.kt).
 * The admin adds them once with "Add MUJ demo spots" in the Admin Portal; fixed ids
 * mean pressing it again only refreshes them.
 */
export function mujDemoSpots(adminId: string): Omit<ParkingSpot, "bookedSlots">[] {
  const now = Date.now();
  const spot = (
    id: string, parkingNumber: string, societyName: string, address: string, parkingType: string,
    lengthFeet: number, widthFeet: number, vehicleTypes: string[], imagePaths: string[],
    pricePerHour: number, pricePerDay: number, rating: number, ageMinutes: number
  ): Omit<ParkingSpot, "bookedSlots"> => ({
    id, ownerId: adminId, ownerName: "Parkora (demo listing)",
    parkingNumber, societyName, address, city: "Jaipur",
    parkingType, lengthFeet, widthFeet, vehicleTypes,
    imagePaths, documentNames: [], documentPaths: [],
    pricePerHour, pricePerDay, availableForRent: true, availableForSale: false,
    isAvailable: true, isVerified: true, rating, createdAt: now - ageMinutes * 60_000,
  });

  return [
    spot("muj_demo_1", "DK-12", "Dehmi Kalan Village Parking", "Near MUJ Main Gate, Dehmi Kalan",
      "Open", 18, 9, ["SUV", "Sedan", "Regular", "Two Wheeler"], ["demo_muj_dk12"], 20, 150, 4, 1),
    spot("muj_demo_2", "AE-04", "Ajmer Road Student Residency", "Jaipur–Ajmer Expressway (NH-48), 1 km from MUJ",
      "Open", 17, 8, ["SUV", "Sedan", "Regular", "Two Wheeler"], ["demo_muj_ae04", "demo_muj_ae04_road"], 30, 220, 5, 2),
    spot("muj_demo_3", "BG-21", "Bagru Heights Apartments", "Basement B1, Bagru Road, 3 km from MUJ",
      "Basement", 16, 8, ["Sedan", "Regular", "Two Wheeler"], ["demo_muj_bg21"], 25, 180, 4, 3),
    spot("muj_demo_4", "TW-07", "Dehmi Kalan Market Stand", "Market Road, Dehmi Kalan (opp. MUJ hostels)",
      "Open", 8, 4, ["Two Wheeler"], ["demo_muj_tw07"], 10, 60, 4, 4),
  ];
}
