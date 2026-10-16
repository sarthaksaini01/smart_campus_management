import { useState } from 'react';
import { api } from '../../api';
import ProgressBar from '../../components/ProgressBar';
import StatCard from '../../components/StatCard';
import { useFetch } from '../../hooks/useFetch';
import { LEVEL_BADGE, LEVEL_LABEL, LEVEL_TONE } from '../../utils/risk';

export default function Insights() {
  const risk = useFetch('/api/student/risk');
  const [lang, setLang] = useState('en');
  const [advice, setAdvice] = useState(null); // { text, note }
  const [asking, setAsking] = useState(false);
  const [adviceError, setAdviceError] = useState('');

  async function getAdvice() {
    setAsking(true);
    setAdviceError('');
    try {
      const response = await api(`/api/student/insights?lang=${lang}`);
      setAdvice({ text: response.advice, note: response.adviceNote });
    } catch (err) {
      setAdviceError(err.message);
    } finally {
      setAsking(false);
    }
  }

  const r = risk.data;
  const hasData = r && r.riskLevel !== 'INSUFFICIENT_DATA';

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Risk and AI advice</h1>
          <p className="muted">See how you are doing and get simple tips to improve.</p>
        </div>
      </div>

      {risk.error && <div className="alert alert-error">{risk.error}</div>}
      {risk.loading && !r && <p className="muted">Loading...</p>}

      {r && (
        <section className="card">
          <div className="toolbar">
            <h2>Your academic risk</h2>
            <span className={`badge ${LEVEL_BADGE[r.riskLevel]}`}>{LEVEL_LABEL[r.riskLevel]}</span>
          </div>

          {!hasData && (
            <p className="muted">Your risk cannot be calculated yet. It needs some attendance or marks.</p>
          )}

          {hasData && (
            <>
              <div className="cell-bar">
                <ProgressBar value={r.riskScore} tone={LEVEL_TONE[r.riskLevel]} />
                <strong>{r.riskScore} / 100</strong>
              </div>

              <div className="stat-grid spaced">
                <StatCard
                  label="Attendance"
                  value={r.attendancePercentage === null ? '—' : `${r.attendancePercentage}%`}
                />
                <StatCard label="Average marks" value={r.averageMarks === null ? '—' : r.averageMarks} />
                <StatCard
                  label="Failed subjects"
                  value={r.failedSubjects}
                  tone={r.failedSubjects > 0 ? 'danger' : 'success'}
                />
              </div>

              <h3>Why this score</h3>
              {r.reasons.length === 0 ? (
                <p className="muted">No concerns. Keep it up.</p>
              ) : (
                <ul className="reasons">
                  {r.reasons.map((reason) => (
                    <li key={reason}>{reason}</li>
                  ))}
                </ul>
              )}
            </>
          )}

          <p className="muted small">
            The score is rule-based: it uses your attendance, average marks and failed subjects. It is a guide, not a
            prediction.
          </p>
        </section>
      )}

      {r && hasData && (
        <section className="card">
          <h2>AI study advice</h2>
          <p className="muted">
            Our AI reads your numbers (without your name or roll number) and suggests three simple tips.
          </p>

          <div className="inline-row">
            <select className="input select-inline" value={lang} onChange={(event) => setLang(event.target.value)}>
              <option value="en">English</option>
              <option value="hinglish">Hinglish</option>
            </select>
            <button className="btn btn-primary" onClick={getAdvice} disabled={asking}>
              {asking ? 'Thinking...' : advice ? 'Get new advice' : 'Get AI advice'}
            </button>
          </div>

          {adviceError && <div className="alert alert-error spaced-top">{adviceError}</div>}

          {advice && advice.text && (
            <>
              <div className="advice">{advice.text}</div>
              <p className="muted small">AI-generated suggestions. Please check important decisions with your faculty advisor.</p>
            </>
          )}
          {advice && !advice.text && advice.note && <div className="alert alert-warn spaced-top">{advice.note}</div>}
        </section>
      )}
    </>
  );
}