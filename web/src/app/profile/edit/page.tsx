"use client";

import { Mail, MapPin, Phone, User } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { PageHeader, RequireLogin, useToast } from "@/components/layout";
import { Button, TextField } from "@/components/ui";
import { useParkora } from "@/lib/store";
import { required, validateName, validatePhone } from "@/lib/utils";

/* EDIT PROFILE ( /profile/edit ) – name, phone and location. Email is the login id and cannot change. */
function EditProfile() {
  const { currentUser, updateProfile } = useParkora();
  const router = useRouter();
  const toast = useToast();
  const user = currentUser!;
  const [name, setName] = useState(user.name);
  const [phone, setPhone] = useState(user.phone);
  const [location, setLocation] = useState(user.location);
  const [errors, setErrors] = useState<Record<string, string | null>>({});

  const [saving, setSaving] = useState(false);

  async function save(e: React.FormEvent) {
    e.preventDefault();
    const next = { name: validateName(name), phone: validatePhone(phone), location: required(location, "your location") };
    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    setSaving(true);
    const err = await updateProfile(name, phone, location);
    setSaving(false);
    toast(err ?? "Profile updated");
    if (!err) router.push("/profile/");
  }

  return (
    <form onSubmit={save} noValidate className="mx-auto max-w-xl space-y-3 px-4 py-8 animate-fade-up">
      <TextField label="Full Name" icon={User} value={name} onChange={(v) => { setName(v); setErrors({ ...errors, name: null }); }} error={errors.name} />
      <TextField label="Phone Number" icon={Phone} inputMode="numeric" maxLength={10} value={phone}
        onChange={(v) => { setPhone(v.replace(/\D/g, "")); setErrors({ ...errors, phone: null }); }} error={errors.phone} />
      <TextField label="Location" icon={MapPin} placeholder="Area, City, Country" value={location}
        onChange={(v) => { setLocation(v); setErrors({ ...errors, location: null }); }} error={errors.location}
        hint="Typed by you – shown on the Find Parking page. (No GPS in this version.)" />
      <TextField label="Email (cannot be changed)" icon={Mail} value={user.email} onChange={() => {}} readOnly className="opacity-70" />
      <Button type="submit" size="lg" arrow loading={saving} className="mt-4 w-full">Save Changes</Button>
    </form>
  );
}

export default function EditProfilePage() {
  return (
    <>
      <PageHeader title="Edit Profile" back="/profile/" />
      <RequireLogin><EditProfile /></RequireLogin>
    </>
  );
}
