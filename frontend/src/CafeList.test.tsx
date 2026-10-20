import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { Cafe } from './api'
import { CafeList } from './CafeList'

const cafe: Cafe = {
  id: 1,
  name: '<img src=x onerror=alert(1)>',
  address: '서울',
  latitude: 37.5,
  longitude: 127,
  crowd: { level: 'UNKNOWN', source: null, observedAt: null, reportCount: 0 },
}

describe('CafeList', () => {
  it('renders names as text, never as HTML, and shows 정보 없음', () => {
    const { container } = render(
      <CafeList cafes={[cafe]} status="ready" searching={false} selectedId={null} onSelect={() => {}} />,
    )
    expect(container.querySelector('img')).toBeNull()
    expect(screen.getByText('<img src=x onerror=alert(1)>')).toBeInTheDocument()
    expect(screen.getByText('정보 없음')).toBeInTheDocument()
  })

  it('calls onSelect when a cafe is clicked', () => {
    const onSelect = vi.fn()
    render(<CafeList cafes={[cafe]} status="ready" searching={false} selectedId={null} onSelect={onSelect} />)
    fireEvent.click(screen.getByRole('button'))
    expect(onSelect).toHaveBeenCalledWith(cafe)
  })

  it('shows distinct empty messages', () => {
    const { rerender } = render(
      <CafeList cafes={[]} status="ready" searching={true} selectedId={null} onSelect={() => {}} />,
    )
    expect(screen.getByText('검색 결과가 없습니다.')).toBeInTheDocument()
    rerender(<CafeList cafes={[]} status="ready" searching={false} selectedId={null} onSelect={() => {}} />)
    expect(screen.getByText('이 지역에 등록된 카페 정보가 없습니다.')).toBeInTheDocument()
  })

  it('shows a generic error without details', () => {
    render(<CafeList cafes={[]} status="error" searching={false} selectedId={null} onSelect={() => {}} />)
    expect(screen.getByRole('alert')).toHaveTextContent('불러오지 못했습니다')
  })
})
