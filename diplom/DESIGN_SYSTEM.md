# DiplomShop Design System

## 📊 Design Overview

**Product Type:** E-commerce SaaS with A/B Testing  
**Style:** Modern Minimalism with Glassmorphism accents  
**Audience:** Online shoppers, Product managers, A/B test managers  
**Tone:** Professional, Clean, Data-driven, Accessible

---

## 🎨 Color Palette

### Primary Colors
- **Primary (Action):** `#3B82F6` — Blue (trust, professional)
- **Primary Dark:** `#1E40AF` — Deep blue (buttons, links)
- **Primary Light:** `#DBEAFE` — Light blue (backgrounds, hovers)

### Semantic Colors
- **Success:** `#10B981` — Green
- **Warning:** `#F59E0B` — Amber
- **Error:** `#EF4444` — Red
- **Info:** `#06B6D4` — Cyan

### Neutral Scale
- **Gray-50:** `#F9FAFB` — Almost white
- **Gray-100:** `#F3F4F6` — Light backgrounds
- **Gray-200:** `#E5E7EB` — Borders, dividers
- **Gray-300:** `#D1D5DB` — Secondary borders
- **Gray-500:** `#6B7280` — Secondary text
- **Gray-700:** `#374151` — Primary text
- **Gray-900:** `#111827` — Darkest text

### Dark Mode (inverted)
- **Surface:** `#1F2937` — Dark bg
- **Surface Secondary:** `#111827` — Darker sections
- **Text Primary:** `#F9FAFB` — Light text
- **Text Secondary:** `#D1D5DB` — Muted text

---

## 📝 Typography

### Font Stack
**Primary (Headings):** `'Inter', 'Segoe UI', system-ui, sans-serif`  
**Secondary (Body):** `'Inter', 'Segoe UI', system-ui, sans-serif`  

### Type Scale
| Level | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| **Display** | 2.5rem (40px) | 800 | 1.2 | Page hero titles |
| **H1** | 2rem (32px) | 700 | 1.3 | Section titles |
| **H2** | 1.5rem (24px) | 600 | 1.4 | Subsection titles |
| **H3** | 1.25rem (20px) | 600 | 1.5 | Card titles |
| **Base** | 1rem (16px) | 400 | 1.6 | Body text, inputs |
| **Small** | 0.875rem (14px) | 400 | 1.6 | Labels, captions |
| **Tiny** | 0.75rem (12px) | 500 | 1.5 | Badges, hints |

### Best Practices
- Minimum 16px body text (no auto-zoom on iOS)
- Line length 50-75 characters per line
- Line height 1.5-1.75 for readability
- Use 600-700 weight for hierarchy (not just size)

---

## 🎯 Spacing System (4dp / 8dp)

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4px | Tight spacing within components |
| `sm` | 8px | Padding, small gaps |
| `md` | 16px | Regular padding, component spacing |
| `lg` | 24px | Section spacing |
| `xl` | 32px | Large section spacing |
| `2xl` | 48px | Page-level spacing |

---

## 🎨 Glassmorphism Style Rules

### Card Styling
```css
background: rgba(255, 255, 255, 0.95);
backdrop-filter: blur(10px);
border: 1px solid rgba(255, 255, 255, 0.6);
border-radius: 0.75rem;
```

### Dark Mode Cards
```css
background: rgba(31, 41, 55, 0.90);
backdrop-filter: blur(10px);
border: 1px solid rgba(107, 114, 128, 0.3);
```

### Button Style
- **Primary:** Solid blue with subtle shadow, smooth hover (0.1s)
- **Secondary:** Transparent with border, glassmorphic feel
- **Hover:** Slight scale (1.02), opacity 0.9

---

## ✨ Effects & Shadows

### Shadow Scale
| Level | Value |
|-------|-------|
| **xs** | `0 1px 2px rgba(0,0,0,0.05)` |
| **sm** | `0 1px 3px rgba(0,0,0,0.1)` |
| **md** | `0 4px 6px rgba(0,0,0,0.1)` |
| **lg** | `0 10px 15px rgba(0,0,0,0.1)` |
| **xl** | `0 20px 25px rgba(0,0,0,0.15)` |

### Transitions
- **Micro (fast):** 150ms (button hover, opacity)
- **Normal:** 200-250ms (card scale, color fade)
- **Slow:** 300-400ms (modal entrance, complex layouts)

### Easing
- **Entrance:** `cubic-bezier(0.34, 1.56, 0.64, 1)` — spring-like
- **Exit:** `cubic-bezier(0.25, 0.46, 0.45, 0.94)` — ease-out
- **Interactive:** `cubic-bezier(0.4, 0, 0.2, 1)` — material

---

## 🧩 Component Patterns

### Buttons
- **Primary:** Blue solid, 44px height, rounded 0.5rem
- **Secondary:** Border + transparent, same height
- **Size variants:** Full width on mobile, auto on desktop
- **States:** Hover (scale 1.02), Active (scale 0.98), Disabled (opacity 0.5)

### Cards
- **Product cards:** 260px width (mobile flex), rounded 0.75rem, glassmorphic
- **Hover effect:** Shadow lg + translate Y(-4px), 200ms
- **Image aspect:** 1:1 or 4:3 (consistent per page)

### Forms
- **Inputs:** 44px height, 0.5rem radius, clear labels (not placeholder-only)
- **Focus:** Blue border + subtle glow (box-shadow)
- **Error state:** Red border + red text, clear error message below

### Navigation
- **Desktop:** Top nav bar with links, right-aligned actions
- **Mobile:** Hamburger menu or bottom nav (max 5 items)
- **Active state:** Blue underline or background color

### Modals & Overlays
- **Scrim:** `rgba(0,0,0,0.5)` (40-50% opacity)
- **Modal:** Glassmorphic card, centered or sheet-style
- **Entrance:** Scale + fade (150-200ms), with spatial continuity

---

## 📱 Responsive Breakpoints

| Device | Width | Layout | Nav |
|--------|-------|--------|-----|
| **Mobile** | 320-640px | Single column, full-width | Bottom nav |
| **Tablet** | 641-1024px | 2-3 column grid | Top nav |
| **Desktop** | 1025px+ | 4+ column grid, sidebar | Top nav + sidebar |

---

## ♿ Accessibility Checklist

- [ ] Color contrast ≥ 4.5:1 for text
- [ ] Touch targets ≥ 44×44px
- [ ] Focus rings visible (2-3px blue)
- [ ] Form labels with `<label for="">` (not placeholder-only)
- [ ] Alt text for all meaningful images
- [ ] Semantic HTML: `<button>`, `<a>`, `<form>` (not `<div>`)
- [ ] Aria-labels for icon-only buttons
- [ ] Keyboard navigation (Tab, Enter, Escape)
- [ ] Screen reader support (heading hierarchy)

---

## 🚀 Implementation Priority

1. **Phase 1:** Update CSS variables, colors, typography
2. **Phase 2:** Redesign cards (product, admin panels)
3. **Phase 3:** Improve buttons, forms, inputs
4. **Phase 4:** Navigation & layout
5. **Phase 5:** Dark mode toggle + animations
6. **Phase 6:** Responsive mobile optimization

---

## 📖 Anti-Patterns to Avoid

❌ Emojis as navigation icons (use SVG)  
❌ Gray text on gray backgrounds  
❌ Hover-only interactions (mobile-friendly!)  
❌ Unintended layout shift on interaction  
❌ Fixed widths instead of flexible layouts  
❌ No focus rings (accessibility fail)  
❌ Decorative-only animations (should convey meaning)  
❌ Placeholder-only form labels  
