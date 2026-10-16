export default function StatCard({ label, value, hint, tone }) {
  return (
    <div className={tone ? `stat stat-${tone}` : 'stat'}>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      {hint && <div className="stat-hint">{hint}</div>}
    </div>
  );
}