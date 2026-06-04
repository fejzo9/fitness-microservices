const inputCls = "w-full bg-secondary border border-border rounded px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all";

export function Input({ label, ...props }) {
  return (
    <div>
      {label && <label className="block text-xs text-muted-foreground mb-1 font-medium">{label}</label>}
      <input className={inputCls} {...props} />
    </div>
  );
}
