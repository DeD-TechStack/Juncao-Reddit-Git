import * as React from "react";
import { cn } from "../../lib/utils";

export function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={cn(
        "ui-input ui-focus-ring ui-surface ui-border h-11 w-full rounded-lg px-3 text-sm placeholder:opacity-70",
        props.className
      )}
    />
  );
}
