import { useNavigate } from "react-router-dom";
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { cn } from "../lib/utils";

type IdeaCardProps = {
  id: string;
  title: string;
  description: string;
  createdAt?: string;
  className?: string;
  onEdit?: () => void | Promise<void>;
  onDelete?: () => void | Promise<void>;
  showActions?: boolean;
  isOwner?: boolean;
};

export default function IdeaCard({
  id,
  title,
  description,
  createdAt,
  className,
  onEdit,
  onDelete,
  showActions = true,
  isOwner = false,
}: IdeaCardProps) {
  const navigate = useNavigate();

  return (
    <Card className={cn("transition hover:shadow-sm", className)}>
      <CardHeader className="space-y-2">
        <div className="flex items-start justify-between gap-3">
          <CardTitle className="text-base md:text-lg">{title}</CardTitle>
          <Badge variant="secondary" className="shrink-0">
            Idea
          </Badge>
        </div>

        {createdAt ? (
          <p className="text-xs text-muted">
            Criado em: {new Date(createdAt).toLocaleString()}
          </p>
        ) : (
          <p className="text-xs text-muted">—</p>
        )}
      </CardHeader>

      <CardContent className="space-y-4">
        <p className="text-sm text-muted line-clamp-3">{description}</p>

        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => navigate(`/view-idea/${id}`)}>
            Ver
          </Button>

          {showActions && isOwner && (
            <>
              <Button
                variant="secondary"
                onClick={() => (onEdit ? onEdit() : navigate(`/edit-idea/${id}`))}
              >
                Editar
              </Button>

              <Button variant="destructive" onClick={() => onDelete?.()}>
                Excluir
              </Button>
            </>
          )}
        </div>

        {showActions && !isOwner && (
          <p className="text-xs text-muted">Somente leitura (não é sua ideia).</p>
        )}
      </CardContent>
    </Card>
  );
}
