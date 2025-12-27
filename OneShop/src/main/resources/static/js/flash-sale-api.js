/**
 * Flash Sale API Functions
 * Functions to interact with Flash Sale API endpoints
 */

const FlashSaleAPI = {
    /**
     * Get active flash sales
     * @returns {Promise<Array>} List of active flash sales
     */
    getActiveFlashSales: async function() {
        try {
            console.log('FlashSaleAPI: Fetching from /flash-sale/api/active');
            const response = await fetch('/flash-sale/api/active');
            console.log('FlashSaleAPI: Response status:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('FlashSaleAPI: Response not OK:', errorText);
                throw new Error(`Failed to fetch active flash sales: ${response.status} ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('FlashSaleAPI: Received data:', data);
            console.log('FlashSaleAPI: Data type:', typeof data);
            console.log('FlashSaleAPI: Is array?', Array.isArray(data));
            
            // Ensure we return an array
            if (Array.isArray(data)) {
                return data;
            } else {
                console.warn('FlashSaleAPI: Data is not an array, converting:', data);
                return [];
            }
        } catch (error) {
            console.error('FlashSaleAPI: Error fetching active flash sales:', error);
            console.error('FlashSaleAPI: Error stack:', error.stack);
            return [];
        }
    },

    /**
     * Get upcoming flash sales
     * @returns {Promise<Array>} List of upcoming flash sales
     */
    getUpcomingFlashSales: async function() {
        try {
            const response = await fetch('/flash-sale/api/upcoming');
            if (!response.ok) {
                throw new Error('Failed to fetch upcoming flash sales');
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching upcoming flash sales:', error);
            return [];
        }
    },

    /**
     * Get flash sale price for a product
     * @param {number} productId - Product ID
     * @returns {Promise<Object>} Flash sale info for the product
     */
    getProductFlashSalePrice: async function(productId) {
        try {
            const url = `/flash-sale/api/product/${productId}/price`;
            console.log('FlashSaleAPI: Fetching product price from:', url);
            const response = await fetch(url);
            console.log('FlashSaleAPI: Response status:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('FlashSaleAPI: Response not OK:', errorText);
                throw new Error(`Failed to fetch flash sale price: ${response.status} ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('FlashSaleAPI: Received product price data:', data);
            return data;
        } catch (error) {
            console.error('FlashSaleAPI: Error fetching flash sale price:', error);
            console.error('FlashSaleAPI: Error stack:', error.stack);
            return { inFlashSale: false };
        }
    },

    /**
     * Get products in a flash sale
     * @param {number} flashSaleId - Flash Sale ID
     * @returns {Promise<Object>} Flash sale with products
     */
    getFlashSaleProducts: async function(flashSaleId) {
        try {
            const url = `/flash-sale/api/${flashSaleId}/products`;
            console.log('FlashSaleAPI: Fetching products from:', url);
            const response = await fetch(url);
            console.log('FlashSaleAPI: Response status:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('FlashSaleAPI: Response not OK:', errorText);
                throw new Error(`Failed to fetch flash sale products: ${response.status} ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('FlashSaleAPI: Received products data:', data);
            return data;
        } catch (error) {
            console.error('FlashSaleAPI: Error fetching flash sale products:', error);
            console.error('FlashSaleAPI: Error stack:', error.stack);
            return { success: false, message: error.message };
        }
    }
};

