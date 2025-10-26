/**
 * Cloudinary Upload Helper for OneShop
 * Tương tự như web bán giày nhưng được cải thiện cho OneShop
 */

window.cloudinaryUpload = {
    
    /**
     * Upload image to specific type
     * @param {File} file - Image file to upload
     * @param {string} type - Type of image (product, user, brand, category, rating, general)
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadImage(file, type = 'general', options = {}) {
        try {
            // Validate file
            if (!this.validateImageFile(file)) {
                throw new Error('File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.');
            }

            const formData = new FormData();
            formData.append('file', file);

            let endpoint = '/api/images/upload';
            if (type !== 'general') {
                endpoint += `/${type}`;
            }

            const response = await fetch(endpoint, {
                method: 'POST',
                body: formData
            });

            const result = await response.json();
            
            if (!result.success) {
                throw new Error(result.error || 'Upload failed');
            }

            return result;
        } catch (error) {
            console.error('Upload failed:', error);
            throw error;
        }
    },

    /**
     * Upload product image
     * @param {File} file - Image file
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadProductImage(file, options = {}) {
        return this.uploadImage(file, 'product', options);
    },

    /**
     * Upload user avatar
     * @param {File} file - Image file
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadUserImage(file, options = {}) {
        return this.uploadImage(file, 'user', options);
    },

    /**
     * Upload brand logo
     * @param {File} file - Image file
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadBrandImage(file, options = {}) {
        return this.uploadImage(file, 'brand', options);
    },

    /**
     * Upload category image
     * @param {File} file - Image file
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadCategoryImage(file, options = {}) {
        return this.uploadImage(file, 'category', options);
    },

    /**
     * Upload rating image
     * @param {File} file - Image file
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} Upload result
     */
    async uploadRatingImage(file, options = {}) {
        return this.uploadImage(file, 'rating', options);
    },

    /**
     * Delete image by URL
     * @param {string} imageUrl - Image URL to delete
     * @returns {Promise<Object>} Delete result
     */
    async deleteImage(imageUrl) {
        try {
            const response = await fetch('/api/images/delete', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `url=${encodeURIComponent(imageUrl)}`
            });

            const result = await response.json();
            return result;
        } catch (error) {
            console.error('Delete failed:', error);
            throw error;
        }
    },

    /**
     * Get optimized image URL
     * @param {string} publicId - Cloudinary public ID
     * @param {number} width - Desired width
     * @param {number} height - Desired height
     * @returns {Promise<Object>} URL result
     */
    async getOptimizedImageUrl(publicId, width = 300, height = 300) {
        try {
            const response = await fetch(`/api/images/url/${publicId}?width=${width}&height=${height}`);
            const result = await response.json();
            return result;
        } catch (error) {
            console.error('Get URL failed:', error);
            throw error;
        }
    },

    /**
     * Validate image file
     * @param {File} file - File to validate
     * @returns {boolean} Is valid
     */
    validateImageFile(file) {
        if (!file) return false;
        
        // Check file size (max 10MB)
        if (file.size > 10 * 1024 * 1024) {
            return false;
        }
        
        // Check file type
        const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            return false;
        }
        
        // Check file extension
        const validExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp'];
        const fileName = file.name.toLowerCase();
        return validExtensions.some(ext => fileName.endsWith(ext));
    },

    /**
     * Create image preview
     * @param {File} file - Image file
     * @param {HTMLElement} previewElement - Element to show preview
     * @returns {Promise<void>}
     */
    async createPreview(file, previewElement) {
        if (!this.validateImageFile(file)) {
            throw new Error('File không hợp lệ!');
        }

        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                previewElement.src = e.target.result;
                previewElement.style.display = 'block';
                resolve();
            };
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    },

    /**
     * Upload with progress callback
     * @param {File} file - Image file
     * @param {string} type - Image type
     * @param {Function} progressCallback - Progress callback function
     * @returns {Promise<Object>} Upload result
     */
    async uploadWithProgress(file, type = 'general', progressCallback = null) {
        try {
            // Validate file
            if (!this.validateImageFile(file)) {
                throw new Error('File không hợp lệ!');
            }

            const formData = new FormData();
            formData.append('file', file);

            let endpoint = '/api/images/upload';
            if (type !== 'general') {
                endpoint += `/${type}`;
            }

            return new Promise((resolve, reject) => {
                const xhr = new XMLHttpRequest();
                
                // Progress tracking
                xhr.upload.addEventListener('progress', (e) => {
                    if (e.lengthComputable && progressCallback) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        progressCallback(percentComplete);
                    }
                });

                xhr.addEventListener('load', () => {
                    if (xhr.status === 200) {
                        try {
                            const result = JSON.parse(xhr.responseText);
                            if (result.success) {
                                resolve(result);
                            } else {
                                reject(new Error(result.error || 'Upload failed'));
                            }
                        } catch (e) {
                            reject(new Error('Invalid response format'));
                        }
                    } else {
                        reject(new Error(`Upload failed with status: ${xhr.status}`));
                    }
                });

                xhr.addEventListener('error', () => {
                    reject(new Error('Network error during upload'));
                });

                xhr.open('POST', endpoint);
                xhr.send(formData);
            });
        } catch (error) {
            console.error('Upload with progress failed:', error);
            throw error;
        }
    }
};

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Cloudinary Upload Helper initialized for OneShop');
});