// Search Autocomplete JavaScript

(function() {
    'use strict';
    
    let autocompleteCache = {};
    
    function initAutocomplete() {
        const searchInputs = document.querySelectorAll('input[name="searchQuery"]');
        
        searchInputs.forEach(input => {
            if (!input.dataset.autocompleteInitialized) {
                input.dataset.autocompleteInitialized = 'true';
                setupAutocomplete(input);
            }
        });
    }
    
    function setupAutocomplete(input) {
        let debounceTimer;
        let suggestionsContainer;
        
        // Create suggestions container
        suggestionsContainer = document.createElement('div');
        suggestionsContainer.className = 'autocomplete-suggestions';
        suggestionsContainer.style.display = 'none';
        input.parentElement.style.position = 'relative';
        input.parentElement.appendChild(suggestionsContainer);
        
        // Handle input
        input.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            
            clearTimeout(debounceTimer);
            
            if (query.length < 2) {
                suggestionsContainer.style.display = 'none';
                return;
            }
            
            debounceTimer = setTimeout(() => {
                fetchSuggestions(query, suggestionsContainer, input);
            }, 300);
        });
        
        // Handle clicks outside
        document.addEventListener('click', function(e) {
            if (!input.contains(e.target) && !suggestionsContainer.contains(e.target)) {
                suggestionsContainer.style.display = 'none';
            }
        });
        
        // Handle keyboard
        input.addEventListener('keydown', function(e) {
            const suggestions = suggestionsContainer.querySelectorAll('.autocomplete-suggestion');
            let activeIndex = -1;
            
            // Find active suggestion
            for (let i = 0; i < suggestions.length; i++) {
                if (suggestions[i].classList.contains('active')) {
                    activeIndex = i;
                    break;
                }
            }
            
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (activeIndex < suggestions.length - 1) {
                    if (activeIndex >= 0) suggestions[activeIndex].classList.remove('active');
                    suggestions[activeIndex + 1].classList.add('active');
                    suggestions[activeIndex + 1].scrollIntoView({ block: 'nearest' });
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (activeIndex > 0) {
                    suggestions[activeIndex].classList.remove('active');
                    suggestions[activeIndex - 1].classList.add('active');
                    suggestions[activeIndex - 1].scrollIntoView({ block: 'nearest' });
                }
            } else if (e.key === 'Enter' && activeIndex >= 0) {
                e.preventDefault();
                suggestions[activeIndex].click();
            }
        });
    }
    
    function fetchSuggestions(query, container, input) {
        // Check cache
        if (autocompleteCache[query]) {
            displaySuggestions(autocompleteCache[query], container, input);
            return;
        }
        
        // Show loading
        container.innerHTML = '<div class="search-loading">Đang tìm kiếm...</div>';
        container.style.display = 'block';
        
        // Fetch from API
        fetch(`/api/search/autocomplete?q=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    return [];
                }
                return response.json();
            })
            .then(data => {
                // Safely handle array
                const items = Array.isArray(data) ? data : [];
                
                // Cache results
                autocompleteCache[query] = items;
                
                // Display suggestions
                displaySuggestions(items, container, input);
            })
            .catch(error => {
                console.log('Autocomplete: No results or API unavailable');
                container.style.display = 'none';
            });
    }
    
    function displaySuggestions(suggestions, container, input) {
        if (!Array.isArray(suggestions) || suggestions.length === 0) {
            container.style.display = 'none';
            return;
        }
        
        container.innerHTML = '';
        
        suggestions.forEach((item) => {
            const suggestionDiv = document.createElement('div');
            suggestionDiv.className = 'autocomplete-suggestion';
            suggestionDiv.tabIndex = 0;
            
            // Safely get item name
            const itemName = item.name || item || '';
            const query = input.value;
            
            // Highlight query text
            let displayText = itemName;
            if (query && itemName) {
                displayText = highlightQuery(itemName, query);
            }
            
            suggestionDiv.innerHTML = displayText;
            
            // Handle click
            suggestionDiv.addEventListener('click', function() {
                input.value = itemName;
                container.style.display = 'none';
                
                // Trigger search
                const form = input.closest('form');
                if (form) {
                    form.submit();
                }
            });
            
            container.appendChild(suggestionDiv);
        });
        
        container.style.display = 'block';
    }
    
    function highlightQuery(text, query) {
        const regex = new RegExp(`(${query})`, 'gi');
        return text.replace(regex, '<span class="suggestion-highlight">$1</span>');
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAutocomplete);
    } else {
        initAutocomplete();
    }
})();
