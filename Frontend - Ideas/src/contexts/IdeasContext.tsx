import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  ReactNode,
  useCallback,
} from "react";
import { useAuth } from "./AuthContext";

export interface Idea {
  id: string;
  title: string;
  description: string;
  authorId: string;
  createdAt: string;
}

interface IdeasContextType {
  ideas: Idea[];
  myIdeas: Idea[];
  loading: boolean;

  refreshAll: () => Promise<void>;
  refreshMine: () => Promise<void>;

  createIdea: (idea: { title: string; description: string }) => Promise<boolean>;
  updateIdea: (id: string, idea: { title: string; description: string }) => Promise<boolean>;
  deleteIdea: (id: string) => Promise<boolean>;

  getIdea: (id: string) => Idea | undefined;
  fetchIdeaById: (id: string) => Promise<Idea | null>;

  isOwner: (idea?: Pick<Idea, "authorId"> | null) => boolean;
}

const IdeasContext = createContext<IdeasContextType | undefined>(undefined);

const API_IDEAS = import.meta.env.VITE_IDEAS_API_URL ? `${import.meta.env.VITE_IDEAS_API_URL}/ideas` : "http://localhost:8082/api/ideas";

export function IdeasProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated, token, user } = useAuth();

  const [ideas, setIdeas] = useState<Idea[]>([]);
  const [myIdeas, setMyIdeas] = useState<Idea[]>([]);
  const [loading, setLoading] = useState(false);

  const authHeaders = useCallback((): Record<string, string> => {
    return token ? { Authorization: `Bearer ${token}` } : {};
  }, [token]);

  const isOwner = useCallback(
    (idea?: Pick<Idea, "authorId"> | null) => {
      if (!idea?.authorId) return false;
      if (!user?.email) return false;
      return idea.authorId === user.email;
    },
    [user?.email]
  );

  const refreshAll = useCallback(async () => {
    if (!isAuthenticated) {
      setIdeas([]);
      return;
    }

    try {
      const res = await fetch(API_IDEAS, { headers: authHeaders() });

      if (!res.ok) {
        if (res.status === 401 || res.status === 403) setIdeas([]);
        return;
      }

      const data = (await res.json()) as Idea[];
      setIdeas(data);
    } catch (err) {
      console.error(err);
      setIdeas([]);
    }
  }, [authHeaders, isAuthenticated]);

  const refreshMine = useCallback(async () => {
    if (!isAuthenticated) {
      setMyIdeas([]);
      return;
    }

    try {
      const res = await fetch(`${API_IDEAS}/my-ideas`, { headers: authHeaders() });

      if (!res.ok) {
        if (res.status === 401 || res.status === 403) setMyIdeas([]);
        return;
      }

      const data = (await res.json()) as Idea[];
      setMyIdeas(data);
    } catch (err) {
      console.error(err);
      setMyIdeas([]);
    }
  }, [authHeaders, isAuthenticated]);

  const refreshBoot = useCallback(async () => {
    if (!isAuthenticated) {
      setIdeas([]);
      setMyIdeas([]);
      return;
    }

    setLoading(true);
    try {
      await Promise.all([refreshAll(), refreshMine()]);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, refreshAll, refreshMine]);

  useEffect(() => {
    refreshBoot();
  }, [refreshBoot]);

  const createIdea = useCallback(
    async (idea: { title: string; description: string }) => {
      try {
        const res = await fetch(API_IDEAS, {
          method: "POST",
          headers: { "Content-Type": "application/json", ...authHeaders() },
          body: JSON.stringify(idea),
        });

        if (!res.ok) return false;

        await refreshBoot();
        return true;
      } catch {
        return false;
      }
    },
    [authHeaders, refreshBoot]
  );

  const updateIdea = useCallback(
    async (id: string, idea: { title: string; description: string }) => {
      try {
        const res = await fetch(`${API_IDEAS}/${id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json", ...authHeaders() },
          body: JSON.stringify(idea),
        });

        if (!res.ok) return false;

        await refreshBoot();
        return true;
      } catch {
        return false;
      }
    },
    [authHeaders, refreshBoot]
  );

  const deleteIdea = useCallback(
    async (id: string) => {
      try {
        const res = await fetch(`${API_IDEAS}/${id}`, {
          method: "DELETE",
          headers: authHeaders(),
        });

        if (!res.ok) return false;

        await refreshBoot();
        return true;
      } catch {
        return false;
      }
    },
    [authHeaders, refreshBoot]
  );

  const fetchIdeaById = useCallback(
    async (id: string): Promise<Idea | null> => {
      try {
        const res = await fetch(`${API_IDEAS}/${id}`, { headers: authHeaders() });
        if (!res.ok) return null;
        return (await res.json()) as Idea;
      } catch {
        return null;
      }
    },
    [authHeaders]
  );

  const value = useMemo<IdeasContextType>(
    () => ({
      ideas,
      myIdeas,
      loading,
      refreshAll,
      refreshMine,
      createIdea,
      updateIdea,
      deleteIdea,
      getIdea: (id) => myIdeas.find((i) => i.id === id) ?? ideas.find((i) => i.id === id),
      fetchIdeaById,
      isOwner,
    }),
    [ideas, myIdeas, loading, refreshAll, refreshMine, createIdea, updateIdea, deleteIdea, fetchIdeaById, isOwner]
  );

  return <IdeasContext.Provider value={value}>{children}</IdeasContext.Provider>;
}

export function useIdeas() {
  const context = useContext(IdeasContext);
  if (!context) throw new Error("useIdeas deve ser usado dentro de IdeasProvider");
  return context;
}
