import { useEffect, useRef, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { createPopper } from '@popperjs/core'

interface Props {
  className?: string
  placement?: string
  autoClose?: boolean
  trigger: ReactNode
  children: ReactNode
}

export default function Dropdown({ className, placement = 'bottom-end', autoClose = true, trigger, children }: Props) {
  const [show, setShow] = useState(false)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const popperRef = useRef<ReturnType<typeof createPopper> | null>(null)

  useEffect(() => {
    if (show) {
      const popper = createPopper(buttonRef.current!, dropdownRef.current!, {
        placement: placement as never,
        modifiers: [
          {
            name: 'preventOverflow',
            options: {
              altBoundary: true,
            },
          },
        ],
      })
      popperRef.current = popper
    } else if (popperRef.current) {
      const popper = popperRef.current
      popperRef.current = null
      setTimeout(() => popper.destroy(), 100)
    }
    return () => {
      if (popperRef.current) {
        popperRef.current.destroy()
        popperRef.current = null
      }
    }
  }, [show, placement])

  useEffect(() => {
    if (typeof window === 'undefined') return
    const onKeydown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setShow(false)
      }
    }
    document.addEventListener('keydown', onKeydown)
    return () => document.removeEventListener('keydown', onKeydown)
  }, [])

  return (
    <button type="button" ref={buttonRef} className={className} onClick={() => setShow(true)}>
      {trigger}
      {show &&
        typeof document !== 'undefined' &&
        createPortal(
          <div>
            <div
              style={{ position: 'fixed', top: 0, right: 0, left: 0, bottom: 0, zIndex: 99998, background: 'black', opacity: 0.2 }}
              onClick={() => setShow(false)}
            />
            <div
              ref={dropdownRef}
              style={{ position: 'absolute', zIndex: 99999 }}
              onClick={autoClose ? () => setShow(false) : undefined}
            >
              {children}
            </div>
          </div>,
          document.getElementById('dropdown')!,
        )}
    </button>
  )
}
