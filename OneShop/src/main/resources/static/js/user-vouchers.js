document.addEventListener("DOMContentLoaded", () => {
  // Simple section filter
  const filterBtns = Array.from(document.querySelectorAll('.sf-btn'));
  const systemSection = document.querySelector('[th\\:if]') ? null : null; // placeholder to avoid thymeleaf syntax here
  const systemContainer = document.querySelector('.voucher-section:nth-of-type(1)');
  const shopContainer = document.querySelector('.voucher-section:nth-of-type(2)');

  function setFilter(target) {
    if (systemContainer && shopContainer) {
      if (target === 'system') {
        systemContainer.style.display = '';
        shopContainer.style.display = 'none';
      } else if (target === 'shop') {
        systemContainer.style.display = 'none';
        shopContainer.style.display = '';
      } else {
        systemContainer.style.display = '';
        shopContainer.style.display = '';
      }
    }
  }

  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      filterBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      setFilter(btn.dataset.target);
    });
  });

  // Copy voucher code functionality only
  const copyButtons = document.querySelectorAll(".btn-copy-code, .copy-btn-icon");
  copyButtons.forEach((button) => {
    button.addEventListener("click", async (e) => {
      e.preventDefault();
      const code = button.dataset.code;
      if (!code) return;

      try {
        await navigator.clipboard.writeText(code);
        
        // Visual feedback
        const originalHTML = button.innerHTML;
        const icon = button.querySelector("i");
        
        if (icon) {
          icon.className = "fa-solid fa-check";
        } else {
          button.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        
        button.classList.add("copied");

        // Reset after 2 seconds
        setTimeout(() => {
          button.innerHTML = originalHTML;
          button.classList.remove("copied");
        }, 2000);

      } catch (err) {
        console.warn("Copy failed", err);
        alert("Không thể sao chép mã. Vui lòng thử lại.");
      }
    });
  });

  // Optional: graceful fallback when Clipboard API missing
  if (!navigator.clipboard) {
    copyButtons.forEach((btn) => {
      btn.addEventListener("click", () => {
        alert("Sao chép mã: " + (btn.dataset.code || ""));
      });
    });
  }
});


