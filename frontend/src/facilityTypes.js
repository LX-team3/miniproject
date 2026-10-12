// DB의 type 값 → 화면 표시 이름·색.
// 실제 type 값이 다르면 여기만 고치면 됩니다. 목록에 없는 값은 원래 값 그대로, 남는 색으로 표시돼요.
export const TYPE_INFO = {
  camp: { label: '캠핑장', color: '#0d9488' },
  er: { label: '응급실', color: '#e5484d' },
  fire: { label: '소방서·119', color: '#f76b15' },
  police: { label: '지구대·파출소', color: '#3e63dd' },
  store: { label: '편의점', color: '#d6409f' },
  toilet: { label: '공중화장실', color: '#8e4ec6' },
  light: { label: '보안등', color: '#e2b203' },
  cctv: { label: 'CCTV', color: '#5b6b7a' },
  house: { label: '주택', color: '#ad7f58' },
}

// 처음 화면에서 꺼둘 종류 (개수가 많아 지도를 덮는 것)
export const DEFAULT_HIDDEN = ['house', 'er', 'fire', 'police', 'store', 'toilet', 'light', 'cctv']

const SPARE_COLORS = ['#12a594', '#ad7f58', '#5b5bd6', '#978365', '#0090ff', '#e93d82']

export function typeInfo(type, index = 0) {
  return (
    TYPE_INFO[type] ?? {
      label: type ?? '기타',
      color: SPARE_COLORS[index % SPARE_COLORS.length],
    }
  )
}

// ── 마커 아이콘 ──
// 종류별 흰색 그림(SVG path). 24×24 칸 기준으로 그림. 바꾸고 싶으면 이 path만 고치면 됨.
// stroke = 선으로 그림, fill = 면으로 채움
// ※ markerSvg의 <g>에 fill="none"이 있어서 Lucide path는 그대로 붙여넣으면 됨 (속이 배경색으로 보임)
//   면을 흰색으로 채우고 싶은 아이콘만 fill="#fff"를 직접 적기 (예: er 십자)
export const TYPE_ICON = {
  // 텐트 (https://lucide.dev/icons/tent)
  camp: '<path d="M3.5 21 14 3"/><path d="M20.5 21 10 3"/><path d="M15.5 21 12 15l-3.5 6"/><path d="M2 21h20"/>',
  // 십자(병원)
  er: '<path d="M10 5h4v5h5v4h-5v5h-4v-5H5v-4h5V5Z" stroke="none" fill="#fff"/>',
  // 소방 (https://lucide.dev/icons/fire-extinguisher)
  fire: '<path d="M15 6.5V3a1 1 0 0 0-1-1h-2a1 1 0 0 0-1 1v3.5"/><path d="M9 18h8"/><path d="M18 3h-3"/><path d="M11 3a6 6 0 0 0-6 6v11"/><path d="M5 13h4"/><path d="M17 10a4 4 0 0 0-8 0v10a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2Z"/>',
  // 경찰서 (https://lucide.dev/icons/siren)
  police: '<path d="M7 18v-6a5 5 0 1 1 10 0v6"/><path d="M5 21a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-1a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2z"/><path d="M21 12h1"/><path d="M18.5 4.5 18 5"/><path d="M2 12h1"/><path d="M12 2v1"/><path d="m4.929 4.929.707.707"/><path d="M12 12v6"/>',
  // 편의점 (https://lucide.dev/icons/shopping-cart)
  store: '<path d="m2.05 2.05 1.099-.028a1 1 0 0 1 1.008.815l2.69 14.347A1 1 0 0 0 7.83 18H18"/><path d="M4.563 5h16.435a1 1 0 0 1 .981 1.204l-1.026 6.226A2 2 0 0 1 18.962 14H6.25"/><circle cx="18" cy="20" r="2"/><circle cx="8" cy="20" r="2"/>',
  // 공중화장실 (https://lucide.dev/icons/toilet)
  toilet: '<path d="M7 12h13a1 1 0 0 1 1 1 5 5 0 0 1-5 5h-.598a.5.5 0 0 0-.424.765l1.544 2.47a.5.5 0 0 1-.424.765H5.402a.5.5 0 0 1-.424-.765L7 18"/><path d="M8 18a5 5 0 0 1-5-5V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v8"/>',
  // 전구
  light: '<path d="M15 14c.2-1 .7-1.7 1.5-2.5 1-.9 1.5-2.2 1.5-3.5A6 6 0 0 0 6 8c0 1 .2 2.2 1.5 3.5.7.7 1.3 1.5 1.5 2.5"/><path d="M9 18h6"/><path d="M10 22h4"/>',
  // 카메라
  cctv: '<rect x="4" y="8" width="11" height="7" rx="1.5" fill="none"/><path d="m15 10 5-2v7l-5-2" fill="none"/>',
  // 집
  house: '<path d="M4 11 12 5l8 6" fill="none"/><path d="M6 10v9h12v-9" fill="none"/>',
}

// 핀 모양 마커(SVG → data URI). 위는 동그라미 + 아이콘, 아래는 뾰족한 꼬리
// color: 배경색, type: 시설 종류(아이콘 선택)
export const MARKER_W = 30
export const MARKER_H = 38
export function markerSvg(color, type) {
  const icon = TYPE_ICON[type] ?? '<circle cx="12" cy="12" r="3" fill="#fff" stroke="none"/>'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${MARKER_W}" height="${MARKER_H}" viewBox="0 0 30 38">
<path d="M15 37C15 37 2 24 2 15a13 13 0 0 1 26 0c0 9-13 22-13 22Z" fill="${color}" stroke="#fff" stroke-width="2"/>
<g transform="translate(5 5) scale(0.8333)" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${icon}</g>
</svg>`
  return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg)
}

// 안심 등급 색 (ScoreService의 등급 이름과 맞춤)
export const GRADE_COLOR = {
  안심: '#18794e',
  보통: '#ad5700',
  주의: '#cd2b31',
}
