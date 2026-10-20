import { afterEach, describe, expect, it, vi } from 'vitest'
import { searchCafes } from './api'

afterEach(() => vi.restoreAllMocks())

function mockFetch(ok: boolean, body: unknown = { items: [], page: 0, size: 50, totalElements: 0 }) {
  const fn = vi.fn().mockResolvedValue({ ok, status: ok ? 200 : 500, json: async () => body })
  vi.stubGlobal('fetch', fn)
  return fn
}

describe('searchCafes', () => {
  it('encodes the query and bounds as URL parameters', async () => {
    const fn = mockFetch(true)
    await searchCafes({ q: ' 강남 & co ', bounds: { minLat: 1, maxLat: 2, minLng: 3, maxLng: 4 } })
    const url = new URL(fn.mock.calls[0][0] as string, 'http://x')
    expect(url.pathname).toBe('/api/cafes')
    expect(url.searchParams.get('q')).toBe('강남 & co')
    expect(url.searchParams.get('minLat')).toBe('1')
    expect(url.searchParams.get('maxLng')).toBe('4')
  })

  it('omits an empty query', async () => {
    const fn = mockFetch(true)
    await searchCafes({ q: '   ' })
    expect(new URL(fn.mock.calls[0][0] as string, 'http://x').searchParams.has('q')).toBe(false)
  })

  it('throws on a non-OK response', async () => {
    mockFetch(false)
    await expect(searchCafes({})).rejects.toThrow()
  })
})
