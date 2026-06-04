export function Spinner({ size = 'md', className = '' }) {
  const sizeClasses = {
    sm: 'w-4 h-4 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
    xl: 'w-16 h-16 border-4',
  };

  return (
    <div className={`flex items-center justify-center ${className}`}>
      <div
        className={`${sizeClasses[size]} border-primary border-t-transparent rounded-full animate-spin`}
        style={{ borderTopColor: 'transparent' }}
      />
    </div>
  );
}

export function LoadingSpinner({ message, size = 'md' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-8">
      <Spinner size={size} />
      {message && <p className="text-sm text-muted-foreground">{message}</p>}
    </div>
  );
}
