import * as React from "react";
import { cn } from "../../lib/utils";

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "secondary" | "outline" | "danger" | "destructive";
}

export function Badge({ className, variant = "default", ...props }: BadgeProps) {
  const isDestructive = variant === "destructive" || variant === "danger";

  return (
    <div
      className={cn(
        "badge",
        variant === "default" && "badge-primary",
        variant === "secondary" && "badge-secondary",
        variant === "outline" && "badge-outline",
        isDestructive && "badge-destructive",
        className
      )}
      {...props}
    />
  );
}
