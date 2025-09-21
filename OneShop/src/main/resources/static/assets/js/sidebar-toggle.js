// Sidebar Toggle Functionality
document.addEventListener("DOMContentLoaded", function () {
  // Initialize sidebar toggle
  initSidebarToggle();

  // Handle responsive behavior
  handleResponsiveSidebar();
});

function initSidebarToggle() {
  const toggleButtons = document.querySelectorAll(".toggle-sidebar");
  const wrapper = document.querySelector(".wrapper");

  toggleButtons.forEach((button) => {
    button.addEventListener("click", function (e) {
      e.preventDefault();
      toggleSidebar();
    });
  });

  // Check if sidebar should be minimized on page load
  const savedState = localStorage.getItem("sidebarMinimized");
  if (savedState === "true") {
    wrapper.classList.add("sidebar_minimize");
    updateToggleButtons(true);
  }
}

function toggleSidebar() {
  const wrapper = document.querySelector(".wrapper");
  const isMinimized = wrapper.classList.contains("sidebar_minimize");

  if (isMinimized) {
    // Expand sidebar
    wrapper.classList.remove("sidebar_minimize");
    updateToggleButtons(false);
    localStorage.setItem("sidebarMinimized", "false");
  } else {
    // Minimize sidebar
    wrapper.classList.add("sidebar_minimize");
    updateToggleButtons(true);
    localStorage.setItem("sidebarMinimized", "true");
  }

  // Trigger resize event for charts and other components
  window.dispatchEvent(new Event("resize"));
}

function updateToggleButtons(isMinimized) {
  const toggleButtons = document.querySelectorAll(".toggle-sidebar");

  toggleButtons.forEach((button) => {
    if (isMinimized) {
      button.classList.add("toggled");
    } else {
      button.classList.remove("toggled");
    }
  });
}

function handleResponsiveSidebar() {
  // Handle mobile sidebar behavior
  const mediaQuery = window.matchMedia("(max-width: 768px)");

  function handleMobileSidebar(e) {
    const wrapper = document.querySelector(".wrapper");

    if (e.matches) {
      // Mobile: sidebar should be hidden by default
      wrapper.classList.add("sidebar_minimize");
      updateToggleButtons(true);
    } else {
      // Desktop: restore saved state
      const savedState = localStorage.getItem("sidebarMinimized");
      if (savedState === "false") {
        wrapper.classList.remove("sidebar_minimize");
        updateToggleButtons(false);
      }
    }
  }

  // Initial check
  handleMobileSidebar(mediaQuery);

  // Listen for changes
  mediaQuery.addListener(handleMobileSidebar);
}

// Keyboard shortcut for toggling sidebar (Ctrl + B)
document.addEventListener("keydown", function (e) {
  if (e.ctrlKey && e.key === "b") {
    e.preventDefault();
    toggleSidebar();
  }
});

// Smooth scroll for sidebar navigation
function smoothScrollToElement(element) {
  if (element) {
    element.scrollIntoView({
      behavior: "smooth",
      block: "center",
    });
  }
}

// Add click handlers for sidebar navigation
document.addEventListener("DOMContentLoaded", function () {
  const sidebarLinks = document.querySelectorAll(".sidebar .nav-link");

  sidebarLinks.forEach((link) => {
    link.addEventListener("click", function (e) {
      // Add active state
      sidebarLinks.forEach((l) => l.classList.remove("active"));
      this.classList.add("active");

      // Close mobile sidebar after navigation
      if (window.innerWidth <= 768) {
        setTimeout(() => {
          const wrapper = document.querySelector(".wrapper");
          wrapper.classList.add("sidebar_minimize");
          updateToggleButtons(true);
        }, 300);
      }
    });
  });
});

// Export functions for global access
window.toggleSidebar = toggleSidebar;
window.updateToggleButtons = updateToggleButtons;
