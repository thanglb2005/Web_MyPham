/**
 * JWT Authentication JavaScript Library for OneShop
 * Handles JWT token management, API calls, and authentication
 */

class JwtAuth {
    constructor() {
        this.baseUrl = '/api/auth';
        this.accessTokenKey = 'oneshop_access_token';
        this.refreshTokenKey = 'oneshop_refresh_token';
        this.userInfoKey = 'oneshop_user_info';
        
        // Auto-refresh token before expiration
        this.setupTokenRefresh();
    }

    /**
     * Store JWT tokens in localStorage
     */
    storeTokens(accessToken, refreshToken, userInfo = null) {
        localStorage.setItem(this.accessTokenKey, accessToken);
        localStorage.setItem(this.refreshTokenKey, refreshToken);
        
        if (userInfo) {
            localStorage.setItem(this.userInfoKey, JSON.stringify(userInfo));
        }
    }

    /**
     * Get access token from localStorage
     */
    getAccessToken() {
        return localStorage.getItem(this.accessTokenKey);
    }

    /**
     * Get refresh token from localStorage
     */
    getRefreshToken() {
        return localStorage.getItem(this.refreshTokenKey);
    }

    /**
     * Get user info from localStorage
     */
    getUserInfo() {
        const userInfo = localStorage.getItem(this.userInfoKey);
        return userInfo ? JSON.parse(userInfo) : null;
    }

    /**
     * Store tokens in session for server-side access
     */
    storeTokensInSession(accessToken, refreshToken) {
        // Store in sessionStorage (available to server-side)
        sessionStorage.setItem('jwtAccessToken', accessToken);
        sessionStorage.setItem('jwtRefreshToken', refreshToken);
    }

    /**
     * Clear all tokens and user info
     */
    clearTokens() {
        localStorage.removeItem(this.accessTokenKey);
        localStorage.removeItem(this.refreshTokenKey);
        localStorage.removeItem(this.userInfoKey);
        sessionStorage.removeItem('jwtAccessToken');
        sessionStorage.removeItem('jwtRefreshToken');
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        const token = this.getAccessToken();
        return token && !this.isTokenExpired(token);
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(token) {
        if (!token) return true;
        
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            return payload.exp < currentTime;
        } catch (error) {
            return true;
        }
    }

