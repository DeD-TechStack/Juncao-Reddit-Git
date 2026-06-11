import { useNavigate } from "react-router-dom";
import { usePageTitle } from "../lib/usePageTitle";
import { useAuth } from "../contexts/AuthContext";

export default function LandingPage() {
  usePageTitle();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  return (
    <div className="page">
      <header className="app-header">
        <div className="container-app h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="brand-badge h-9 w-9 grid place-items-center rounded-xl font-bold">
              💡
            </div>
            <div className="font-semibold">Gerenciador de Ideias</div>
          </div>

          <div className="flex items-center gap-3">
            {!isAuthenticated ? (
              <>
                <button
                  onClick={() => navigate("/login")}
                  className="text-sm text-muted hover:text-strong"
                >
                  Login
                </button>
                <button
                  onClick={() => navigate("/register")}
                  className="btn-primary h-10 px-4 rounded-lg text-sm font-medium"
                >
                  Registrar
                </button>
              </>
            ) : (
              <button
                onClick={() => navigate("/dashboard")}
                className="btn-primary h-10 px-4 rounded-lg text-sm font-medium"
              >
                Dashboard
              </button>
            )}
          </div>
        </div>
      </header>

      <main className="container-app py-14">
        <div className="text-center max-w-3xl mx-auto">
          <h1 className="text-4xl md:text-5xl font-bold tracking-tight">
            Registre Suas Ideias de Forma <br className="hidden md:block" />
            Simples e Eficiente
          </h1>

          <p className="mt-5 text-muted text-base md:text-lg">
            Um sistema moderno baseado em microserviços para gerenciar todas as suas ideias em um só lugar —
            com segurança. Um lugar onde não poderão roubar suas ideias. Crie, edite e organize suas inspirações
            de maneira profissional.
          </p>

          <div className="mt-8 flex items-center justify-center gap-3">
            <button
              onClick={() => navigate(isAuthenticated ? "/dashboard" : "/register")}
              className="btn-primary h-11 px-6 rounded-lg font-medium"
            >
              Começar Agora
            </button>

            <button
              onClick={() => navigate(isAuthenticated ? "/ideas" : "/login")}
              className="btn-outline h-11 px-6 rounded-lg font-medium"
            >
              Já tenho conta
            </button>
          </div>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {[
            {
              title: "Gestão de Ideias",
              desc: "Crie, edite e organize suas ideias com título, descrição e metadados.",
              icon: "💡",
            },
            {
              title: "Autenticação JWT",
              desc: "Sistema seguro de autenticação com tokens JWT e controle de acesso.",
              icon: "🛡️",
            },
            {
              title: "Controle de Autoria",
              desc: "Apenas o autor pode editar ou excluir suas próprias ideias.",
              icon: "👥",
            },
            {
              title: "Microserviços",
              desc: "Arquitetura moderna com Spring Boot e serviços separados.",
              icon: "⚡",
            },
          ].map((c) => (
            <div key={c.title} className="feature-card p-6">
              <div className="h-11 w-11 rounded-2xl bg-white/10 grid place-items-center text-lg">
                {c.icon}
              </div>
              <h3 className="mt-4 font-semibold">{c.title}</h3>
              <p className="mt-2 text-sm text-muted">{c.desc}</p>
            </div>
          ))}
        </div>

        <div className="mt-14 feature-card p-8">
          <h2 className="text-center font-semibold">Tecnologias Utilizadas</h2>
          <p className="mt-2 text-center text-sm text-muted">
            Frontend (React + Vite + Tailwind) • Auth (Spring Security + JWT + MySQL) • IdeasHub (Spring Boot + MongoDB)
          </p>
        </div>
      </main>
    </div>
  );
}
