import React, { useState, useEffect } from 'react';
import { tasksAPI, appointmentsAPI } from '../services/api';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { CheckSquare, CalendarDays, TrendingUp, Clock, ArrowRight, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';

function Dashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalTasks: 0,
        pendingTasks: 0,
        completedTasks: 0,
        upcomingAppointments: 0,
        recentTasks: [],
        nextAppointment: null,
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const [tasksRes, appointmentsRes] = await Promise.all([
                    tasksAPI.getAll(),
                    appointmentsAPI.getAll()
                ]);

                const tasks = tasksRes.data;
                const appointments = appointmentsRes.data;

                const pendingTasks = tasks.filter(t => t.status === 'TODO');
                const completedTasks = tasks.filter(t => t.status === 'DONE');

                // Sort appointments by date and find next upcoming
                const now = new Date();
                const upcoming = appointments
                    .filter(a => new Date(a.date) > now)
                    .sort((a, b) => new Date(a.date) - new Date(b.date));

                setStats({
                    totalTasks: tasks.length,
                    pendingTasks: pendingTasks.length,
                    completedTasks: completedTasks.length,
                    upcomingAppointments: upcoming.length,
                    recentTasks: tasks.slice(0, 5), // Just take first 5 for now, ideally sort by date if available
                    nextAppointment: upcoming.length > 0 ? upcoming[0] : null,
                });
            } catch (err) {
                console.error("Failed to load dashboard data", err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-full">
                <Loader2 className="h-8 w-8 animate-spin text-primary-500" />
            </div>
        );
    }

    const StatCard = ({ title, value, icon: Icon, color, description }) => (
        <Card>
            <CardContent className="p-6">
                <div className="flex items-center justify-between space-y-0 pb-2">
                    <p className="text-sm font-medium text-slate-500">{title}</p>
                    <Icon className={`h-4 w-4 text-${color}-500`} />
                </div>
                <div className="text-2xl font-bold">{value}</div>
                <p className="text-xs text-slate-500 mt-1">{description}</p>
            </CardContent>
        </Card>
    );

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
                <p className="text-slate-500">Overview of your activity and upcoming schedule.</p>
            </div>

            {/* Stats Grid */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <StatCard
                    title="Total Tasks"
                    value={stats.totalTasks}
                    icon={CheckSquare}
                    color="blue"
                    description={`${stats.completedTasks} completed`}
                />
                <StatCard
                    title="Pending Tasks"
                    value={stats.pendingTasks}
                    icon={Clock}
                    color="orange"
                    description="Tasks waiting for action"
                />
                <StatCard
                    title="Upcoming Meetings"
                    value={stats.upcomingAppointments}
                    icon={CalendarDays}
                    color="indigo"
                    description="Scheduled appointments"
                />
                <StatCard
                    title="Completion Rate"
                    value={`${stats.totalTasks ? Math.round((stats.completedTasks / stats.totalTasks) * 100) : 0}%`}
                    icon={TrendingUp}
                    color="green"
                    description="Task completion progress"
                />
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
                {/* Recent Tasks */}
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Recent Tasks</CardTitle>
                    </CardHeader>
                    <CardContent>
                        {stats.recentTasks.length === 0 ? (
                            <p className="text-sm text-slate-500">No tasks created yet.</p>
                        ) : (
                            <div className="space-y-4">
                                {stats.recentTasks.map(task => (
                                    <div key={task.id} className="flex items-center justify-between border-b border-slate-100 pb-2 last:border-0 last:pb-0">
                                        <div className="flex items-center gap-3">
                                            <div className={`h-2 w-2 rounded-full ${task.status === 'DONE' ? 'bg-green-500' : 'bg-orange-500'}`} />
                                            <div className="space-y-1">
                                                <p className="text-sm font-medium leading-none">{task.title}</p>
                                                <p className="text-xs text-slate-500 line-clamp-1">{task.description || "No description"}</p>
                                            </div>
                                        </div>
                                        <Badge variant={task.status === 'DONE' ? 'success' : 'secondary'}>
                                            {task.status}
                                        </Badge>
                                    </div>
                                ))}
                            </div>
                        )}
                        <div className="mt-4">
                            <Link to="/tasks">
                                <Button variant="ghost" size="sm" className="w-full text-slate-500">
                                    View all tasks <ArrowRight className="ml-2 h-4 w-4" />
                                </Button>
                            </Link>
                        </div>
                    </CardContent>
                </Card>

                {/* Next Appointment & Actions */}
                <div className="col-span-3 space-y-4">
                    <Card className="bg-primary-600 text-white border-primary-600">
                        <CardHeader>
                            <CardTitle className="text-white">Next Appointment</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {stats.nextAppointment ? (
                                <div className="space-y-4">
                                    <div>
                                        <h3 className="text-2xl font-bold">{new Date(stats.nextAppointment.date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</h3>
                                        <p className="text-primary-100">{new Date(stats.nextAppointment.date).toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' })}</p>
                                    </div>
                                    <div>
                                        <p className="font-medium text-lg">{stats.nextAppointment.subject}</p>
                                    </div>
                                </div>
                            ) : (
                                <div className="py-4">
                                    <p className="text-primary-100">No upcoming appointments scheduled.</p>
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>Quick Actions</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            <Link to="/tasks" className="block">
                                <Button variant="outline" className="w-full justify-start">
                                    <Plus className="mr-2 h-4 w-4" /> Create New Task
                                </Button>
                            </Link>
                            <Link to="/appointments" className="block">
                                <Button variant="outline" className="w-full justify-start">
                                    <CalendarDays className="mr-2 h-4 w-4" /> Schedule Meeting
                                </Button>
                            </Link>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}

// Icon helper for Quick Actions
function Plus({ className }) {
    return (
        <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14" />
            <path d="M12 5v14" />
        </svg>
    );
}

export default Dashboard;
