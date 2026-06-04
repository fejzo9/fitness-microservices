import { Outlet, Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from '../contexts/AuthContext';

const navItems = [
  { path: "/", label: "Dashboard" },
  { path: "/plan-treninga", label: "Plan treninga" },
  { path: "/biblioteka", label: "Biblioteka" },
  { path: "/ishrana", label: "Ishrana" },
  { path: "/napredak", label: "Napredak" },
  { path: "/profil", label: "Profil" },
];

export function Layout() {
  const location = useLocation();
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const isActive = (path) => location.pathname === path;
  const isAdmin = user?.roleName === 'ADMIN';
  const isTrener = user?.roleName === 'TRENER';

  // Zatvori sidebar pri navigaciji
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Spriječi scroll pozadine dok je sidebar otvoren
  useEffect(() => {
    if (sidebarOpen) {
      document.body.classList.add('overflow-hidden');
    } else {
      document.body.classList.remove('overflow-hidden');
    }
    return () => document.body.classList.remove('overflow-hidden');
  }, [sidebarOpen]);

  return (
    <div className="min-h-screen bg-background text-foreground dark flex flex-col">
      {/* Top Header */}
      <header className="bg-sidebar border-b border-sidebar-border px-4 md:px-6 py-3 flex-shrink-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            {/* Hamburger gumb — vidljiv samo ispod lg */}
            <button
              type="button"
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden flex items-center justify-center w-10 h-10 rounded text-foreground hover:bg-sidebar-accent transition-colors"
              aria-label="Otvori meni"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <rect y="3" width="20" height="2" rx="1"/>
                <rect y="9" width="20" height="2" rx="1"/>
                <rect y="15" width="20" height="2" rx="1"/>
              </svg>
            </button>
            <Link to="/" className="flex items-center gap-3">
              <h1
                className="text-base font-semibold text-foreground tracking-wide uppercase"
                style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.08em" }}
              >
                <span className="hidden sm:inline">Fitness i Trening Menadžer</span>
                <span className="sm:hidden">FT Menadžer</span>
              </h1>
            </Link>
          </div>
          <div className="flex items-center gap-2 md:gap-3">
            <span className="hidden sm:block text-sm text-muted-foreground truncate max-w-[120px]">{user?.username || 'Korisnik'}</span>
            <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary/40 flex items-center justify-center flex-shrink-0">
              <span className="text-primary text-xs font-bold">{user?.username?.substring(0, 2).toUpperCase() || 'K'}</span>
            </div>
            <button
              type="button"
              onClick={logout}
              className="text-xs text-muted-foreground hover:text-foreground transition-colors min-h-[44px] min-w-[44px] flex items-center justify-center"
            >
              Odjava
            </button>
          </div>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* Overlay pozadina — vidljiva samo na mobilnom kad je sidebar otvoren */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-black/50 z-30 lg:hidden"
            onClick={() => setSidebarOpen(false)}
            aria-hidden="true"
          />
        )}

        {/* Left Sidebar Navigation */}
        <aside className={`
          fixed lg:relative inset-y-0 left-0 z-40
          w-64 lg:w-52 bg-sidebar border-r border-sidebar-border flex-shrink-0
          flex flex-col
          transform transition-transform duration-200 ease-in-out
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0
        `}>
          {/* Zatvorni gumb unutar drawera — samo na mobilnom */}
          <div className="lg:hidden flex items-center justify-between px-4 py-3 border-b border-sidebar-border">
            <span
              className="text-sm font-semibold text-foreground uppercase tracking-wide"
              style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
            >
              Navigacija
            </span>
            <button
              type="button"
              onClick={() => setSidebarOpen(false)}
              className="flex items-center justify-center w-10 h-10 rounded text-muted-foreground hover:text-foreground hover:bg-sidebar-accent transition-colors"
              aria-label="Zatvori meni"
            >
              <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M2 2l12 12M14 2L2 14" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </button>
          </div>

          <nav className="p-3 space-y-0.5 flex-1 overflow-y-auto">
            {navItems.map(({ path, label }) => (
                <Link
                    key={path}
                    to={path}
                    className={`flex items-center px-3 py-3 lg:py-2.5 text-sm rounded transition-colors min-h-[44px] ${
                        isActive(path)
                            ? "bg-primary text-white font-medium"
                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    }`}
                >
                  {label}
                </Link>
            ))}
            {(isAdmin || isTrener) && (
                <Link
                    to="/trener-panel"
                    className={`flex items-center px-3 py-3 lg:py-2.5 text-sm rounded transition-colors min-h-[44px] ${
                        isActive("/trener-panel")
                            ? "bg-primary text-white font-medium"
                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    }`}
                >
                  Trener Panel
                </Link>
            )}
            {isAdmin && (
                <Link
                    to="/admin-panel"
                    className={`flex items-center px-3 py-3 lg:py-2.5 text-sm rounded transition-colors min-h-[44px] ${
                        isActive("/admin-panel")
                            ? "bg-primary text-white font-medium"
                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    }`}
                >
                  Admin Panel
                </Link>
            )}
          </nav>

          <div className="p-3 border-t border-sidebar-border">
            <div className="text-xs text-muted-foreground text-center">v1.0 · Student projekat</div>
          </div>
        </aside>

        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
