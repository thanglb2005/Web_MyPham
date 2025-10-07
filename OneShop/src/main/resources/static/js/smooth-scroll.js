// Smooth scroll functionality for banner buttons
$(document).ready(function() {
    // Smooth scroll for anchor links
    $('a[href^="#"]').on('click', function(e) {
        e.preventDefault();
        
        var target = $(this).attr('href');
        var $target = $(target);
        
        if ($target.length) {
            var offset = 80; // Offset for fixed header
            var targetPosition = $target.offset().top - offset;
            
            $('html, body').animate({
                scrollTop: targetPosition
            }, 800, 'swing'); // 800ms duration with swing easing
        }
    });
    
    // Add smooth scroll behavior to CSS
    $('html').css('scroll-behavior', 'smooth');
});
