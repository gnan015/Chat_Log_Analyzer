const input = document.querySelector('#chatInput');
const emptyState = document.querySelector('#emptyState');
const results = document.querySelector('#results');
const rows = document.querySelector('#resultRows');
const metrics = document.querySelector('#metrics');
const fileInput = document.querySelector('#fileInput');
const fileStatus = document.querySelector('#fileStatus');
const alertsOnly = document.querySelector('#alertsOnly');
const chatSummary = document.querySelector('#chatSummary');
let latestResults = [];

const sample = `[09:00] Alice: Good morning team
[09:01] Bob: Good morning Alice
[09:02] Alice: Please share your update
[09:03] Bob: Working on the dashboard
[09:04] Bob: Working on the dashboard`;

document.querySelector('#sampleButton').addEventListener('click', () => { input.value = sample; input.focus(); });
fileInput.addEventListener('change', () => {
  const file = fileInput.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => { input.value = reader.result; fileStatus.textContent = `Loaded ${file.name}. Select Analyze chat to process it.`; };
  reader.onerror = () => { fileStatus.textContent = 'Unable to read this file. Please choose a valid text file.'; };
  reader.readAsText(file);
});
document.querySelector('#clearButton').addEventListener('click', () => {
  input.value = ''; fileInput.value = ''; fileStatus.textContent = 'Supported: WhatsApp exports and [HH:MM] Name: Message logs.'; rows.innerHTML = ''; metrics.innerHTML = ''; chatSummary.textContent = ''; results.classList.add('d-none'); emptyState.classList.remove('d-none');
});
document.querySelector('#analyzeButton').addEventListener('click', analyze);
alertsOnly.addEventListener('change', () => render(latestResults));

async function analyze() {
  const button = document.querySelector('#analyzeButton');
  if (!input.value.trim()) return alert('Please enter at least one chat message.');
  button.disabled = true; button.textContent = 'Analyzing...';
  try {
    const response = await fetch('/api/analyze', { method: 'POST', headers: { 'Content-Type': 'text/plain' }, body: input.value });
    if (!response.ok) throw new Error('The analysis request failed.');
    render(await response.json());
  } catch (error) { alert(error.message); }
  finally { button.disabled = false; button.textContent = 'Analyze chat'; }
}

function render(data) {
  latestResults = data;
  const valid = data.filter(item => !item.error);
  const spam = valid.filter(item => item.is_spam).length;
  const fraud = valid.filter(item => item.fraud_risk === 'HIGH' || item.fraud_risk === 'MEDIUM').length;
  const active = valid.length ? valid[valid.length - 1].most_active : '—';
  metrics.innerHTML = metric('Processed', data.length) + metric('Valid messages', valid.length) + metric('Spam flags', spam) + metric('Fraud warnings', fraud) + metric('Most active', active);
  chatSummary.textContent = makeSummary(valid, spam, fraud, active);
  const displayed = alertsOnly.checked ? data.filter(item => item.is_spam || (item.fraud_risk && item.fraud_risk !== 'NONE')) : data;
  rows.innerHTML = displayed.map(item => item.error
    ? `<tr><td colspan="7" class="text-danger"><strong>Invalid entry:</strong> ${escapeHtml(item.error)}</td></tr>`
    : `<tr class="${item.is_spam || item.fraud_risk !== 'NONE' ? 'alert-row' : ''}"><td class="fw-semibold">${escapeHtml(item.user)}</td><td><span class="message-preview" title="${escapeHtml(item.message)}">${escapeHtml(item.message)}</span></td><td>${item.message_count}</td><td>${item.word_count}</td><td>${item.is_spam ? '<span class="badge text-bg-warning">Detected</span>' : '<span class="text-secondary">No</span>'}</td><td>${fraudCell(item.fraud_risk, item.fraud_reasons)}</td><td>${escapeHtml(item.most_active)}</td></tr>`).join('');
  document.querySelector('#resultCount').textContent = `${displayed.length} of ${data.length}`;
  emptyState.classList.add('d-none'); results.classList.remove('d-none');
}

function metric(label, value) { return `<div class="col-sm-6 col-xl-3"><div class="card metric-card shadow-sm border-0"><div class="card-body py-3"><div class="small text-secondary">${label}</div><div class="metric-value text-truncate">${escapeHtml(String(value))}</div></div></div></div>`; }
function fraudCell(risk, reasons) { if (risk === 'NONE') return '<span class="text-secondary">None</span>'; const color = risk === 'HIGH' ? 'danger' : risk === 'MEDIUM' ? 'warning' : 'secondary'; return `<span class="badge text-bg-${color}">${escapeHtml(risk)}</span><div class="small text-secondary mt-1">${escapeHtml(reasons)}</div>`; }
function makeSummary(valid, spam, fraud, active) {
  if (!valid.length) return 'No valid chat messages were available to summarize.';
  const users = [...new Set(valid.map(item => item.user))];
  const totalWords = valid.reduce((total, item) => total + item.word_count, 0);
  let text = `This chat contains ${valid.length} messages from ${users.length} participant${users.length === 1 ? '' : 's'}: ${users.join(', ')}. `;
  text += `${active} is currently the most active user. The conversation contains ${totalWords} words. `;
  text += spam ? `${spam} repeated message${spam === 1 ? ' was' : 's were'} flagged as spam. ` : 'No repeated-message spam was detected. ';
  text += fraud ? `${fraud} message${fraud === 1 ? ' was' : 's were'} flagged for medium or high fraud risk; review the highlighted alert rows.` : 'No medium or high fraud-risk messages were detected.';
  return text;
}
function escapeHtml(value) { const div = document.createElement('div'); div.textContent = value; return div.innerHTML; }
