import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";

type Props = {
  type?: "404" | "403";
};

export default function ErrorPage({ type = "404" }: Props) {
  const navigate = useNavigate();
  const is403 = type === "403";

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-10">
          <div className="mx-auto w-full max-w-xl">
            <Card>
              <CardHeader>
                <CardTitle className="text-2xl">
                  {is403 ? "Acesso negado" : "Página não encontrada"}
                </CardTitle>
                <CardDescription>
                  {is403
                    ? "Você não tem permissão para acessar esse conteúdo."
                    : "A rota não existe ou foi movida."}
                </CardDescription>
              </CardHeader>

              <CardContent className="flex flex-col gap-3">
                <div className="error-box rounded-lg p-4">
                  <div className="text-5xl font-semibold tracking-tight">{type}</div>
                  <p className="mt-2 text-sm text-muted">Volte para uma rota segura.</p>
                </div>

                <div className="flex flex-col gap-2 sm:flex-row sm:justify-end">
                  <Button variant="outline" onClick={() => navigate(-1)}>
                    Voltar
                  </Button>
                  <Button onClick={() => navigate("/dashboard")}>Ir para Dashboard</Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  );
}
