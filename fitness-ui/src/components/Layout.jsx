import { Outlet, Link, useLocation } from "react-router-dom";
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
  const isActive = (path) => location.pathname === path;
  const isAdmin = user?.roleName === 'ADMIN';
  const isTrener = user?.roleName === 'TRENER';
  return (
    <div className="min-h-screen bg-background text-foreground dark flex flex-col">
      {/* Top Header */}
      <header className="bg-sidebar border-b border-sidebar-border px-6 py-3 flex-shrink-0">
        <div className="flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3">
            <h1
              className="text-base font-semibold text-foreground tracking-wide uppercase"
              style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.08em" }}
            >
              Fitness i Trening Menadžer
            </h1>
          </Link>
          <div className="flex items-center gap-3">
            <span className="text-sm text-muted-foreground">{user?.username || 'Korisnik'}</span>
            <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary/40 flex items-center justify-center">
              <span className="text-primary text-xs font-bold">{user?.username?.substring(0, 2).toUpperCase() || 'K'}</span>
            </div>
            <button
              type="button"
              onClick={logout}
              className="text-xs text-muted-foreground hover:text-foreground transition-colors"
            >
              Odjava
            </button>
          </div>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar Navigation */}
        <aside className="w-52 bg-sidebar border-r border-sidebar-border flex-shrink-0 relative">
          <nav className="p-3 space-y-0.5">
            {navItems.map(({ path, label }) => (
                <Link
                    key={path}
                    to={path}
                    className={`flex items-center px-3 py-2.5 text-sm rounded transition-colors ${
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
                    className={`flex items-center px-3 py-2.5 text-sm rounded transition-colors ${
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
                    className={`flex items-center px-3 py-2.5 text-sm rounded transition-colors ${
                        isActive("/admin-panel")
                            ? "bg-primary text-white font-medium"
                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                    }`}
                >
                  Admin Panel
                </Link>
            )}
          </nav>

          <div className="absolute bottom-0 left-0 w-52 p-3 border-t border-sidebar-border">
            <div className="text-xs text-muted-foreground text-center">v1.0 · Student projekat</div>
          </div>
        </aside>

        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
