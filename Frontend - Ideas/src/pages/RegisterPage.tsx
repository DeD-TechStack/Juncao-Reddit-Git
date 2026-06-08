import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

type PwRuleKey = "minLen" | "upper" | "lower" | "number" | "special";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const nameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const passRef = useRef<HTMLInputElement>(null);
  const confirmRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [shake, setShake] = useState(false);

  useEffect(() => {
    nameRef.current?.focus();
  }, []);

  useEffect(() => {
    if (!error) return;
    setShake(true);
    const t = setTimeout(() => setShake(false), 400);
    return () => clearTimeout(t);
  }, [error]);

  const rules = useMemo(() => {
    const pw = password;

    const hasMinLen = pw.length >= 8;
    const hasUpper = /[A-Z]/.test(pw);
    const hasLower = /[a-z]/.test(pw);
    const hasNumber = /\d/.test(pw);
    const hasSpecial = /[^A-Za-z0-9]/.test(pw);

    const map: Record<PwRuleKey, boolean> = {
      minLen: hasMinLen,
      upper: hasUpper,
      lower: hasLower,
      number: hasNumber,
      special: hasSpecial,
    };

    const passed = Object.values(map).filter(Boolean).length;

    let strength: "fraca" | "média" | "forte" = "fraca";
    if (passed >= 4) strength = "forte";
    else if (passed >= 3) strength = "média";

    return { map, passed, strength };
  }, [password]);

  const canSubmit = useMemo(() => {
    const nameOk = name.trim().length >= 3;
    const emailOk = email.trim().length > 0;
    const pwOk = rules.passed === 5; 
    const confirmOk = password === confirm;

    return nameOk && emailOk && pwOk && confirmOk && !submitting;
  }, [name, email, password, confirm, submitting, rules.passed]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    if (name.trim().length < 3) {
      setError("Informe seu nome (mínimo 3 caracteres).");
      nameRef.current?.focus();
      return;
    }

    if (!email.trim()) {
      setError("Informe um email válido.");
      emailRef.current?.focus();
      return;
    }

    if (rules.passed !== 5) {
      setError("Sua senha ainda não atende todos os requisitos.");
      passRef.current?.focus();
      return;
    }

    if (password !== confirm) {
      setError("As senhas não conferem.");
      confirmRef.current?.focus();
      return;
    }

    setSubmitting(true);
    try {
      const ok = await register(name.trim(), email.trim(), password);
      if (!ok) {
        setError("Não foi possível criar a conta. Verifique os dados e tente novamente.");
        return;
      }
      navigate("/dashboard");
    } finally {
      setSubmitting(false);
    }
  }

  const strengthLabel =
    rules.strength === "fraca" ? "Fraca" : rules.strength === "média" ? "Média" : "Forte";

  const strengthClass =
    rules.strength === "fraca" ? "weak" : rules.strength === "média" ? "medium" : "strong";

  return (
    <div className="min-h-screen app-page hero-bg flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md text-center">
        <div className="brand-badge mx-auto h-10 w-10 grid place-items-center rounded-2xl font-bold">
          💡
        </div>

        <h1 className="mt-4 text-3xl font-bold text-strong">Criar conta</h1>
        <p className="mt-2 text-muted">Comece a organizar suas ideias agora</p>

        <div className={`mt-8 feature-card p-6 text-left ${shake ? "shake" : ""}`}>
          {error && (
            <div className="alert-danger mb-4 rounded-lg px-4 py-3 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={onSubmit} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-strong">Nome</label>
              <div className="mt-2">
                <input
                  ref={nameRef}
                  className="ui-input ui-border ui-surface h-11 w-full rounded-lg px-3 text-sm ui-focus-ring"
                  placeholder="Seu nome completo"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  autoComplete="name"
                  onKeyDown={(e) => {
                    if (e.key === "Enter") emailRef.current?.focus();
                  }}
                />
              </div>
              <p className="mt-2 text-xs text-muted">
                Use seu nome real (mínimo 3 caracteres).
              </p>
            </div>

            <div>
              <label className="text-sm font-medium text-strong">Email</label>
              <div className="mt-2">
                <input
                  ref={emailRef}
                  className="ui-input ui-border ui-surface h-11 w-full rounded-lg px-3 text-sm ui-focus-ring"
                  placeholder="seu@email.com"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                  onKeyDown={(e) => {
                    if (e.key === "Enter") passRef.current?.focus();
                  }}
                />
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-strong">Senha</label>
              <div className="mt-2">
                <input
                  ref={passRef}
                  className="ui-input ui-border ui-surface h-11 w-full rounded-lg px-3 text-sm ui-focus-ring"
                  placeholder="Crie uma senha segura"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                  onKeyDown={(e) => {
                    if (e.key === "Enter") confirmRef.current?.focus();
                  }}
                />
              </div>

              <div className="pw-meter" aria-hidden="true">
                <div className={`pw-bar ${rules.passed >= 1 ? `active ${strengthClass}` : ""}`} />
                <div className={`pw-bar ${rules.passed >= 3 ? `active ${strengthClass}` : ""}`} />
                <div className={`pw-bar ${rules.passed >= 4 ? `active ${strengthClass}` : ""}`} />
              </div>

              <div className="pw-hint text-muted">
                Força: <span className="text-strong font-medium">{strengthLabel}</span>
              </div>

              <div className="pw-hint">
                <div className={rules.map.minLen ? "pw-ok" : "pw-bad"}>• Pelo menos 8 caracteres</div>
                <div className={rules.map.upper ? "pw-ok" : "pw-bad"}>• 1 letra maiúscula (A-Z)</div>
                <div className={rules.map.lower ? "pw-ok" : "pw-bad"}>• 1 letra minúscula (a-z)</div>
                <div className={rules.map.number ? "pw-ok" : "pw-bad"}>• 1 número (0-9)</div>
                <div className={rules.map.special ? "pw-ok" : "pw-bad"}>• 1 símbolo (ex: @ # $ %)</div>
              </div>
            </div>

            <div>
              <label className="text-sm font-medium text-strong">Confirmar senha</label>
              <div className="mt-2">
                <input
                  ref={confirmRef}
                  className="ui-input ui-border ui-surface h-11 w-full rounded-lg px-3 text-sm ui-focus-ring"
                  placeholder="Repita a senha"
                  type="password"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  autoComplete="new-password"
                />
              </div>

              {confirm.length > 0 && (
                <p className={`mt-2 text-xs ${password === confirm ? "pw-ok" : "pw-bad"}`}>
                  {password === confirm ? "Senhas conferem." : "Senhas não conferem."}
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={!canSubmit}
              className="btn-primary h-11 w-full rounded-lg font-medium disabled:opacity-60"
            >
              {submitting ? "Criando..." : "Criar conta"}
            </button>

            <p className="text-center text-sm text-muted">
              Já tem uma conta?{" "}
              <Link to="/login" className="link-primary font-medium">
                Faça login aqui
              </Link>
            </p>
          </form>
        </div>

        <button
          onClick={() => navigate("/")}
          className="mt-6 text-sm text-muted hover:text-strong"
        >
          ← Voltar para página inicial
        </button>
      </div>
    </div>
  );
}
