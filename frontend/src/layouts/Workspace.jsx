import { NavLink, Outlet, Link } from "react-router-dom";
import { useAuth, officerRole } from "../context/AuthContext";
export default function Workspace() {
  const { user, logout } = useAuth(),
    officer = officerRole(user.role);
  const links = officer
    ? [
        ["/officer", "Overview", "◫"],
        ["/officer/kyc", "KYC review", "▣"],
        ["/officer/loans", "Loan queue", "↗"],
      ]
    : [
        ["/borrower", "Overview", "◫"],
        ["/borrower/profile", "My profile", "○"],
        ["/borrower/kyc", "My documents", "▣"],
        ["/borrower/loans", "My applications", "↗"],
        ["/borrower/apply", "Apply for a loan", "＋"],
      ];
  return (
    <div className="workspace">
      <aside className="sidebar">
        <Link to={officer ? "/officer" : "/borrower"} className="brand">
          <span className="brand-mark">m.</span> microloan
          <span className="brand-dot">●</span>
        </Link>
        <div className="workspace-label">
          {officer ? "OFFICER WORKSPACE" : "BORROWER WORKSPACE"}
        </div>
        <nav aria-label="Main navigation">
          {links.map(([to, label, icon]) => (
            <NavLink
              key={to}
              to={to}
              end={to === "/borrower" || to === "/officer"}
            >
              <span>{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-note">
          <span>SMALL LOANS. BIG POSSIBILITIES.</span>
          <p>
            Every next step starts
            <br />
            with a little support.
          </p>
          <div className="note-art">↗</div>
        </div>
        <div className="account">
          <div className="avatar">{user.fullName?.slice(0, 1)}</div>
          <div>
            <strong>{user.fullName}</strong>
            <small>{user.role.replaceAll("_", " ")}</small>
          </div>
          <button className="text-button" onClick={logout}>
            Log out
          </button>
        </div>
      </aside>
      <div className="main-shell">
        <div className="topbar">
          <span>Microloan lifecycle management</span>
          <span className="checkpoint">● First 50% checkpoint</span>
        </div>
        <main>
          <Outlet />
        </main>
        <footer>
          Microloan · A clearer path to responsible borrowing{" "}
          <span>Academic demonstration</span>
        </footer>
      </div>
    </div>
  );
}
