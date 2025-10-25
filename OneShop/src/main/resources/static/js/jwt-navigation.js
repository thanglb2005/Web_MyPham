/**
 * JWT-based Navigation System
 * Handles navigation for Pure JWT authentication
 */

class JWTNavigation {
    constructor() {
        this.baseUrl = window.location.origin;
        this.jwtAuth = window.jwtAuth; // Reference to jwt-auth.js
    }

    /**
     * Navigate to a protected page with JWT authentication
     * @param {string} url - The URL to navigate to
     * @param {boolean} forceReload - Force page reload after navigation
     */
    async navigateTo(url, forceReload = true) {
        try {
            // Get current JWT token
            const token = this.jwtAuth.getAccessToken();
            if (!token) {
                window.location.href = '/login';
                return;
            }

            // Validate token before navigation
            const isValid = await this.validateToken(token);
            if (!isValid) {
                this.jwtAuth.clearTokens();
                window.location.href = '/login';
                return;
            }

            // Set authentication context on server
            await this.setServerAuthContext(token);

            // Navigate to the URL - Always use full page reload for web pages
            window.location.href = url;

        } catch (error) {
            // Fallback to login page
            window.location.href = '/login';
        }
    }

    /**
     * Navigate without full page reload (for SPA-like behavior)
     * @param {string} url - The URL to navigate to
     */
    navigateWithoutReload(url) {
        try {
            // Use History API for navigation
            history.pushState(null, '', url);
            
            // Trigger popstate event to handle the navigation
            window.dispatchEvent(new PopStateEvent('popstate'));
            
            // If the page doesn't handle popstate, fallback to full reload
            setTimeout(() => {
                if (window.location.pathname !== url) {
                    window.location.href = url;
                }
            }, 100);
            
        } catch (error) {
            window.location.href = url;
        }
    }

    /**
     * Validate JWT token
     * @param {string} token - JWT token to validate
     * @returns {boolean} - Whether token is valid
     */
    async validateToken(token) {
        try {
            const response = await fetch('/api/auth/validate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ token: token })
            });

            if (response.ok) {
                const result = await response.json();
                return result.valid === true;
            }
            return false;
        } catch (error) {
            console.error('Token validation error:', error);
            return false;
        }
    }

    /**
     * Set server-side authentication context
     * @param {string} token - JWT token
     */
    async setServerAuthContext(token) {
        try {
            const response = await fetch('/api/auth/authenticate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ token: token })
            });

            if (!response.ok) {
                throw new Error(`Server auth context failed: ${response.status}`);
            }

            const result = await response.json();
            console.log('✅ Server auth context set:', result);
            
        } catch (error) {
            console.error('❌ Failed to set server auth context:', error);
            throw error;
        }
    }

    /**
     * Smart redirect based on user roles
     * @param {Object} userInfo - User information from JWT
     */
    redirectBasedOnRole(userInfo) {
        if (!userInfo || !userInfo.roles) {
            console.warn('⚠️ No user roles found, redirecting to home');
            this.navigateTo('/');
            return;
        }

        const roles = userInfo.roles;
        console.log('🎯 User roles:', roles);

        if (roles.includes('ROLE_ADMIN')) {
            this.navigateTo('/admin/home');
        } else if (roles.includes('ROLE_VENDOR')) {
            this.navigateTo('/vendor/my-shops');
        } else if (roles.includes('ROLE_CSKH')) {
            this.navigateTo('/cskh/chat');
        } else if (roles.includes('ROLE_SHIPPER')) {
            this.navigateTo('/shipper/home');
        } else {
            this.navigateTo('/');
        }
    }

    /**
     * Initialize JWT navigation system
     */
    init() {
        console.log('🚀 JWT Navigation System initialized');
        
        // Handle browser back/forward buttons
        window.addEventListener('popstate', (event) => {
            console.log('🔄 Popstate event:', event.state);
            // Let the page handle the navigation
        });

        // Intercept all internal links
        document.addEventListener('click', (event) => {
            const link = event.target.closest('a[href]');
            if (!link) return;

            const href = link.getAttribute('href');
            
            // Only intercept internal links that are not API calls
            if (href && href.startsWith('/') && !href.startsWith('/api/') && !href.startsWith('#')) {
                // Check if it's a protected route
                if (this.isProtectedRoute(href)) {
                    event.preventDefault();
                    this.navigateTo(href);
                }
            }
        });
    }

    /**
     * Check if a route is protected (requires authentication)
     * @param {string} route - The route to check
     * @returns {boolean} - Whether the route is protected
     */
    isProtectedRoute(route) {
        const protectedRoutes = [
            '/admin/',
            '/vendor/',
            '/cskh/',
            '/shipper/',
            '/user/',
            '/profile',
            '/dashboard'
        ];

        return protectedRoutes.some(protectedRoute => route.startsWith(protectedRoute));
    }

    /**
     * Handle login success and redirect
     * @param {Object} loginResponse - Login response from JWT API
     */
    async handleLoginSuccess(loginResponse) {
        try {
            console.log('🎉 Login successful, setting up navigation...');
            
            // Store tokens
            this.jwtAuth.storeTokensInSession(loginResponse.data.access_token, loginResponse.data.refresh_token);
            
            // Set server auth context
            await this.setServerAuthContext(loginResponse.data.access_token);
            
            // Redirect based on user role
            this.redirectBasedOnRole(loginResponse.data.user_info);
            
        } catch (error) {
            console.error('❌ Login success handling failed:', error);
            // Fallback to home page
            window.location.href = '/';
        }
    }
}

// Initialize JWT Navigation when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.jwtNavigation = new JWTNavigation();
    window.jwtNavigation.init();
});

// Export for use in other scripts
window.JWTNavigation = JWTNavigation;
