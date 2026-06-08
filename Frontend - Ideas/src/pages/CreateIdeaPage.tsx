import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useIdeas } from "../contexts/IdeasContext";
import { useAuth } from "../contexts/AuthContext";

import Header from "../components/Header";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Textarea } from "../components/ui/textarea";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";

export default function CreateIdeaPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { createIdea } = useIdeas();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>("");

  const titleLen = title.trim().length;
  const descLen = description.trim().length;

  const canSubmit = useMemo(() => {
    return !!user && titleLen >= 3 && descLen >= 10 && !submitting;
  }, [user, titleLen, descLen, submitting]);

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (!user) {
      setError("Você precisa estar logado para criar uma ideia.");
      navigate("/login");
      return;
    }

    if (titleLen < 3) {
      setError("O título precisa ter pelo menos 3 caracteres.");
      return;
    }

    if (descLen < 10) {
      setError("A descrição precisa ter pelo menos 10 caracteres.");
      return;
    }

    setSubmitting(true);
    try {
      const ok = await createIdea({
        title: title.trim(),
        description: description.trim(),
      });

      if (!ok) {
        setError("Não foi possível criar a ideia. Tente novamente em instantes.");
        return;
      }

      navigate("/ideas");
    } catch {
      setError("Erro inesperado ao criar a ideia.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-8">
          <div className="mx-auto w-full max-w-2xl">
            <div className="mb-6">
              <h1 className="text-2xl font-semibold tracking-tight">Criar nova ideia</h1>
              <p className="text-sm text-muted mt-1">
                Escreva um título claro e descreva bem a ideia. Depois você pode editar.
              </p>
            </div>

            {error && (
              <div className="mb-4">
                <Alert>
                  <AlertTitle>Algo deu errado</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              </div>
            )}

            <Card>
              <CardHeader>
                <CardTitle>Detalhes da ideia</CardTitle>
                <CardDescription>Preencha as informações abaixo.</CardDescription>
              </CardHeader>

              <CardContent>
                <form onSubmit={onSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Título</label>
                    <Input
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      placeholder="Ex: App para organizar tarefas com IA"
                      autoFocus
                    />
                    <div className="flex justify-between text-xs text-muted">
                      <span>Mínimo 3 caracteres</span>
                      <span>{titleLen}/80</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Descrição</label>
                    <Textarea
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder="Descreva o problema, a solução e como você imagina usar isso..."
                      rows={6}
                    />
                    <div className="flex justify-between text-xs text-muted">
                      <span>Mínimo 10 caracteres</span>
                      <span>{descLen}/800</span>
                    </div>
                  </div>

                  <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end sm:gap-3 pt-2">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => navigate(-1)}
                      disabled={submitting}
                    >
                      Cancelar
                    </Button>

                    <Button type="submit" disabled={!canSubmit}>
                      {submitting ? "Criando..." : "Criar ideia"}
                    </Button>
                  </div>

                  {!user && (
                    <p className="text-xs text-muted pt-2">
                      Você não está logado. Faça login para criar ideias.
                    </p>
                  )}
                </form>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  );
}
