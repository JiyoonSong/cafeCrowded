import { useState } from 'react'
import { ReportError, submitReport, type Crowd } from './api'

const OPTIONS = [
  { level: 'QUIET', label: '한산' },
  { level: 'NORMAL', label: '보통' },
  { level: 'CROWDED', label: '혼잡' },
] as const

interface Props {
  cafeId: number
  onReported: (crowd: Crowd) => void
}

export function ReportButtons({ cafeId, onReported }: Props) {
  const [message, setMessage] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function send(level: (typeof OPTIONS)[number]['level']) {
    setBusy(true)
    setMessage(null)
    try {
      onReported(await submitReport(cafeId, level))
      setMessage('제보해 주셔서 감사합니다.')
    } catch (e) {
      setMessage(
        e instanceof ReportError && e.status === 429
          ? '이미 제보하셨거나 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.'
          : '제보를 보내지 못했습니다. 잠시 후 다시 시도해 주세요.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="report">
      <div>지금 상황을 알려주세요</div>
      {OPTIONS.map((o) => (
        <button key={o.level} disabled={busy} onClick={() => send(o.level)}>
          {o.label}
        </button>
      ))}
      {message && <p role="status">{message}</p>}
    </div>
  )
}
