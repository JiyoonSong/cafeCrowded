import type { CrowdLevel } from './CrowdBadge'

export interface Cafe {
  id: number
  name: string
  address: string | null
  latitude: number
  longitude: number
  crowd: Crowd
}

export interface Crowd {
  level: CrowdLevel
  source: string | null
  observedAt: string | null
  reportCount: number
}

export class ReportError extends Error {
  status: number
  constructor(status: number) {
    super(`report failed (${status})`)
    this.status = status
  }
}

export async function submitReport(cafeId: number, level: Exclude<CrowdLevel, 'UNKNOWN'>): Promise<Crowd> {
  const res = await fetch(`/api/cafes/${encodeURIComponent(String(cafeId))}/reports`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ level }),
  })
  if (!res.ok) throw new ReportError(res.status)
  return (await res.json()) as Crowd
}

export interface CafePage {
  items: Cafe[]
  page: number
  size: number
  totalElements: number
}

export interface Bounds {
  minLat: number
  maxLat: number
  minLng: number
  maxLng: number
}

export interface CafeQuery {
  q?: string
  bounds?: Bounds
  size?: number
}

// Same-origin call to our own backend. No external API or secret is involved in the browser.
export async function searchCafes(query: CafeQuery, signal?: AbortSignal): Promise<CafePage> {
  const params = new URLSearchParams()
  const q = query.q?.trim()
  if (q) params.set('q', q)
  if (query.bounds) {
    for (const [k, v] of Object.entries(query.bounds)) params.set(k, String(v))
  }
  params.set('size', String(query.size ?? 50))

  const res = await fetch(`/api/cafes?${params}`, { signal, headers: { Accept: 'application/json' } })
  if (!res.ok) throw new Error(`cafes request failed (${res.status})`)
  return (await res.json()) as CafePage
}
