import { useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import { useIdeas } from "../contexts/IdeasContext";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";
import { Badge } from "../components/ui/badge";

export default function ViewIdeaPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { getIdea, isOwner } = useIdeas();

  const idea = useMemo(() => (id ? getIdea(id) : undefined), [getIdea, id]);
  const owner = isOwner(idea);

  if (!id) {
    return (
      <div className="min-h-screen app-page">
        <Header />
        <main className="container-app py-10">
          <Alert>
            <AlertTitle>Rota inválida</AlertTitle>
            <AlertDescription>ID não informado.</AlertDescription>
          </Alert>
        </main>
      </div>
    );
  }

  if (!idea) {
    return (
      <div className="min-h-screen app-page">
        <Header />
        <main className="container-app py-10">
          <Alert>
            <AlertTitle>Ideia não encontrada</AlertTitle>
            <AlertDescription>
              Essa ideia não está no cache do front. Volte e atualize a lista.
            </AlertDescription>
          </Alert>

          <div className="mt-4 flex gap-2">
            <Button variant="outline" onClick={() => navigate(-1)}>
              Voltar
            </Button>
            <Button onClick={() => navigate("/ideas")}>Ir para Ideias</Button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-8">
          <div className="mx-auto w-full max-w-3xl space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <h1 className="text-2xl md:text-3xl font-semibold tracking-tight">
                    {idea.title}
                  </h1>
                  <Badge variant="secondary">Idea</Badge>
                </div>

                <p className="text-sm text-muted">
                  {idea.createdAt
                    ? `Criado em: ${new Date(idea.createdAt).toLocaleString()}`
                    : "—"}
                </p>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" onClick={() => navigate(-1)}>
                  Voltar
                </Button>
                {owner && (
                  <Button onClick={() => navigate(`/edit-idea/${idea.id}`)}>
                    Editar
                  </Button>
                )}
              </div>
            </div>

            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">Detalhes</CardTitle>
                <CardDescription>
                  Informações gerais da ideia.
                </CardDescription>
              </CardHeader>
              <CardContent className="flex flex-col gap-2 text-sm">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-muted">Acesso</span>
                  <span className="font-medium">{owner ? "Você é o autor" : "Somente leitura"}</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Descrição</CardTitle>
                <CardDescription>Detalhes completos da ideia.</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="whitespace-pre-wrap text-sm leading-relaxed text-muted">
                  {idea.description}
                </p>
              </CardContent>
            </Card>

            {!owner && (
              <Alert>
                <AlertTitle>Somente leitura</AlertTitle>
                <AlertDescription>
                  Você pode visualizar essa ideia, mas não pode editar/excluir porque não é o autor.
                </AlertDescription>
              </Alert>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
