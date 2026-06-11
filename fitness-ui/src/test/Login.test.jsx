import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { Login } from '../pages/Login'

// Mock dependencies
vi.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    login: vi.fn()
  })
}))

vi.mock('../services/api', () => ({
  api: {
    login: vi.fn()
  }
}))

describe('Login Page', () => {
  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    )
  }

  it('renders login form with title', () => {
    renderLogin()
    expect(screen.getByText('Fitness i Trening Menadžer')).toBeInTheDocument()
    expect(screen.getByText('Prijavite se na vaš nalog')).toBeInTheDocument()
  })

  it('renders username and password inputs', () => {
    renderLogin()
    expect(screen.getByPlaceholderText('Unesite korisničko ime')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Unesite lozinku')).toBeInTheDocument()
  })

  it('renders login button', () => {
    renderLogin()
    expect(screen.getByRole('button', { name: /prijavi se/i })).toBeInTheDocument()
  })

  it('renders register link', () => {
    renderLogin()
    expect(screen.getByText('Registrujte se')).toBeInTheDocument()
  })

  it('updates username input on change', () => {
    renderLogin()
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    expect(usernameInput.value).toBe('testuser')
  })

  it('updates password input on change', () => {
    renderLogin()
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    expect(passwordInput.value).toBe('password123')
  })

  it('shows error message when login fails', async () => {
    const { api } = await import('../services/api')
    api.login.mockRejectedValue({ response: { status: 401 } })

    renderLogin()
    
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')
    const submitButton = screen.getByRole('button', { name: /prijavi se/i })

    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Pogrešna lozinka')).toBeInTheDocument()
    })
  })

  it('disables submit button while loading', async () => {
    const { api } = await import('../services/api')
    api.login.mockImplementation(() => new Promise(() => {}))

    renderLogin()
    
    const usernameInput = screen.getByPlaceholderText('Unesite korisničko ime')
    const passwordInput = screen.getByPlaceholderText('Unesite lozinku')
    const submitButton = screen.getByRole('button', { name: /prijavi se/i })

    fireEvent.change(usernameInput, { target: { value: 'testuser' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(submitButton).toBeDisabled()
      expect(screen.getByText('Prijavljivanje...')).toBeInTheDocument()
    })
  })
})
