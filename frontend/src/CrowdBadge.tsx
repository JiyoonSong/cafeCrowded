export type CrowdLevel = 'QUIET' | 'NORMAL' | 'CROWDED' | 'UNKNOWN'

const LABELS: Record<CrowdLevel, string> = {
  QUIET: '한산',
  NORMAL: '보통',
  CROWDED: '혼잡',
  UNKNOWN: '정보 없음',
}

interface Props {
  level: CrowdLevel
  source?: string | null
  observedAt?: string | null
  reportCount?: number
}

// Renders text only (React escapes it). Source and time are shown whenever a level is known.
export function CrowdBadge({ level, source, observedAt, reportCount }: Props) {
  const known = level !== 'UNKNOWN'
  return (
    <span className={`crowd crowd-${level.toLowerCase()}`}>
      {LABELS[level]}
      {known && source && observedAt && (
        <small>
          {' '}
          · {source === 'USER_REPORT' ? '사용자 제보' : source}
          {reportCount ? ` ${reportCount}건` : ''} · 최근 {new Date(observedAt).toLocaleString('ko-KR')}
        </small>
      )}
    </span>
  )
}
