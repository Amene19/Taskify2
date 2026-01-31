import React from 'react';
import { cn } from '../../lib/utils';

const Badge = React.forwardRef(({ className, variant = "default", ...props }, ref) => {
    const variants = {
        default: "border-transparent bg-primary-600 text-white shadow hover:bg-primary-700",
        secondary: "border-transparent bg-slate-100 text-slate-900 hover:bg-slate-200/80",
        destructive: "border-transparent bg-red-500 text-white shadow hover:bg-red-600",
        outline: "text-slate-950 border-slate-200",
        success: "border-transparent bg-green-500 text-white shadow hover:bg-green-600",
        warning: "border-transparent bg-yellow-500 text-white shadow hover:bg-yellow-600",
    };

    return (
        <div
            ref={ref}
            className={cn(
                "inline-flex items-center rounded-md border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
                variants[variant],
                className
            )}
            {...props}
        />
    );
});

Badge.displayName = "Badge";

export { Badge };
