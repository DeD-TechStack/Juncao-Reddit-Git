import { useCallback, useEffect, useState } from "react";
import { usePageTitle } from "../lib/usePageTitle";
import Header from "../components/Header";
import { apiFetch } from "../lib/apiFetch";

import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "../components/ui/card";
import { Skeleton } from "../components/ui/skeleton";
import { Alert, AlertDescription, AlertTitle } from "../components/ui/alert";

const API_TRENDING = import.meta.env.VITE_TRENDING_API_URL || "http://localhost:8085";

interface TrendingItem {
  ideaId: number;
  score: number;
}

interface TrendingResponse {
  period: string;
  key: string;
  items: TrendingItem[];
}

type Period = "daily" | "weekly";

export default function TrendingPage() {
  usePageTitle("Em alta");

  const [period, setPeriod] = useState<Period>("daily");
  const [data, setData] = useState<TrendingResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async (target: Period) => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`${API_TRENDING}/trending/${target}`);

      if (!res.ok) {
        setError("Não foi possível carregar o ranking de ideias em alta.");
        setData(null);
        return;
      }

      setData((await res.json()) as TrendingResponse);
    } catch {
      setError("Erro de conexão com o serviço de Trending.");
      setData(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(period);
  }, [period, load]);

  return (
    <div className="min-h-screen app-page">
      <Header />

      <div className="w-full app-page-bg">
        <main className="container-app py-8">
          <div className="mx-auto w-full max-w-3xl space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <div className="space-y-1">
                <h1 className="text-2xl md:text-3xl font-semibold tracking-tight">
                  Em alta
                </h1>
                <p className="text-sm text-muted">
                  Ranking de ideias com mais curtidas, calculado pelo serviço Trending.
                </p>
              </div>

              <div className="flex gap-2">
                <Button
                  variant={period === "daily" ? "default" : "outline"}
                  onClick={() => setPeriod("daily")}
                >
                  Hoje
                </Button>
                <Button
                  variant={period === "weekly" ? "default" : "outline"}
                  onClick={() => setPeriod("weekly")}
                >
                  Semana
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
              <CardHeader>
                <CardTitle>{period === "daily" ? "Top do dia" : "Top da semana"}</CardTitle>
                <CardDescription>
                  Identificador interno da ideia no Trending e pontuação acumulada.
                </CardDescription>
              </CardHeader>

              <CardContent className="space-y-3">
                {loading ? (
                  <div className="space-y-2">
                    <Skeleton className="h-10 w-full" />
                    <Skeleton className="h-10 w-full" />
                    <Skeleton className="h-10 w-full" />
                  </div>
                ) : !data || data.items.length === 0 ? (
                  <div className="py-10 text-center text-sm text-muted">
                    Nenhuma ideia em alta por aqui ainda.
                  </div>
                ) : (
                  <ol className="space-y-2">
                    {data.items.map((item, index) => (
                      <li
                        key={item.ideaId}
                        className="flex items-center justify-between gap-3 rounded-lg border px-4 py-2"
                      >
                        <div className="flex items-center gap-3">
                          <span className="text-sm font-semibold text-muted">#{index + 1}</span>
                          <span className="text-sm font-medium">Ideia #{item.ideaId}</span>
                        </div>
                        <span className="text-sm text-muted">{item.score.toFixed(1)} pts</span>
                      </li>
                    ))}
                  </ol>
                )}
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  );
}
