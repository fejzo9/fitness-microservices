import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { Register } from '../pages/Register'

// Mock dependencies
vi.mock('../services/api', () => ({
  api: {
    register: vi.fn()
  }
}))

describe('Register Page', () => {
  const renderRegister = () => {
    return render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    )
  }

  it('renders register form with title', () => {
    renderRegister()
    expect(screen.getByText('Fitness i Trening Menadžer')).toBeInTheDocument()
    expect(screen.getByText('Kreirajte novi nalog')).toBeInTheDocument()
  })

  it('renders all required input fields', () => {
    renderRegister()
    expect(screen.getByPlaceholderText('Ime')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Prezime')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Unesite korisničko ime')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Unesite email')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Unesite lozinku')).toBeInTheDocument()
  })

  it('renders register button', () => {
    renderRegister()
    expect(screen.getByRole('button', { name: /registruj se/i })).toBeInTheDocument()
  })

  it('renders login link', () => {
    renderRegister()
    expect(screen.getByText('Prijavite se')).toBeInTheDocument()
  })

  it('updates form fields on change', () => {
    renderRegister()
    
    const firstNameInput = screen.getByPlaceholderText('Ime')
    const lastNameInput = screen.getByPlaceholderText('Prezime')
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    const emailInput = screen.getByPlaceholderText('Unesite email')
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')

    fireEvent.change(firstNameInput, { target: { value: 'John' } })
    fireEvent.change(lastNameInput, { target: { value: 'Doe' } })
    fireEvent.change(usernameInput, { target: { value: 'johndoe' } })
    fireEvent.change(emailInput, { target: { value: 'john@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })

    expect(firstNameInput.value).toBe('John')
    expect(lastNameInput.value).toBe('Doe')
    expect(usernameInput.value).toBe('johndoe')
    expect(emailInput.value).toBe('john@example.com')
    expect(passwordInput.value).toBe('password123')
  })

  it('shows error message when registration fails with conflict', async () => {
    const { api } = await import('../services/api')
    api.register.mockRejectedValue({ response: { status: 409 } })

    renderRegister()
    
    const firstNameInput = screen.getByPlaceholderText('Ime')
    const lastNameInput = screen.getByPlaceholderText('Prezime')
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    const emailInput = screen.getByPlaceholderText('Unesite email')
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')
    const submitButton = screen.getByRole('button', { name: /registruj se/i })

    fireEvent.change(firstNameInput, { target: { value: 'John' } })
    fireEvent.change(lastNameInput, { target: { value: 'Doe' } })
    fireEvent.change(usernameInput, { target: { value: 'existinguser' } })
    fireEvent.change(emailInput, { target: { value: 'existing@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Korisničko ime ili email već postoji')).toBeInTheDocument()
    })
  })

  it('disables submit button while loading', async () => {
    const { api } = await import('../services/api')
    api.register.mockImplementation(() => new Promise(() => {}))

    renderRegister()
    
    const firstNameInput = screen.getByPlaceholderText('Ime')
    const lastNameInput = screen.getByPlaceholderText('Prezime')
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    const emailInput = screen.getByPlaceholderText('Unesite email')
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')
    const submitButton = screen.getByRole('button', { name: /registruj se/i })

    fireEvent.change(firstNameInput, { target: { value: 'John' } })
    fireEvent.change(lastNameInput, { target: { value: 'Doe' } })
    fireEvent.change(usernameInput, { target: { value: 'johndoe' } })
    fireEvent.change(emailInput, { target: { value: 'john@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(submitButton).toBeDisabled()
      expect(screen.getByText('Registracija...')).toBeInTheDocument()
    })
  })
})
