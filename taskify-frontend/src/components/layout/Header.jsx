import React from 'react';
import { Bell, User } from 'lucide-react';
import { Button } from '../ui/Button';

const Header = () => {
    const email = localStorage.getItem('userEmail') || 'User';

    return (
        <header className="h-16 border-b border-slate-200 bg-white px-8 flex items-center justify-between">
            {/* Left side (could be breadcrumbs later) */}
            <div className="flex items-center gap-4">
                {/* Placeholder for breadcrumbs or title */}
            </div>

            {/* Right side */}
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" className="relative text-slate-500">
                    <Bell className="h-5 w-5" />
                    <span className="absolute top-2 right-2 h-2 w-2 rounded-full bg-red-500 ring-2 ring-white" />
                </Button>

                <div className="flex items-center gap-3 pl-4 border-l border-slate-200">
                    <div className="text-right hidden sm:block">
                        <p className="text-sm font-medium text-slate-900">{email}</p>
                        <p className="text-xs text-slate-500">Free Plan</p>
                    </div>
                    <div className="h-9 w-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center">
                        <User className="h-5 w-5 text-slate-500" />
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;