    /**
     * Login with email and password
     */
    async login(email, password, rememberMe = false) {
        try {
            const response = await fetch(`${this.baseUrl}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    rememberMe: rememberMe
                })
            });

            const data = await response.json();

            if (response.ok) {
                this.storeTokens(data.access_token, data.refresh_token, data.user_info);
                
                // Also store tokens in session for server-side access
                this.storeTokensInSession(data.access_token, data.refresh_token);
                
                return { success: true, data: data };
            } else {
                return { success: false, error: data.error || 'Login failed' };
            }
        } catch (error) {
            return { success: false, error: 'Network error: ' + error.message };
        }
    }

    /**
     * Refresh access token using refresh token
     */
    async refreshToken() {
        const refreshToken = this.getRefreshToken();
        
        if (!refreshToken) {
            throw new Error('No refresh token available');
        }

        try {
            const response = await fetch(`${this.baseUrl}/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refresh_token: refreshToken
                })
            });

            const data = await response.json();

            if (response.ok) {
                this.storeTokens(data.access_token, data.refresh_token);
                return data.access_token;
            } else {
                this.clearTokens();
                throw new Error(data.error || 'Token refresh failed');
            }
        } catch (error) {
            this.clearTokens();
            throw error;
        }
    }

    /**
     * Logout user
     */
    async logout() {
        try {
            const token = this.getAccessToken();
            
            if (token) {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    }
                });
            }
        } catch (error) {
            // Logout failed, but continue with clearing tokens
        } finally {
            this.clearTokens();
            // Redirect to login page
            window.location.href = '/login';
        }
    }

    /**
     * Get current user information
     */
    async getCurrentUser() {
        try {
            const response = await this.makeAuthenticatedRequest(`${this.baseUrl}/me`);
            
            if (response.ok) {
                const userInfo = await response.json();
                localStorage.setItem(this.userInfoKey, JSON.stringify(userInfo));
                return userInfo;
            } else {
                throw new Error('Failed to get user info');
            }
        } catch (error) {
            console.error('Get current user failed:', error);
            throw error;
        }
    }

    /**
     * Make authenticated API request with automatic token refresh
     */
    async makeAuthenticatedRequest(url, options = {}) {
        let token = this.getAccessToken();

        // Check if token is expired and refresh if needed
        if (token && this.isTokenExpired(token)) {
            try {
                token = await this.refreshToken();
            } catch (error) {
                // Refresh failed, redirect to login
                this.logout();
                return;
            }
        }

        // Add authorization header
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const requestOptions = {
            ...options,
            headers: headers
        };

        try {
            const response = await fetch(url, requestOptions);

            // If unauthorized, try to refresh token once
            if (response.status === 401 && token) {
                try {
                    token = await this.refreshToken();
                    headers['Authorization'] = `Bearer ${token}`;
                    
                    const retryResponse = await fetch(url, {
                        ...requestOptions,
                        headers: headers
                    });
                    
                    return retryResponse;
                } catch (refreshError) {
                    // Refresh failed, logout
                    this.logout();
                    return;
                }
            }

            return response;
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    /**
     * Setup automatic token refresh
     */
    setupTokenRefresh() {
        // Check token expiration every 5 minutes
        setInterval(() => {
            const token = this.getAccessToken();
            
            if (token && this.isTokenExpired(token)) {
                const refreshToken = this.getRefreshToken();
                
                if (refreshToken) {
                    this.refreshToken().catch(() => {
                        // Refresh failed, user will be logged out on next request
                        console.warn('Automatic token refresh failed');
                    });
                }
            }
        }, 5 * 60 * 1000); // 5 minutes
    }

    /**
     * Validate token
     */
    async validateToken(token = null) {
        const tokenToValidate = token || this.getAccessToken();
        
        if (!tokenToValidate) {
            return { valid: false, error: 'No token provided' };
        }

        try {
            const response = await fetch(`${this.baseUrl}/validate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    token: tokenToValidate
                })
            });

            return await response.json();
        } catch (error) {
            return { valid: false, error: error.message };
        }
    }

    /**
     * Get authorization header for manual API calls
     */
    getAuthHeader() {
        const token = this.getAccessToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    /**
     * Check if user has specific role
     */
    hasRole(roleName) {
        const userInfo = this.getUserInfo();
        return userInfo && userInfo.roles && userInfo.roles.includes(roleName);
    }

    /**
     * Check if user is admin
     */
    isAdmin() {
        return this.hasRole('ROLE_ADMIN');
    }

    /**
     * Check if user is vendor
     */
    isVendor() {
        return this.hasRole('ROLE_VENDOR');
    }

    /**
     * Check if user is shipper
     */
    isShipper() {
        return this.hasRole('ROLE_SHIPPER');
    }

    /**
     * Check if user is CSKH
     */
    isCSKH() {
        return this.hasRole('ROLE_CSKH');
    }
}

// Create global instance
window.jwtAuth = new JwtAuth();

// Utility functions for backward compatibility
window.JwtAuthUtils = {
    // Login function
    login: (email, password, rememberMe) => window.jwtAuth.login(email, password, rememberMe),
    
    // Logout function
    logout: () => window.jwtAuth.logout(),
    
    // Check authentication
    isAuthenticated: () => window.jwtAuth.isAuthenticated(),
    
    // Get user info
    getUserInfo: () => window.jwtAuth.getUserInfo(),
    
    // Make authenticated request
    apiCall: (url, options) => window.jwtAuth.makeAuthenticatedRequest(url, options),
    
    // Get auth headers
    getAuthHeaders: () => window.jwtAuth.getAuthHeader()
};

// Auto-initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is authenticated and update UI accordingly
    if (window.jwtAuth.isAuthenticated()) {
        const userInfo = window.jwtAuth.getUserInfo();
        if (userInfo) {
            console.log('User authenticated:', userInfo.email);
            
            // Update UI elements if they exist
            const userNameElement = document.querySelector('.user-name');
            if (userNameElement) {
                userNameElement.textContent = userInfo.name;
            }
            
            const userEmailElement = document.querySelector('.user-email');
            if (userEmailElement) {
                userEmailElement.textContent = userInfo.email;
            }
        }
    }
});

console.log('JWT Auth library loaded successfully');
