import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Input } from '../components/Input'

describe('Input Component', () => {
  it('renders input without label', () => {
    render(<Input placeholder="Enter text" />)
    const input = screen.getByPlaceholderText('Enter text')
    expect(input).toBeInTheDocument()
  })

  it('renders input with label', () => {
    render(<Input label="Username" placeholder="Enter username" />)
    const label = screen.getByText('Username')
    const input = screen.getByPlaceholderText('Enter username')
    expect(label).toBeInTheDocument()
    expect(input).toBeInTheDocument()
  })

  it('passes through props to input element', () => {
    render(<Input type="email" placeholder="Email" />)
    const input = screen.getByPlaceholderText('Email')
    expect(input).toHaveAttribute('type', 'email')
  })

  it('applies correct CSS classes', () => {
    render(<Input placeholder="Test" />)
    const input = screen.getByRole('textbox')
    expect(input).toHaveClass('bg-secondary')
  })
})
