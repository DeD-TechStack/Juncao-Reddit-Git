import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { useAuth } from "../contexts/AuthContext";
import { useIdeas } from "../contexts/IdeasContext";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { myIdeas } = useIdeas();
  const [copied, setCopied] = useState(false);

  const total = useMemo(() => myIdeas.length, [myIdeas]);

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
                <p className="text-sm text-muted mt-1">
                  Suas informações e atalhos rápidos.
                </p>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" onClick={() => navigate("/dashboard")}>
                  Dashboard
                </Button>
                <Button
                  variant="danger"
                  onClick={() => {
                    logout();
                    navigate("/login");
                  }}
                >
                  Sair
                </Button>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
              <Card className="md:col-span-2">
                <CardHeader>
                  <CardTitle>Informações</CardTitle>
                  <CardDescription>Seu perfil incrível.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-xs text-muted">Nome</span>
                    <div className="text-base font-medium">{user?.name ?? "—"}</div>
                  </div>

                  <div className="flex items-center justify-between gap-3">
                    <div className="flex flex-col gap-1">
                      <span className="text-xs text-muted">Email</span>
                      <div className="text-sm font-medium">{user?.email ?? "—"}</div>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" onClick={copyEmail}>
                        {copied ? "Copiado!" : "Copiar"}
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Ideias</CardTitle>
                  <CardDescription>Resumo</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="text-3xl font-semibold">{total}</div>
                  <p className="text-xs text-muted">
                    Total de ideias criadas por você.
                  </p>
                  <Button className="w-full" onClick={() => navigate("/ideas")}>
                    Ver ideias
                  </Button>
                </CardContent>
              </Card>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
