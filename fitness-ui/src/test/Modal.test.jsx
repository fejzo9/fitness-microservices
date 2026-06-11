import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Modal } from '../components/Modal'

describe('Modal Component', () => {
  it('renders modal with title', () => {
    render(<Modal title="Test Modal" onClose={() => {}}>Content</Modal>)
    expect(screen.getByText('Test Modal')).toBeInTheDocument()
  })

  it('renders children content', () => {
    render(
      <Modal title="Test Modal" onClose={() => {}}>
        <p>Modal content</p>
      </Modal>
    )
    expect(screen.getByText('Modal content')).toBeInTheDocument()
  })

  it('calls onClose when close button is clicked', () => {
    const handleClose = vi.fn()
    render(
      <Modal title="Test Modal" onClose={handleClose}>
        <p>Content</p>
      </Modal>
    )
    const closeButton = screen.getByText('×')
    closeButton.click()
    expect(handleClose).toHaveBeenCalledTimes(1)
  })

  it('renders close button', () => {
    render(<Modal title="Test Modal" onClose={() => {}}>Content</Modal>)
    const closeButton = screen.getByText('×')
    expect(closeButton).toBeInTheDocument()
  })
})
