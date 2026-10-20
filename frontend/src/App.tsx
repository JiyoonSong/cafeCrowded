import 'leaflet/dist/leaflet.css'
import { useEffect, useState } from 'react'
import { CircleMarker, MapContainer, Popup, TileLayer, useMap, useMapEvents } from 'react-leaflet'
import { searchCafes, type Bounds, type Cafe } from './api'
import { CafeList } from './CafeList'
import { CrowdBadge } from './CrowdBadge'
import { ReportButtons } from './ReportButtons'
import './App.css'

// Seoul City Hall as the initial view.
const SEOUL: [number, number] = [37.5665, 126.978]

function toBounds(map: L.Map): Bounds {
  const b = map.getBounds()
  return { minLat: b.getSouth(), maxLat: b.getNorth(), minLng: b.getWest(), maxLng: b.getEast() }
}

function BoundsWatcher({ onChange }: { onChange: (b: Bounds) => void }) {
  const map = useMap()
  useEffect(() => onChange(toBounds(map)), [map, onChange])
  useMapEvents({ moveend: () => onChange(toBounds(map)) })
  return null
}

function FlyTo({ cafe }: { cafe: Cafe | null }) {
  const map = useMap()
  useEffect(() => {
    if (cafe) map.flyTo([cafe.latitude, cafe.longitude], Math.max(map.getZoom(), 16))
  }, [cafe, map])
  return null
}

export default function App() {
  const [input, setInput] = useState('')
  const [q, setQ] = useState('')
  const [bounds, setBounds] = useState<Bounds | null>(null)
  const [result, setResult] = useState<{ key: string; cafes: Cafe[]; error: boolean }>({
    key: '',
    cafes: [],
    error: false,
  })
  const [selected, setSelected] = useState<Cafe | null>(null)

  useEffect(() => {
    const t = setTimeout(() => setQ(input.trim()), 300)
    return () => clearTimeout(t)
  }, [input])

  // A text search looks across all areas; otherwise show what is inside the visible map.
  const searchBounds = q ? undefined : (bounds ?? undefined)
  const key = q || !bounds ? `q:${q}` : `b:${JSON.stringify(bounds)}`
  const ready = q !== '' || bounds !== null

  useEffect(() => {
    if (!ready) return
    const ctrl = new AbortController()
    searchCafes({ q, bounds: searchBounds }, ctrl.signal)
      .then((page) => {
        setResult({ key, cafes: page.items, error: false })
        if (q && page.items.length > 0) setSelected(page.items[0])
      })
      .catch((e) => {
        if (e.name !== 'AbortError') setResult({ key, cafes: [], error: true })
      })
    return () => ctrl.abort()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const cafes = result.cafes
  const status = !ready || result.key !== key ? 'loading' : result.error ? 'error' : 'ready'

  return (
    <div className="app">
      <header>
        <h1>CafeCrowded</h1>
        <input
          type="search"
          placeholder="카페 이름 또는 지역 검색"
          maxLength={50}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          aria-label="카페 검색"
        />
      </header>
      <div className="body">
        <aside>
          <CafeList
            cafes={cafes}
            status={status}
            searching={q !== ''}
            selectedId={selected?.id ?? null}
            onSelect={setSelected}
          />
        </aside>
        <MapContainer center={SEOUL} zoom={14} className="map">
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <BoundsWatcher onChange={setBounds} />
          <FlyTo cafe={selected} />
          {cafes.map((c) => (
            <CircleMarker
              key={c.id}
              center={[c.latitude, c.longitude]}
              radius={c.id === selected?.id ? 11 : 8}
              eventHandlers={{ click: () => setSelected(c) }}
            >
              <Popup>
                <strong>{c.name}</strong>
                <br />
                {c.address}
                <br />
                <CrowdBadge
                  level={c.crowd.level}
                  source={c.crowd.source}
                  observedAt={c.crowd.observedAt}
                  reportCount={c.crowd.reportCount}
                />
                <ReportButtons
                  cafeId={c.id}
                  onReported={(crowd) =>
                    setResult((r) => ({ ...r, cafes: r.cafes.map((x) => (x.id === c.id ? { ...x, crowd } : x)) }))
                  }
                />
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>
    </div>
  )
}
