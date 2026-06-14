import { useEffect, useMemo, useState } from "react";
import { usePageTitle } from "../lib/usePageTitle";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { useAuth } from "../contexts/AuthContext";
import { useIdeas } from "../contexts/IdeasContext";
import { apiFetch } from "../lib/apiFetch";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Skeleton } from "../components/ui/skeleton";
import { Badge } from "../components/ui/badge";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";

const API_PROFILE = import.meta.env.VITE_PROFILE_API_URL || "http://localhost:8085/api";
const API_REPUTATION = import.meta.env.VITE_REPUTATION_API_URL || "http://localhost:8084";

interface Reputation {
  userId: string;
  xp: number;
  level: number;
  title: string;
}

interface Profile {
  id: string;
  userId: string;
  username: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  location: string | null;
  website: string | null;
  isPublic: boolean;
}

interface ProfileForm {
  username: string;
  displayName: string;
  bio: string;
  location: string;
  website: string;
}

export default function ProfilePage() {
  usePageTitle("Perfil");
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const { myIdeas } = useIdeas();

  const [profile, setProfile] = useState<Profile | null>(null);
  const [reputation, setReputation] = useState<Reputation | null>(null);
  const [reputationLoading, setReputationLoading] = useState(true);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [copied, setCopied] = useState(false);

  const [form, setForm] = useState<ProfileForm>({
    username: "",
    displayName: "",
    bio: "",
    location: "",
    website: "",
  });

  const total = useMemo(() => myIdeas.length, [myIdeas]);

  useEffect(() => {
    if (!token) return;

    (async () => {
      setLoading(true);
      try {
        const res = await apiFetch(`${API_PROFILE}/profiles/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.ok) {
          const data: Profile = await res.json();
          setProfile(data);
          setForm({
            username: data.username ?? "",
            displayName: data.displayName ?? "",
            bio: data.bio ?? "",
            location: data.location ?? "",
            website: data.website ?? "",
          });
        }
      } catch {
        setError("Não foi possível carregar o perfil.");
      } finally {
        setLoading(false);
      }
    })();
  }, [token]);

  useEffect(() => {
    if (!token) return;

    (async () => {
      setReputationLoading(true);
      try {
        const res = await apiFetch(`${API_REPUTATION}/reputation/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.ok) {
          setReputation((await res.json()) as Reputation);
        }
      } catch {
        // Falha ao carregar reputação não bloqueia a página de perfil.
      } finally {
        setReputationLoading(false);
      }
    })();
  }, [token]);

  async function handleSave() {
    if (!token) return;
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      const res = await apiFetch(`${API_PROFILE}/profiles/me`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          username: form.username || undefined,
          displayName: form.displayName || undefined,
          bio: form.bio || undefined,
          location: form.location || undefined,
          website: form.website || undefined,
        }),
      });

      if (!res.ok) {
        const body = await res.json().catch(() => null);
        setError(body?.message ?? "Erro ao salvar perfil.");
        return;
      }

      const updated: Profile = await res.json();
      setProfile(updated);
      setEditing(false);
      setSuccess("Perfil atualizado com sucesso!");
      setTimeout(() => setSuccess(""), 3000);
    } catch {
      setError("Erro de conexão ao salvar perfil.");
    } finally {
      setSaving(false);
    }
  }

  async function copyEmail() {
    if (!user?.email) return;
    try {
      await navigator.clipboard.writeText(user.email);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    } catch {
      // ignora
    }
  }

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-8">
          <div className="mx-auto w-full max-w-3xl space-y-4">

            <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <h1 className="text-2xl md:text-3xl font-semibold tracking-tight">Perfil</h1>
                <p className="text-sm text-muted mt-1">Suas informações e atalhos rápidos.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" onClick={() => navigate("/dashboard")}>
                  Dashboard
                </Button>
                <Button
                  variant="danger"
                  onClick={async () => {
                    await logout();
                    navigate("/login");
                  }}
                >
                  Sair
                </Button>
              </div>
            </div>

            {error && (
              <Alert>
                <AlertTitle>Ops</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            {success && (
              <Alert>
                <AlertTitle>Sucesso</AlertTitle>
                <AlertDescription>{success}</AlertDescription>
              </Alert>
            )}

            <div className="grid gap-4 md:grid-cols-3">
              <Card className="md:col-span-2">
                <CardHeader className="flex flex-row items-center justify-between">
                  <div>
                    <CardTitle>Informações</CardTitle>
                    <CardDescription>Seu perfil na plataforma.</CardDescription>
                  </div>
                  {!editing && (
                    <Button variant="outline" onClick={() => setEditing(true)}>
                      Editar
                    </Button>
                  )}
                </CardHeader>

                <CardContent className="space-y-4">
                  {loading ? (
                    <>
                      <Skeleton className="h-5 w-3/4" />
                      <Skeleton className="h-5 w-1/2" />
                      <Skeleton className="h-5 w-2/3" />
                    </>
                  ) : editing ? (
                    <div className="space-y-3">
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Username</span>
                        <Input
                          value={form.username}
                          onChange={(e) => setForm({ ...form, username: e.target.value })}
                          placeholder="seu_username"
                        />
                      </div>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Nome de exibição</span>
                        <Input
                          value={form.displayName}
                          onChange={(e) => setForm({ ...form, displayName: e.target.value })}
                          placeholder="Seu Nome"
                        />
                      </div>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Bio</span>
                        <Input
                          value={form.bio}
                          onChange={(e) => setForm({ ...form, bio: e.target.value })}
                          placeholder="Fale um pouco sobre você..."
                        />
                      </div>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Localização</span>
                        <Input
                          value={form.location}
                          onChange={(e) => setForm({ ...form, location: e.target.value })}
                          placeholder="Cidade, País"
                        />
                      </div>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Website</span>
                        <Input
                          value={form.website}
                          onChange={(e) => setForm({ ...form, website: e.target.value })}
                          placeholder="https://seusite.com"
                        />
                      </div>
                      <div className="flex gap-2 pt-2">
                        <Button onClick={handleSave} disabled={saving}>
                          {saving ? "Salvando..." : "Salvar"}
                        </Button>
                        <Button variant="outline" onClick={() => setEditing(false)} disabled={saving}>
                          Cancelar
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="flex flex-col gap-1">
                        <span className="text-xs text-muted">Nome</span>
                        <div className="flex items-center gap-2">
                          <div className="text-base font-medium">{profile?.displayName || user?.name || "—"}</div>
                          {reputationLoading ? (
                            <Skeleton className="h-5 w-20" />
                          ) : reputation ? (
                            <Badge variant="secondary">
                              {reputation.title} · Nível {reputation.level}
                            </Badge>
                          ) : null}
                        </div>
                      </div>
                      {profile?.username && (
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-muted">Username</span>
                          <div className="text-sm font-medium">@{profile.username}</div>
                        </div>
                      )}
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-muted">Email</span>
                          <div className="text-sm font-medium">{user?.email ?? "—"}</div>
                        </div>
                        <Button variant="outline" onClick={copyEmail}>
                          {copied ? "Copiado!" : "Copiar"}
                        </Button>
                      </div>
                      {profile?.bio && (
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-muted">Bio</span>
                          <div className="text-sm">{profile.bio}</div>
                        </div>
                      )}
                      {profile?.location && (
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-muted">Localização</span>
                          <div className="text-sm">{profile.location}</div>
                        </div>
                      )}
                      {profile?.website && (
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-muted">Website</span>
                          <a
                            href={profile.website}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-sm text-blue-600 hover:underline dark:text-blue-400"
                          >
                            {profile.website}
                          </a>
                        </div>
                      )}
                    </>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Ideias</CardTitle>
                  <CardDescription>Resumo</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="text-3xl font-semibold">{total}</div>
                  <p className="text-xs text-muted">Total de ideias criadas por você.</p>
                  <Button className="w-full" onClick={() => navigate("/ideas")}>
                    Ver ideias
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Reputação</CardTitle>
                  <CardDescription>Pontuação na plataforma</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                  {reputationLoading ? (
                    <>
                      <Skeleton className="h-8 w-1/2" />
                      <Skeleton className="h-4 w-2/3" />
                    </>
                  ) : reputation ? (
                    <>
                      <div className="text-3xl font-semibold">{reputation.xp} XP</div>
                      <p className="text-xs text-muted">
                        Nível {reputation.level} · {reputation.title}
                      </p>
                    </>
                  ) : (
                    <p className="text-xs text-muted">Reputação indisponível no momento.</p>
                  )}
                </CardContent>
              </Card>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
}
