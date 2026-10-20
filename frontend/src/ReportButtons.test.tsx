import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ReportButtons } from './ReportButtons'

afterEach(() => vi.restoreAllMocks())

function mockFetch(status: number, body: unknown = {}) {
  const fn = vi.fn().mockResolvedValue({ ok: status < 400, status, json: async () => body })
  vi.stubGlobal('fetch', fn)
  return fn
}

describe('ReportButtons', () => {
  it('posts the chosen level and reports the new crowd', async () => {
    const crowd = { level: 'CROWDED', source: 'USER_REPORT', observedAt: '2026-10-18T00:00:00Z', reportCount: 1 }
    const fn = mockFetch(201, crowd)
    const onReported = vi.fn()
    render(<ReportButtons cafeId={7} onReported={onReported} />)
    fireEvent.click(screen.getByText('혼잡'))
    await waitFor(() => expect(onReported).toHaveBeenCalledWith(crowd))
    expect(fn.mock.calls[0][0]).toBe('/api/cafes/7/reports')
    expect(JSON.parse(fn.mock.calls[0][1].body)).toEqual({ level: 'CROWDED' })
  })

  it('shows a friendly message on 429', async () => {
    mockFetch(429)
    render(<ReportButtons cafeId={7} onReported={() => {}} />)
    fireEvent.click(screen.getByText('한산'))
    expect(await screen.findByRole('status')).toHaveTextContent('이미 제보하셨거나')
  })

  it('shows a generic message on other failures', async () => {
    mockFetch(500)
    render(<ReportButtons cafeId={7} onReported={() => {}} />)
    fireEvent.click(screen.getByText('보통'))
    expect(await screen.findByRole('status')).toHaveTextContent('보내지 못했습니다')
  })
})
