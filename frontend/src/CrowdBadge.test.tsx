import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { CrowdBadge } from './CrowdBadge'

describe('CrowdBadge', () => {
  it('shows "정보 없음" when level is unknown, without source/time', () => {
    render(<CrowdBadge level="UNKNOWN" source="user-report" observedAt="2026-10-18T00:00:00Z" />)
    expect(screen.getByText('정보 없음')).toBeInTheDocument()
    expect(screen.queryByText(/user-report/)).toBeNull()
  })

  it('shows level with source when known', () => {
    render(<CrowdBadge level="CROWDED" source="user-report" observedAt="2026-10-18T00:00:00Z" />)
    expect(screen.getByText(/혼잡/)).toBeInTheDocument()
    expect(screen.getByText(/user-report/)).toBeInTheDocument()
  })
})
