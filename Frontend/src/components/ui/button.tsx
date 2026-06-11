import * as React from "react";
import { cn } from "../../lib/utils";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "secondary" | "outline" | "danger" | "destructive";
  size?: "default" | "sm" | "lg" | "icon";
}

export function Button({
  className,
  variant = "default",
  size = "default",
  ...props
}: ButtonProps) {
  const isDestructive = variant === "destructive" || variant === "danger";

  return (
    <button
      className={cn(
        "btn ui-focus-ring",
        variant === "default" && "btn-primary",
        variant === "secondary" && "btn-secondary",
        variant === "outline" && "btn-outline",
        isDestructive && "btn-destructive",
        size === "sm" && "h-10 px-3",
        size === "lg" && "h-12 px-8",
        size === "icon" && "h-11 w-11 px-0",
        className
      )}
      {...props}
    />
  );
}
