import { useEffect, useRef, useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth/AuthContext';

const MAX_HISTORY = 6;

const SUGGESTIONS = {
  STUDENT: [
    'Mera attendance kitna hai?',
    'Mere results aur CGPA batao',
    'Kya main academic risk me hun?',
    'Latest notices dikhao',
  ],
  FACULTY: [
    'Mere subjects kaunse hain?',
    'Mere subjects me kaun se students risk me hain?',
    'Latest notices dikhao',
  ],
  ADMIN: [
    'Give me a campus overview',
    'Which subjects have the lowest attendance?',
    'Who are the highest risk students?',
  ],
};

function inline(text) {
  return text.split(/(\*\*[^*]+\*\*)/g).map((part, i) =>
    part.length > 4 && part.startsWith('**') && part.endsWith('**') ? <strong key={i}>{part.slice(2, -2)}</strong> : part,
  );
}

// Shows the AI reply as safe React elements (never as raw HTML): paragraphs, "- " lists and **bold**.
function FormattedText({ text }) {
  const blocks = [];
  let bullets = [];

  function flush() {
    if (bullets.length > 0) {
      blocks.push({ type: 'list', items: bullets });
      bullets = [];
    }
  }

  text.split('\n').forEach((line) => {
    const match = line.match(/^\s*[-*•]\s+(.*)$/);
    if (match) {
      bullets.push(match[1]);
    } else {
      flush();
      if (line.trim() !== '') {
        blocks.push({ type: 'p', text: line });
      }
    }
  });
  flush();

  return blocks.map((block, i) =>
    block.type === 'list' ? (
      <ul key={i}>
        {block.items.map((item, j) => (
          <li key={j}>{inline(item)}</li>
        ))}
      </ul>
    ) : (
      <p key={i}>{inline(block.text)}</p>
    ),
  );
}

export default function ChatWidget() {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, sending, open]);

  async function send(text) {
    const message = text.trim();
    if (!message || sending) {
      return;
    }

    const history = messages
      .filter((m) => !m.error)
      .slice(-MAX_HISTORY)
      .map((m) => ({ role: m.role, text: m.text }));

    setMessages((current) => [...current, { role: 'user', text: message }]);
    setInput('');
    setSending(true);
    try {
      const response = await api('/api/chat', { method: 'POST', body: { message, history } });
      setMessages((current) => [...current, { role: 'assistant', text: response.reply }]);
    } catch (err) {
      setMessages((current) => [...current, { role: 'assistant', text: err.message, error: true }]);
    } finally {
      setSending(false);
    }
  }

  function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      send(input);
    }
  }

  if (!open) {
    return (
      <button className="chat-fab no-print" onClick={() => setOpen(true)} aria-label="Open AI assistant">
        Ask AI
      </button>
    );
  }

  const firstName = user.fullName.split(' ')[0];

  return (
    <div className="chat-panel no-print" role="dialog" aria-label="AI assistant">
      <div className="chat-header">
        <div>
          <strong>Campus assistant</strong>
          <small>Answers from your campus data</small>
        </div>
        <div className="chat-header-actions">
          <button className="chat-link" onClick={() => setMessages([])} disabled={messages.length === 0}>
            Clear
          </button>
          <button className="icon-btn" onClick={() => setOpen(false)} aria-label="Close">
            ×
          </button>
        </div>
      </div>

      <div className="chat-body">
        {messages.length === 0 && (
          <div className="chat-empty">
            <p>Hi {firstName}! Ask me about your campus data. You can write in English, Hindi or Hinglish.</p>
            <div className="chips">
              {(SUGGESTIONS[user.role] ?? []).map((suggestion) => (
                <button key={suggestion} className="chip" onClick={() => send(suggestion)}>
                  {suggestion}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((m, i) => (
          <div key={i} className={`bubble bubble-${m.role}${m.error ? ' bubble-error' : ''}`}>
            {m.role === 'assistant' && !m.error ? <FormattedText text={m.text} /> : m.text}
          </div>
        ))}

        {sending && <div className="bubble bubble-assistant typing">Thinking...</div>}
        <div ref={endRef} />
      </div>

      <div className="chat-input">
        <textarea
          rows={1}
          maxLength={500}
          value={input}
          placeholder="Type your question"
          onChange={(event) => setInput(event.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button className="btn btn-primary" onClick={() => send(input)} disabled={sending || !input.trim()}>
          Send
        </button>
      </div>
      <div className="chat-note">AI can make mistakes. It can only read data you are allowed to see and cannot change anything.</div>
    </div>
  );
}