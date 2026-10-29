"use client";

import { Building2, Camera, FileText, House, IndianRupee, LandPlot, ParkingSquare, Ruler, Upload, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { useRef, useState } from "react";
import { PageHeader, RequireLogin, StepIndicator, useToast } from "@/components/layout";
import { ParkingImage } from "@/components/parking";
import { Button, Chip, CreamCard, ErrorText, FieldLabel, TextField } from "@/components/ui";
import { resizeImage } from "@/lib/image";
import { useParkora } from "@/lib/store";
import { positiveNumber, required } from "@/lib/utils";

/*
 * LIST PARKING SPOT – 4-STEP FORM ( /list-spot )  – Figure 3 + section 9 of the synopsis
 *   Step 1 Location/Address · Step 2 Parking Size · Step 3 Upload Documents · Step 4 Pricing
 * All answers are kept in one `form` state object, so moving between steps loses nothing.
 * "Next" validates only the current step (validateStep), like ListParkingScreen.kt.
 */
const PARKING_TYPES = ["Open", "Covered", "Basement"];
const VEHICLES = ["SUV", "Sedan", "Regular", "Two Wheeler"];
const STEP_TITLES = ["Select Location/Address", "Parking Size", "Upload Documents", "Pricing"];

const emptyForm = {
  parkingNumber: "", societyName: "", address: "", city: "",
  parkingType: "", lengthFeet: "", widthFeet: "", vehicleTypes: [] as string[],
  images: [] as string[], documents: [] as string[],
  pricePerHour: "", pricePerDay: "", forRent: true, forSale: false, availableNow: true,
};
type Form = typeof emptyForm;

function ListSpotForm() {
  const { addParkingSpot } = useParkora();
  const router = useRouter();
  const toast = useToast();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState<Form>(emptyForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [uploading, setUploading] = useState(false);
  const photoInput = useRef<HTMLInputElement>(null);
  const docInput = useRef<HTMLInputElement>(null);

  /** Updates one field and removes its error message. */
  function update<K extends keyof Form>(field: K, value: Form[K], errorKey: string = field) {
    setForm((f) => ({ ...f, [field]: value }));
    setErrors((e) => { const { [errorKey]: _removed, ...rest } = e; return rest; });
  }

  function validateStep(s: number): boolean {
    const e: Record<string, string | null> = {};
    if (s === 1) {
      e.parkingNumber = required(form.parkingNumber, "the parking number");
      e.societyName = required(form.societyName, "the society name");
      e.address = required(form.address, "the address");
      e.city = required(form.city, "the city");
    }
    if (s === 2) {
      e.parkingType = form.parkingType ? null : "Please choose a parking type";
      e.lengthFeet = positiveNumber(form.lengthFeet, "Length");
      e.widthFeet = positiveNumber(form.widthFeet, "Width");
      e.vehicleTypes = form.vehicleTypes.length ? null : "Choose at least one vehicle type";
    }
    if (s === 4) {
      e.pricePerHour = positiveNumber(form.pricePerHour, "Price per hour");
      e.pricePerDay = positiveNumber(form.pricePerDay, "Price per day");
      e.availableFor = form.forRent || form.forSale ? null : "Choose Rent, Sale or both";
    }
    const found = Object.fromEntries(Object.entries(e).filter(([, v]) => v)) as Record<string, string>;
    setErrors(found);
    return Object.keys(found).length === 0;
  }

  async function addPhotos(files: FileList | null) {
    if (!files?.length) return;
    setUploading(true);
    try {
      const resized = await Promise.all(Array.from(files).slice(0, 3 - form.images.length).map((f) => resizeImage(f)));
      update("images", [...form.images, ...resized]);
    } catch (err) {
      toast((err as Error).message);
    } finally {
      setUploading(false);
      if (photoInput.current) photoInput.current.value = "";
    }
  }

  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    setSubmitting(true);
    const error = await addParkingSpot({
      parkingNumber: form.parkingNumber.trim().toUpperCase(), societyName: form.societyName.trim(),
      address: form.address.trim(), city: form.city.trim(),
      parkingType: form.parkingType, lengthFeet: Number(form.lengthFeet), widthFeet: Number(form.widthFeet),
      vehicleTypes: VEHICLES.filter((v) => form.vehicleTypes.includes(v)),
      imagePaths: form.images, documentNames: form.documents, documentPaths: [],
      pricePerHour: Number(form.pricePerHour), pricePerDay: Number(form.pricePerDay),
      availableForRent: form.forRent, availableForSale: form.forSale, isAvailable: form.availableNow,
      isVerified: false, rating: 0,
    });
    setSubmitting(false);
    if (error) { toast(error); return; }
    toast(`Parking spot ${form.parkingNumber.toUpperCase()} is now live!`);
    router.push("/my-spots/");
  }

  function next() {
    if (!validateStep(step)) return;
    if (step < 4) { setStep(step + 1); window.scrollTo({ top: 0, behavior: "smooth" }); }
    else submit();
  }

  const toggleVehicle = (v: string) =>
    update("vehicleTypes", form.vehicleTypes.includes(v) ? form.vehicleTypes.filter((x) => x !== v) : [...form.vehicleTypes, v]);

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      <StepIndicator current={step} />
      <form onSubmit={(e) => { e.preventDefault(); next(); }} noValidate className="mt-8">
        <h2 key={step} className="text-[22px] font-medium animate-fade-up">{STEP_TITLES[step - 1]}</h2>

        {step === 1 && (
          <div className="space-y-3 animate-fade-up">
            <TextField label="Parking Number" icon={ParkingSquare} placeholder="Please specify the parking number"
              value={form.parkingNumber} onChange={(v) => update("parkingNumber", v)} error={errors.parkingNumber} />
            <TextField label="Society Name" icon={Building2} placeholder="e.g. Prateek Grand City"
              value={form.societyName} onChange={(v) => update("societyName", v)} error={errors.societyName} />
            <TextField label="Address" icon={House} placeholder="Tower / block, street, area"
              value={form.address} onChange={(v) => update("address", v)} error={errors.address} />
            <TextField label="City" icon={LandPlot} placeholder="e.g. Ghaziabad"
              value={form.city} onChange={(v) => update("city", v)} error={errors.city} />
          </div>
        )}

        {step === 2 && (
          <div className="animate-fade-up">
            <FieldLabel>Parking Type</FieldLabel>
            <div className="flex flex-wrap gap-2.5">
              {PARKING_TYPES.map((t) => <Chip key={t} selected={form.parkingType === t} onClick={() => update("parkingType", t)}>{t}</Chip>)}
            </div>
            <ErrorText error={errors.parkingType} />
            <div className="mt-4 grid grid-cols-2 gap-4">
              <TextField label="Length (feet)" icon={Ruler} inputMode="numeric" placeholder="e.g. 16"
                value={form.lengthFeet} onChange={(v) => update("lengthFeet", v.replace(/\D/g, ""))} error={errors.lengthFeet} />
              <TextField label="Width (feet)" icon={Ruler} inputMode="numeric" placeholder="e.g. 8"
                value={form.widthFeet} onChange={(v) => update("widthFeet", v.replace(/\D/g, ""))} error={errors.widthFeet} />
            </div>
            <p className="ml-1.5 mt-2 text-xs text-muted">Tip: a normal car needs about 16 × 8 feet.</p>
            <FieldLabel>Suitable Vehicles (choose one or more)</FieldLabel>
            <div className="flex flex-wrap gap-2.5">
              {VEHICLES.map((v) => <Chip key={v} selected={form.vehicleTypes.includes(v)} onClick={() => toggleVehicle(v)}>{v}</Chip>)}
            </div>
            <ErrorText error={errors.vehicleTypes} />
          </div>
        )}

        {step === 3 && (
          <div className="animate-fade-up">
            <FieldLabel>Parking Photos (up to 3)</FieldLabel>
            <div className="flex flex-wrap gap-3">
              {form.images.map((src, i) => (
                <div key={i} className="relative">
                  <ParkingImage path={src} alt={`Photo ${i + 1}`} className="size-28 rounded-xl" />
                  <button type="button" onClick={() => update("images", form.images.filter((_, j) => j !== i))} aria-label={`Remove photo ${i + 1}`}
                    className="absolute right-1.5 top-1.5 grid size-6 place-items-center rounded-full bg-white shadow"><X className="size-3.5" /></button>
                </div>
              ))}
              {form.images.length < 3 ? (
                <button type="button" onClick={() => photoInput.current?.click()} disabled={uploading}
                  className="grid size-28 cursor-pointer place-items-center rounded-xl border border-dashed border-[#e6d98c] bg-cream text-xs transition hover:border-btn-bottom">
                  <span className="flex flex-col items-center gap-1">
                    {uploading ? <span className="size-5 animate-spin rounded-full border-2 border-navy/30 border-t-navy" /> : <Camera className="size-6" />}
                    {uploading ? "Processing…" : "Add Photo"}
                  </span>
                </button>
              ) : null}
            </div>
            <input ref={photoInput} type="file" accept="image/*" multiple hidden onChange={(e) => addPhotos(e.target.files)} />

            <FieldLabel>Ownership / Authorization Documents</FieldLabel>
            <div className="space-y-2">
              {form.documents.map((name, i) => (
                <CreamCard key={i} className="flex items-center gap-3 rounded-xl px-4 py-3 text-sm">
                  <FileText className="size-5 shrink-0" /> <span className="flex-1 truncate">{name}</span>
                  <button type="button" onClick={() => update("documents", form.documents.filter((_, j) => j !== i))} aria-label={`Remove ${name}`}><X className="size-4 text-muted" /></button>
                </CreamCard>
              ))}
              <button type="button" onClick={() => docInput.current?.click()}
                className="flex w-full cursor-pointer flex-col items-center gap-1 rounded-xl border border-dashed border-[#e6d98c] bg-cream py-5 text-sm transition hover:border-btn-bottom">
                <Upload className="size-5" /> Upload Document (PDF or image)
              </button>
              <input ref={docInput} type="file" accept="application/pdf,image/*" multiple hidden
                onChange={(e) => { const names = Array.from(e.target.files ?? []).map((f) => f.name); update("documents", [...form.documents, ...names]); e.target.value = ""; }} />
            </div>
            <p className="mt-4 text-xs text-muted">
              Optional. Up to 3 photos are compressed and saved online with your listing. For documents only the
              <b> file name</b> is recorded. New listings show “Verification pending” until the Parkora admin verifies them.
            </p>
          </div>
        )}

        {step === 4 && (
          <div className="animate-fade-up">
            <div className="grid grid-cols-2 gap-4">
              <TextField label="Price per Hour (₹)" icon={IndianRupee} inputMode="numeric" placeholder="e.g. 40"
                value={form.pricePerHour} onChange={(v) => update("pricePerHour", v.replace(/\D/g, ""))} error={errors.pricePerHour} />
              <TextField label="Price per Day (₹)" icon={IndianRupee} inputMode="numeric" placeholder="e.g. 300"
                value={form.pricePerDay} onChange={(v) => update("pricePerDay", v.replace(/\D/g, ""))} error={errors.pricePerDay} />
            </div>
            <FieldLabel>Available For</FieldLabel>
            <div className="flex gap-2.5">
              <Chip selected={form.forRent} onClick={() => update("forRent", !form.forRent, "availableFor")}>Rent</Chip>
              <Chip selected={form.forSale} onClick={() => update("forSale", !form.forSale, "availableFor")}>Sale</Chip>
            </div>
            <ErrorText error={errors.availableFor} />
            <label className="mt-5 flex cursor-pointer items-center justify-between gap-4">
              <span><span className="block font-medium">Available for booking now</span>
                <span className="text-xs text-muted">You can pause it later in My Parking Spots</span></span>
              <input type="checkbox" role="switch" checked={form.availableNow} onChange={(e) => update("availableNow", e.target.checked)}
                className="h-7 w-12 cursor-pointer appearance-none rounded-full bg-light-gray transition before:block before:size-6 before:translate-x-0.5 before:rounded-full before:bg-white before:shadow before:transition checked:bg-yellow checked:before:translate-x-[22px] checked:before:bg-navy" />
            </label>
            <CreamCard className="mt-6 p-5 text-sm">
              <p className="mb-2 font-semibold">Review</p>
              <Review label="Parking" value={`${form.parkingNumber.toUpperCase()}, ${form.societyName}`} />
              <Review label="Address" value={`${form.address}, ${form.city}`} />
              <Review label="Size" value={`${form.parkingType}, ${form.lengthFeet} × ${form.widthFeet} ft`} />
              <Review label="Vehicles" value={form.vehicleTypes.join(", ")} />
              <Review label="Photos / Docs" value={`${form.images.length} / ${form.documents.length}`} />
            </CreamCard>
          </div>
        )}

        <div className="mt-8 flex gap-3">
          {step > 1 ? <Button type="button" variant="outline" size="lg" onClick={() => { setErrors({}); setStep(step - 1); }}>Back</Button> : null}
          <Button type="submit" size="lg" arrow loading={submitting} className="flex-1">{step < 4 ? "Next" : submitting ? "Publishing…" : "Submit"}</Button>
        </div>
      </form>
    </div>
  );
}

function Review({ label, value }: { label: string; value: string }) {
  return <p className="py-0.5"><span className="text-muted">{label}: </span>{value}</p>;
}

export default function ListSpotPage() {
  return (
    <>
      <PageHeader title="List Parking Spot" back />
      <RequireLogin><ListSpotForm /></RequireLogin>
    </>
  );
}
