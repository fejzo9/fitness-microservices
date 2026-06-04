const BARLOW = { fontFamily: "'Barlow Condensed', sans-serif" };

export function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-card border border-border rounded-lg w-full max-w-lg shadow-xl animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between p-5 border-b border-border">
          <h3 className="text-base font-semibold text-foreground" style={BARLOW}>{title}</h3>
          <button type="button" onClick={onClose} className="text-muted-foreground hover:text-foreground text-xl leading-none cursor-pointer">&times;</button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}
