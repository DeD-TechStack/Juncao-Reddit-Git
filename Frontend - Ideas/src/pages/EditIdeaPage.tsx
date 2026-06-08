import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import { useIdeas } from "../contexts/IdeasContext";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Textarea } from "../components/ui/textarea";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";

export default function EditIdeaPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { getIdea, updateIdea, deleteIdea, isOwner } = useIdeas();

  const idea = useMemo(() => (id ? getIdea(id) : undefined), [getIdea, id]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const titleLen = title.trim().length;
  const descLen = description.trim().length;

  const owner = useMemo(() => isOwner(idea ?? null), [isOwner, idea]); 

  useEffect(() => {
    if (!idea) return;
    setTitle(idea.title ?? "");
    setDescription(idea.description ?? "");
  }, [idea]);

  const canSubmit = useMemo(
    () => owner && titleLen >= 3 && descLen >= 10 && !submitting, 
    [owner, titleLen, descLen, submitting]
  );

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (!id) return;

    if (!owner) {
      setError("Você não tem permissão para editar esta ideia.");
      return;
    }

    if (titleLen < 3) return setError("O título precisa ter pelo menos 3 caracteres.");
    if (descLen < 10) return setError("A descrição precisa ter pelo menos 10 caracteres.");

    setSubmitting(true);
    try {
      const ok = await updateIdea(id, {
        title: title.trim(),
        description: description.trim(),
      });
      if (!ok) {
        setError("Não foi possível atualizar esta ideia.");
        return;
      }
      navigate(`/view-idea/${id}`);
    } catch {
      setError("Erro inesperado ao atualizar.");
    } finally {
      setSubmitting(false);
    }
  }

  async function onDelete() {
    if (!id) return;
    setError("");

    if (!owner) {
      setError("Você não tem permissão para excluir esta ideia.");
      return;
    }

    setSubmitting(true);
    try {
      const ok = await deleteIdea(id);
      if (!ok) {
        setError("Não foi possível excluir. Verifique suas permissões.");
        return;
      }
      navigate("/ideas");
    } finally {
      setSubmitting(false);
    }
  }

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
              Essa ideia não está carregada. Volte para a lista e atualize.
            </AlertDescription>
          </Alert>
          <div className="mt-4">
            <Button onClick={() => navigate("/ideas")}>Voltar para Ideias</Button>
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
          <div className="mx-auto w-full max-w-2xl space-y-4">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <h1 className="text-2xl font-semibold tracking-tight">Editar ideia</h1>
                <p className="text-sm text-muted mt-1">
                  Atualize o conteúdo da sua ideia.
                </p>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" onClick={() => navigate(-1)} disabled={submitting}>
                  Voltar
                </Button>

                {owner && (
                  <Button variant="danger" onClick={onDelete} disabled={submitting}>
                    Excluir
                  </Button>
                )}
              </div>
            </div>

            {!owner && (
              <Alert>
                <AlertTitle>Somente leitura</AlertTitle>
                <AlertDescription>
                  Você pode visualizar esta ideia, mas não pode editar ou excluir.
                </AlertDescription>
              </Alert>
            )}

            {error && (
              <Alert>
                <AlertTitle>Algo deu errado</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <Card>
              <CardHeader>
                <CardTitle>Detalhes</CardTitle>
                <CardDescription>Edite o título e a descrição.</CardDescription>
              </CardHeader>

              <CardContent>
                <form onSubmit={onSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Título</label>
                    <Input
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      disabled={!owner || submitting} 
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
                      rows={7}
                      disabled={!owner || submitting} 
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
                      onClick={() => navigate("/ideas")}
                      disabled={submitting}
                    >
                      Cancelar
                    </Button>

                    <Button type="submit" disabled={!canSubmit}>
                      {submitting ? "Salvando..." : "Salvar alterações"}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  );
}
