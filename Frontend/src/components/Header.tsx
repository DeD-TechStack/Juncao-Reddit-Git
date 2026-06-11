import { Link, NavLink, useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./ui/button";
import { cn } from "../lib/utils";

type HeaderProps = {
  className?: string;
};

export default function Header({ className }: HeaderProps) {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const navClass = ({ isActive }: { isActive: boolean }) =>
    cn("app-navlink text-sm font-medium transition-colors", isActive && "is-active");

  const [hidden, setHidden] = useState(false);
  const lastYRef = useRef(0);
  const tickingRef = useRef(false);

  useEffect(() => {
    lastYRef.current = window.scrollY;

    const onScroll = () => {
      const y = window.scrollY;

      if (tickingRef.current) return;
      tickingRef.current = true;

      requestAnimationFrame(() => {
        const lastY = lastYRef.current;
        const delta = y - lastY;

        const nearTop = y < 24;
        const scrollingDown = delta > 8;
        const scrollingUp = delta < -8;

        if (nearTop) {
          setHidden(false);
        } else if (scrollingDown) {
          setHidden(true);
        } else if (scrollingUp) {
          setHidden(false);
        }

        lastYRef.current = y;
        tickingRef.current = false;
      });
    };

    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={cn(
        "app-header sticky top-0 z-50 w-full",
        "app-header--fx",
        hidden && "app-header--hidden",
        className
      )}
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2">
          <div className="brand-badge grid h-9 w-9 place-items-center rounded-xl font-bold">
            I
          </div>

          <div className="leading-tight">
            <div className="text-sm font-semibold">IdeasHub</div>
            <div className="text-xs text-muted">Organize ideias com estilo</div>
          </div>
        </Link>

        <nav className="hidden items-center gap-6 md:flex">
          {isAuthenticated ? (
            <>
              <NavLink to="/dashboard" className={navClass}>
                Dashboard
              </NavLink>
              <NavLink to="/ideas" className={navClass}>
                Ideias
              </NavLink>
              <NavLink to="/profile" className={navClass}>
                Perfil
              </NavLink>
            </>
          ) : (
            <>
              <NavLink to="/" className={navClass}>
                Início
              </NavLink>
              <NavLink to="/login" className={navClass}>
                Login
              </NavLink>
              <NavLink to="/register" className={navClass}>
                Registro
              </NavLink>
            </>
          )}
        </nav>

        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <>
              <div className="hidden md:block text-right mr-2">
                <div className="text-sm font-medium leading-4">{user?.name ?? "Usuário"}</div>
                <div className="text-xs text-muted">{user?.email ?? ""}</div>
              </div>

              <Button
                variant="outline"
                onClick={() => navigate("/create-idea")}
                className="hidden sm:inline-flex"
              >
                Nova ideia
              </Button>

              <Button
                variant="destructive"
                onClick={() => {
                  logout();
                  navigate("/login");
                }}
              >
                Sair
              </Button>
            </>
          ) : (
            <>
              <Button variant="outline" onClick={() => navigate("/login")}>
                Entrar
              </Button>
              <Button onClick={() => navigate("/register")}>Criar conta</Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
