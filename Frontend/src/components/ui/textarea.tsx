import * as React from "react";
import { cn } from "../../lib/utils";

export function Textarea(props: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      {...props}
      className={cn(
        "ui-input ui-focus-ring ui-surface ui-border w-full rounded-lg px-3 py-2 text-sm placeholder:opacity-70",
        props.className
      )}
    />
  );
}
