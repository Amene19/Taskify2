import React from 'react';
import { NavLink } from 'react-router-dom';
import { cn } from '../../lib/utils';
import {
    LayoutDashboard,
    CheckSquare,
    CalendarDays,
    LogOut,
    Settings
} from 'lucide-react';

const Sidebar = ({ className }) => {
    const navItems = [
        { icon: LayoutDashboard, label: 'Dashboard', href: '/dashboard' },
        { icon: CheckSquare, label: 'Tasks', href: '/tasks' },
        { icon: CalendarDays, label: 'Appointments', href: '/appointments' },
    ];

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userEmail');
        window.location.href = '/login';
    };

    return (
        <aside className={cn("flex flex-col h-screen w-64 bg-slate-900 text-white border-r border-slate-800", className)}>
            <div className="p-6">
                <h1 className="text-xl font-bold flex items-center gap-2">
                    <div className="h-8 w-8 rounded bg-primary-500 flex items-center justify-center">
                        <CheckSquare className="h-5 w-5 text-white" />
                    </div>
                    Taskify
                </h1>
            </div>

            <nav className="flex-1 px-4 space-y-2 py-4">
                {navItems.map((item) => (
                    <NavLink
                        key={item.href}
                        to={item.href}
                        onClick={(e) => item.disabled && e.preventDefault()}
                        className={({ isActive }) => cn(
                            "flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors",
                            isActive
                                ? "bg-primary-600 text-white"
                                : "text-slate-400 hover:bg-slate-800 hover:text-white",
                            item.disabled && "opacity-50 cursor-not-allowed"
                        )}
                    >
                        <item.icon className="h-5 w-5" />
                        {item.label}
                        {item.disabled && <span className="ml-auto text-xs bg-slate-800 px-2 py-0.5 rounded">Soon</span>}
                    </NavLink>
                ))}
            </nav>

            <div className="p-4 border-t border-slate-800">
                <button
                    onClick={handleLogout}
                    className="flex w-full items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 hover:bg-red-900/20 hover:text-red-400 transition-colors"
                >
                    <LogOut className="h-5 w-5" />
                    Sign Out
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
