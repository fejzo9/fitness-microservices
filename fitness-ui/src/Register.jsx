import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

export function Register() {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    roleId: 2, // Default USER role for ordinary users
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error('Neuspješna registracija');
      }

      navigate('/login');
    } catch (err) {
      setError(err.message || 'Greška pri registraciji');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="bg-card border border-border rounded-lg p-8">
          {/* Logo and Title */}
          <div className="text-center mb-8">
            <div className="w-16 h-16 bg-primary rounded flex items-center justify-center mx-auto mb-4">
              <span 
                className="text-white text-2xl font-bold" 
                style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
              >
                FT
              </span>
            </div>
            <h1
              className="text-2xl font-bold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Fitness i Trening Menadžer
            </h1>
            <p className="text-sm text-muted-foreground mt-2">Kreirajte novi nalog</p>
          </div>

          {/* Register Form */}
          <form onSubmit={handleRegister} className="space-y-4">
            {error && (
              <div className="bg-destructive/10 border border-destructive text-destructive px-4 py-3 rounded text-sm">
                {error}
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-muted-foreground mb-2">
                  Ime
                </label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="w-full bg-secondary border border-border rounded px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Ime"
                  required
                />
              </div>

              <div>
                <label className="block text-sm text-muted-foreground mb-2">
                  Prezime
                </label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="w-full bg-secondary border border-border rounded px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Prezime"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm text-muted-foreground mb-2">
                Korisničko ime
              </label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="w-full bg-secondary border border-border rounded px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="Unesite korisničko ime"
                required
              />
            </div>

            <div>
              <label className="block text-sm text-muted-foreground mb-2">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="w-full bg-secondary border border-border rounded px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="Unesite email"
                required
              />
            </div>

            <div>
              <label className="block text-sm text-muted-foreground mb-2">
                Lozinka
              </label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full bg-secondary border border-border rounded px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="Unesite lozinku"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary text-white px-4 py-2 text-sm rounded font-medium hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Registracija...' : 'Registruj se'}
            </button>
          </form>

          {/* Login Link */}
          <div className="mt-6 text-center">
            <p className="text-sm text-muted-foreground">
              Već imate nalog?{' '}
              <Link to="/login" className="text-primary hover:underline font-medium">
                Prijavite se
              </Link>
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-6">
          <p className="text-xs text-muted-foreground">v1.0 · Student projekat</p>
        </div>
      </div>
    </div>
  );
}
