import React from 'react';
import { cn } from '../../lib/utils';
import { X } from 'lucide-react';

const Modal = ({ isOpen, onClose, title, children, className }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className={cn(
                "relative z-50 grid w-full max-w-lg gap-4 rounded-lg bg-white p-6 shadow-lg duration-200 animate-slide-up sm:rounded-xl",
                className
            )}>
                <div className="flex flex-col space-y-1.5 text-center sm:text-left">
                    {title && (
                        <h2 className="text-lg font-semibold leading-none tracking-tight">
                            {title}
                        </h2>
                    )}
                    <button
                        onClick={onClose}
                        className="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-white transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:pointer-events-none data-[state=open]:bg-slate-100 data-[state=open]:text-slate-500"
                    >
                        <X className="h-4 w-4" />
                        <span className="sr-only">Close</span>
                    </button>
                </div>

                {children}
            </div>
        </div>
    );
};

// Subcomponents for structure
const ModalFooter = ({ className, ...props }) => (
    <div
        className={cn(
            "flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2",
            className
        )}
        {...props}
    />
);

export { Modal, ModalFooter };
