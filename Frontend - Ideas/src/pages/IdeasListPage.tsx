import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Header from "../components/Header";
import IdeaCard from "../components/IdeaCard";
import { useIdeas } from "../contexts/IdeasContext";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";
import { Skeleton } from "../components/ui/skeleton";

export default function IdeasListPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  const {
    ideas,
    myIdeas,
    loading,
    deleteIdea,
    refreshAll,
    refreshMine,
    isOwner,
  } = useIdeas();

  const [q, setQ] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const id = params.get("delete");
    if (!id) return;

    let cancelled = false;

    (async () => {
      const ok = await deleteIdea(id);
      if (!cancelled && !ok) setError("Não foi possível excluir. Verifique se a ideia é sua.");

      const next = new URLSearchParams(params);
      next.delete("delete");
      setParams(next, { replace: true });
    })();

    return () => {
      cancelled = true;
    };
  }, [params, setParams, deleteIdea]);

  const list = useMemo(() => {
    const merged = [...myIdeas];
    const rest = ideas.filter((i) => !merged.find((m) => m.id === i.id));
    const all = [...merged, ...rest];

    const query = q.trim().toLowerCase();
    if (!query) return all;

    return all.filter((i) => {
      return (
        i.title.toLowerCase().includes(query) ||
        i.description.toLowerCase().includes(query)
      );
    });
  }, [ideas, myIdeas, q]);

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-8">
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
              <div className="space-y-1">
                <h1 className="text-2xl md:text-3xl font-semibold tracking-tight">
                  Ideias
                </h1>
                <p className="text-sm text-muted">
                  Lista completa (todas) + suas ideias. Busca por título/descrição.
                </p>
              </div>

              <div className="flex flex-wrap gap-2">
                <Button onClick={() => navigate("/create-idea")}>Nova ideia</Button>
                <Button
                  variant="outline"
                  onClick={async () => {
                    setError("");
                    await Promise.all([refreshAll(), refreshMine()]);
                  }}
                >
                  Atualizar
                </Button>
              </div>
            </div>

            {error && (
              <Alert>
                <AlertTitle>Ops</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <Card>
              <CardHeader className="space-y-2">
                <CardTitle>Gerenciar</CardTitle>
                <CardDescription>
                  Dica: suas ideias mostram ações de editar e excluir.
                </CardDescription>
                <div className="pt-2">
                  <Input
                    value={q}
                    onChange={(e) => setQ(e.target.value)}
                    placeholder="Buscar ideias..."
                  />
                </div>
              </CardHeader>

              <CardContent className="space-y-3">
                {loading ? (
                  <div className="grid gap-3 md:grid-cols-2">
                    <Skeleton className="h-40 w-full rounded-lg" />
                    <Skeleton className="h-40 w-full rounded-lg" />
                    <Skeleton className="h-40 w-full rounded-lg" />
                    <Skeleton className="h-40 w-full rounded-lg" />
                  </div>
                ) : list.length === 0 ? (
                  <div className="py-10 text-center text-sm text-muted">
                    Nada encontrado.
                  </div>
                ) : (
                  <div className="grid gap-3 md:grid-cols-2">
                    {list.map((idea) => (
                      <IdeaCard
                        key={idea.id}
                        id={idea.id}
                        title={idea.title}
                        description={idea.description}
                        createdAt={idea.createdAt}
                        onEdit={() => navigate(`/edit-idea/${idea.id}`)}
                        onDelete={async () => {
                          setError("");
                          const ok = await deleteIdea(idea.id);
                          if (!ok) setError("Não foi possível excluir. Verifique se a ideia é sua.");
                        }}
                        showActions
                        isOwner={isOwner(idea)}
                      />
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  );
}
