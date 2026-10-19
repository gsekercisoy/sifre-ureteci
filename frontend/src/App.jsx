import React, { useEffect, useState } from 'react';
import { api } from './api.js';

export default function App() {
  const [options, setOptions] = useState({
    length: 16, upper: true, lower: true, digits: true, symbols: true, excludeAmbiguous: false
  });
  const [current, setCurrent] = useState(null);
  const [entropy, setEntropy] = useState(null);
  const [batch, setBatchList] = useState([]);
  const [history, setHistory] = useState([]);
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);

  const generate = async (opts) => {
    setBusy(true);
    try {
      const d = await api.post('/passwords/generate', opts || options);
      setCurrent(d.password);
      setEntropy(d.entropyBits);
      setErr('');
      setHistory(h => [{ pw: d.password, bits: d.entropyBits, at: new Date().toLocaleTimeString('tr-TR') }, ...h].slice(0, 5));
    } catch (e) { setErr(e.message); }
    setBusy(false);
  };

  const generateBatch = async () => {
    setBusy(true);
    try {
      const list = await api.post('/passwords/generate/batch', { ...options, count: 5 });
      setBatchList(list.map(x => x.password));
      setErr('');
    } catch (e) { setErr(e.message); }
    setBusy(false);
  };

  // başlangıçta bir kez üret
  useEffect(() => { generate(); }, []);

  const set = (k, v) => {
    const next = { ...options, [k]: v };
    setOptions(next);
    if (k === 'length') generate(next);
  };

  const copy = (text) => {
    navigator.clipboard.writeText(text).then(() => {
      // görsel geri bildirim: mesaj göster
      setErr('');
    });
  };

  return (
    <div className="app">
      <h1>🔐 Şifre Üreteci v2</h1>
      <p className="lead">Şifreler <b>sunucuda SecureRandom</b> ile üretilir; istemci hiçbir karakter dizisi üretmez.</p>
      {err && <div className="err">{err}</div>}

      <div className="panel">
        <div className="out">
          <code id="pw">{current || '—'}</code>
          <button className="iconbtn" onClick={() => copy(current)} title="Kopyala">📋</button>
        </div>
        <div className="s-label">
          {entropy ? `Entropi: ~${entropy} bit` : '—'}
        </div>

        <div className="len-row">
          <span>Uzunluk</span>
          <input type="range" min="6" max="64" value={options.length} onChange={e => set('length', Number(e.target.value))} />
          <span className="len-val">{options.length}</span>
        </div>

        <label className="opt"><input type="checkbox" checked={options.upper} onChange={e => set('upper', e.target.checked)} /> Büyük harf (A-Z)</label>
        <label className="opt"><input type="checkbox" checked={options.lower} onChange={e => set('lower', e.target.checked)} /> Küçük harf (a-z)</label>
        <label className="opt"><input type="checkbox" checked={options.digits} onChange={e => set('digits', e.target.checked)} /> Rakam (0-9)</label>
        <label className="opt"><input type="checkbox" checked={options.symbols} onChange={e => set('symbols', e.target.checked)} /> Sembol (!@#…)</label>
        <label className="opt"><input type="checkbox" checked={options.excludeAmbiguous} onChange={e => set('excludeAmbiguous', e.target.checked)} /> Karışanları çıkar (0/O, 1/l/I…)</label>

        <div className="actions">
          <button className="btn primary" onClick={() => generate()} disabled={busy}>Üret</button>
          <button className="btn ghost" onClick={generateBatch} disabled={busy}>5'li üret</button>
        </div>
      </div>

      {batch.length > 0 && (
        <div className="panel">
          <h2>Toplu üretim (sunucu) transfüzyon: 5 adet</h2>
          <div className="batch">
            {batch.map((pw, i) => (
              <div className="item" key={i} onClick={() => copy(pw)}>
                <span>{pw}</span> <button className="iconbtn small">📋</button>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="panel">
        <h2>Son 5 şifre (tarayıcı oturumunda)</h2>
        <div className="batch">
          {history.map((h, i) => (
            <div className="item" key={i} onClick={() => copy(h.pw)}>
              <span>{h.pw}</span>
              <span className="muted">~{h.bits} bit · {h.at}</span>
            </div>
          ))}
          {history.length === 0 && <div className="muted" alt="Henüz yok" />}
        </div>
      </div>
    </div>
  );
}
