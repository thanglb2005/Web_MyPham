document.addEventListener("DOMContentLoaded", () => {
  const filterButtons = Array.from(document.querySelectorAll(".filter-btn"));
  const searchInput = document.getElementById("voucher-search");
  const shopCards = Array.from(document.querySelectorAll(".shop-voucher-card"));

  function getActiveFilter() {
    const active = filterButtons.find((btn) => btn.classList.contains("active"));
    return active ? active.dataset.filter : "all";
  }

  function matchFilter(item, filter) {
    if (filter === "expiring") {
      return item.dataset.expiring === "true";
    }
    if (filter === "usable") {
      const remaining = parseInt(item.dataset.remaining ?? "1", 10);
      return Number.isNaN(remaining) || remaining > 0;
    }
    return true;
  }

  function applyFilters() {
    const filter = getActiveFilter();
    const query = searchInput ? searchInput.value.trim().toLowerCase() : "";

    shopCards.forEach((card) => {
      const voucherItems = Array.from(card.querySelectorAll(".voucher-item"));
      let visibleCount = 0;
      const shopName = (card.dataset.shop || "").toLowerCase();

      voucherItems.forEach((item) => {
        const matchesFilter = matchFilter(item, filter);
        const code = item.querySelector(".voucher-code strong")?.textContent?.toLowerCase() ?? "";
        const title = item.querySelector(".voucher-title")?.textContent?.toLowerCase() ?? "";
        const matchesSearch =
          !query || shopName.includes(query) || code.includes(query) || title.includes(query);
        const shouldShow = matchesFilter && matchesSearch;
        item.style.display = shouldShow ? "" : "none";
        if (shouldShow) {
          visibleCount += 1;
        }
      });

      card.style.display = visibleCount > 0 ? "" : "none";
    });
  }

  filterButtons.forEach((button) => {
    button.addEventListener("click", () => {
      filterButtons.forEach((btn) => btn.classList.remove("active"));
      button.classList.add("active");
      applyFilters();
    });
  });

  if (searchInput) {
    searchInput.addEventListener("input", () => {
      applyFilters();
    });
  }

  document.querySelectorAll(".copy-btn").forEach((button) => {
    button.addEventListener("click", async () => {
      const code = button.dataset.code;
      if (!code) {
        return;
      }
      try {
        await navigator.clipboard.writeText(code);
        button.textContent = "Đã sao chép";
        button.classList.add("copied");
        setTimeout(() => {
          button.textContent = "Sao chép";
          button.classList.remove("copied");
        }, 2000);
      } catch (err) {
        console.warn("Copy voucher code failed", err);
        button.textContent = "Thử lại";
        setTimeout(() => {
          button.textContent = "Sao chép";
        }, 2000);
      }
    });
  });

  applyFilters();
});
