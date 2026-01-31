import React, { useState, useEffect } from 'react';
import { appointmentsAPI } from '../services/api';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Modal, ModalFooter } from '../components/ui/Modal';
import { CalendarDays, Plus, Pencil, Trash2, Clock, Loader2 } from 'lucide-react';

function Appointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingAppointment, setEditingAppointment] = useState(null);
  const [formData, setFormData] = useState({
    subject: '',
    date: '',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      setLoading(true);
      const response = await appointmentsAPI.getAll();
      setAppointments(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const openCreateModal = () => {
    setEditingAppointment(null);
    setFormData({
      subject: '',
      date: '',
    });
    setShowModal(true);
  };

  const openEditModal = (appointment) => {
    setEditingAppointment(appointment);
    const formattedDate = appointment.date ?
      new Date(appointment.date).toISOString().slice(0, 16) : '';
    setFormData({
      subject: appointment.subject,
      date: formattedDate,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingAppointment(null);
    setFormData({
      subject: '',
      date: '',
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      const appointmentData = {
        subject: formData.subject,
        date: formData.date,
      };

      if (editingAppointment) {
        await appointmentsAPI.update(editingAppointment.id, appointmentData);
      } else {
        await appointmentsAPI.create(appointmentData);
      }
      closeModal();
      fetchAppointments();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save appointment');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this appointment?')) {
      try {
        await appointmentsAPI.delete(id);
        fetchAppointments();
      } catch (err) {
        setError('Failed to delete appointment');
      }
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isUpcoming = (dateString) => {
    return new Date(dateString) > new Date();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <Loader2 className="h-8 w-8 animate-spin text-primary-500" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointments</h1>
          <p className="text-slate-500">Schedule and manage your upcoming meetings.</p>
        </div>
        <Button onClick={openCreateModal}>
          <Plus className="mr-2 h-4 w-4" /> New Appointment
        </Button>
      </div>

      {error && (
        <div className="p-4 rounded-md bg-red-50 text-red-500 text-sm">
          {error}
        </div>
      )}

      {appointments.length === 0 ? (
        <Card className="flex flex-col items-center justify-center py-12 text-center border-dashed">
          <div className="h-12 w-12 rounded-full bg-slate-100 flex items-center justify-center mb-4">
            <CalendarDays className="h-6 w-6 text-slate-400" />
          </div>
          <h3 className="text-lg font-medium text-slate-900">No appointments yet</h3>
          <p className="text-slate-500 max-w-sm mt-2 mb-6">
            Get organized by scheduling your first appointment.
          </p>
          <Button onClick={openCreateModal} variant="outline">
            Schedule Appointment
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {appointments.map(appointment => (
            <Card key={appointment.id} className="flex flex-col transition-shadow hover:shadow-md">
              <CardHeader className="pb-3">
                <div className="flex justify-between items-start gap-2">
                  <CardTitle className="text-lg font-semibold line-clamp-1">
                    {appointment.subject}
                  </CardTitle>
                  <Badge variant={isUpcoming(appointment.date) ? 'default' : 'secondary'}>
                    {isUpcoming(appointment.date) ? 'Upcoming' : 'Past'}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="flex-1 pb-3">
                <div className="flex items-center text-sm text-slate-500">
                  <Clock className="mr-2 h-4 w-4" />
                  {formatDate(appointment.date)}
                </div>
              </CardContent>
              <CardFooter className="pt-3 border-t border-slate-100 flex justify-end gap-1">
                <Button variant="ghost" size="icon" onClick={() => openEditModal(appointment)}>
                  <Pencil className="h-4 w-4 text-slate-500" />
                </Button>
                <Button variant="ghost" size="icon" onClick={() => handleDelete(appointment.id)}>
                  <Trash2 className="h-4 w-4 text-red-500" />
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={showModal}
        onClose={closeModal}
        title={editingAppointment ? 'Edit Appointment' : 'New Appointment'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <label htmlFor="subject" className="text-sm font-medium">Subject</label>
            <Input
              id="subject"
              name="subject"
              value={formData.subject}
              onChange={handleChange}
              placeholder="Meeting subject"
              required
            />
          </div>
          <div className="space-y-2">
            <label htmlFor="date" className="text-sm font-medium">Date & Time</label>
            <Input
              type="datetime-local"
              id="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              required
            />
          </div>

          <ModalFooter>
            <Button type="button" variant="ghost" onClick={closeModal}>
              Cancel
            </Button>
            <Button type="submit" isLoading={saving}>
              {editingAppointment ? 'Save Changes' : 'Schedule'}
            </Button>
          </ModalFooter>
        </form>
      </Modal>
    </div>
  );
}

export default Appointments;
