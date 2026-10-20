import type { Cafe } from './api'
import { CrowdBadge } from './CrowdBadge'

interface Props {
  cafes: Cafe[]
  status: 'loading' | 'error' | 'ready'
  searching: boolean
  selectedId: number | null
  onSelect: (cafe: Cafe) => void
}

export function CafeList({ cafes, status, searching, selectedId, onSelect }: Props) {
  if (status === 'error') return <p role="alert">카페 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</p>
  if (status === 'loading' && cafes.length === 0) return <p>불러오는 중…</p>
  if (cafes.length === 0) {
    return <p>{searching ? '검색 결과가 없습니다.' : '이 지역에 등록된 카페 정보가 없습니다.'}</p>
  }
  return (
    <ul className="cafe-list">
      {cafes.map((c) => (
        <li key={c.id}>
          <button className={c.id === selectedId ? 'selected' : ''} onClick={() => onSelect(c)}>
            <strong>{c.name}</strong>
            {c.address && <span className="addr">{c.address}</span>}
            <CrowdBadge level={c.crowd.level} source={c.crowd.source} observedAt={c.crowd.observedAt}
              reportCount={c.crowd.reportCount} />
          </button>
        </li>
      ))}
    </ul>
  )
}
