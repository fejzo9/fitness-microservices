import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Spinner, LoadingSpinner } from '../components/Spinner'

describe('Spinner Component', () => {
  it('renders with default size', () => {
    const { container } = render(<Spinner />)
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toHaveClass('w-8', 'h-8')
  })

  it('renders with small size', () => {
    const { container } = render(<Spinner size="sm" />)
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toHaveClass('w-4', 'h-4')
  })

  it('renders with large size', () => {
    const { container } = render(<Spinner size="lg" />)
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toHaveClass('w-12', 'h-12')
  })

  it('renders with extra large size', () => {
    const { container } = render(<Spinner size="xl" />)
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toHaveClass('w-16', 'h-16')
  })

  it('applies custom className', () => {
    const { container } = render(<Spinner className="custom-class" />)
    const wrapper = container.querySelector('.custom-class')
    expect(wrapper).toBeInTheDocument()
  })
})

describe('LoadingSpinner Component', () => {
  it('renders spinner with message', () => {
    render(<LoadingSpinner message="Loading..." />)
    expect(screen.getByText('Loading...')).toBeInTheDocument()
  })

  it('renders spinner without message', () => {
    const { container } = render(<LoadingSpinner />)
    const spinner = container.querySelector('.animate-spin')
    expect(spinner).toBeInTheDocument()
  })

  it('renders with custom size', () => {
    render(<LoadingSpinner size="lg" message="Loading..." />)
    expect(screen.getByText('Loading...')).toBeInTheDocument()
  })
})
