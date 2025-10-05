/**
 * Direct summary statistics update script
 * This script directly updates the summary statistics elements from the API
 */
(function() {
    // Wait for DOM to be ready
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Direct summary update script running');
        
        // Function to update summary stats directly
        async function updateSummaryStats() {
            try {
                // Check if elements exist
                const totalOrders = document.getElementById('totalOrders');
                const avgRevenue = document.getElementById('avgRevenue');
                const growthRateDisplay = document.getElementById('growthRateDisplay');
                const completedOrders = document.getElementById('completedOrders');
                
                console.log('Summary elements check:', {
                    totalOrders: totalOrders ? 'Found' : 'Missing',
                    avgRevenue: avgRevenue ? 'Found' : 'Missing', 
                    growthRateDisplay: growthRateDisplay ? 'Found' : 'Missing',
                    completedOrders: completedOrders ? 'Found' : 'Missing'
                });
                
                // If any element is missing or has no content, fetch from API
                if (!totalOrders || !avgRevenue || !growthRateDisplay || !completedOrders ||
                    !totalOrders.textContent || totalOrders.textContent === '...' ||
                    !avgRevenue.textContent || avgRevenue.textContent === '...' ||
                    !growthRateDisplay.textContent || growthRateDisplay.textContent === '...' ||
                    !completedOrders.textContent || completedOrders.textContent === '...') {
                    
                    console.log('Some elements missing or empty, fetching data from API');
                    
                    // Make direct API call
                    const response = await fetch('/admin/api/summary-stats');
                    if (response.ok) {
                        const data = await response.json();
                        console.log('API response:', data);
                        
                        // Update elements if they exist
                        if (totalOrders) {
                            totalOrders.textContent = data.totalOrders || '0';
                            console.log('Updated totalOrders:', totalOrders.textContent);
                        }
                        
                        if (avgRevenue) {
                            avgRevenue.textContent = data.averageRevenue || '0 đ';
                            console.log('Updated avgRevenue:', avgRevenue.textContent);
                        }
                        
                        if (growthRateDisplay) {
                            growthRateDisplay.textContent = data.growthRateDisplay || '0%';
                            console.log('Updated growthRateDisplay:', growthRateDisplay.textContent);
                        }
                        
                        if (completedOrders) {
                            completedOrders.textContent = data.completedOrders || '0/0';
                            console.log('Updated completedOrders:', completedOrders.textContent);
                        }
                        
                        console.log('Summary statistics updated successfully');
                    } else {
                        console.error('API request failed:', response.status, response.statusText);
                    }
                } else {
                    console.log('All elements have content, no need to update');
                }
            } catch (error) {
                console.error('Error updating summary stats:', error);
            }
        }
        
        // Run after a short delay to ensure other scripts have executed
        setTimeout(updateSummaryStats, 500);
    });
})();