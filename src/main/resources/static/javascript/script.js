document.addEventListener("DOMContentLoaded", () => {
//   // Check for redirect after login
//   const redirectUrl = sessionStorage.getItem("redirectAfterLogin");
  
//   // Check if user just authenticated: login button is hidden OR they were redirected to home from login
//   const loginBtnMissing = document.querySelector(".login-btn") === null;
//   const justLoggedIn = document.referrer.includes("/login") && (window.location.pathname === "/" || window.location.pathname === "");

//   if (redirectUrl) {
//     if (loginBtnMissing || justLoggedIn) {
//       sessionStorage.removeItem("redirectAfterLogin");
//       // Redirect back to the original page
//       if (window.location.href !== redirectUrl) {
//         window.location.replace(redirectUrl);
//       }
//     } else if (window.location.pathname !== "/login" && window.location.pathname !== "/register") {
//       // User navigated away from login/register without logging in
//       sessionStorage.removeItem("redirectAfterLogin");
//     }
//   }

//   const loginBtn = document.querySelector(".login-btn a");
//   if (loginBtn) {
//     loginBtn.addEventListener("click", () => {
//       if (!window.location.pathname.includes("/login") && !window.location.pathname.includes("/register")) {
//         sessionStorage.setItem("redirectAfterLogin", window.location.href);
//       }
//     });
//   }

//   const registerBtn = document.querySelector(".register-btn a");
//   if (registerBtn) {
//     registerBtn.addEventListener("click", () => {
//       if (!window.location.pathname.includes("/login") && !window.location.pathname.includes("/register")) {
//         sessionStorage.setItem("redirectAfterLogin", window.location.href);
//       }
//     });
//   }

  // 1. Hero Section Animation (On Page Load)
  const heroTitle = document.querySelector(".hero h1");
  const heroSubtitle = document.querySelector(".hero p");
  const searchBox = document.querySelector(".search-container");

  // Function to apply styles dynamically
  const animateHeroElement = (element, delay) => {
    if (element) {
      element.style.opacity = "0";
      element.style.transform = "translateY(20px)";
      element.style.transition = "opacity 0.8s ease-out, transform 0.8s ease-out";

      setTimeout(() => {
        element.style.opacity = "1";
        element.style.transform = "translateY(0)";
      }, delay);
    }
  };

  animateHeroElement(heroTitle, 100);   // Title appears first
  animateHeroElement(heroSubtitle, 300); // subtitle
  animateHeroElement(searchBox, 500);    // search bar


  // 2. Scroll Reveal Animation (As user scrolls down)
  const observerOptions = {
    threshold: 0.10, // Trigger when 15% of the element is visible
    rootMargin: "0px 0px -50px 0px" // Offset slightly so it triggers before hitting bottom
  };

  const observer = new IntersectionObserver((entries, observer) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add("animate-visible");
        observer.unobserve(entry.target); // Stop watching once animated
      }
    });
  }, observerOptions);

  // Select sections to animate
  const sectionsToAnimate = document.querySelectorAll(
    ".card-container, #offerbanner, #freeeyecheckup"
  );

  sectionsToAnimate.forEach((section) => {
    section.classList.add("animate-hidden");
    observer.observe(section);
  });
});


// Add to cart logic number increase logic:

// Use event delegation to handle all add-to-cart forms (even dynamic ones)
document.addEventListener("submit", async (e) => {
  const form = e.target;
  // Check if the submitted form is an add-to-cart form
  if (form.action && form.action.includes("/cart/add/")) {
    e.preventDefault();

    // Get CSRF token from meta tags
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = {
      "X-Requested-With": "XMLHttpRequest",
    };

    if (csrfMeta && csrfHeaderMeta) {
      headers[csrfHeaderMeta.content] = csrfMeta.content;
    }

    try {
      const response = await fetch(form.action, {
        method: "POST",
        body: new FormData(form),
        headers: headers,
      });

      if (response.status === 401) {
        // Unauthorized: user is not logged in. Redirect to the login page.
        sessionStorage.setItem("redirectAfterLogin", window.location.href);
        window.location.href = "/login";
      } else if (response.ok) {
        // OK: user is logged in, update the cart count in the navbar.
        const newCount = await response.text();
        const countElement = document.querySelector("#cart-count");
        if (countElement) countElement.textContent = newCount;
      }
    } catch (error) {
      console.error("Failed to add to cart", error);
    }
  }
});

// dropdown mwnu for the search container:to detect when the search bar is clicked and show the dropdown

const searchInput = document.getElementById('search-input');
if (searchInput) {
  searchInput.addEventListener('click', function() {
    const dropdown = document.getElementById('dropdownMenu');
    if (dropdown.style.display === 'block') {
      dropdown.style.display = 'none';
    } else {
      dropdown.style.display = 'block';
    }
  });
}

// Hide dropdown when clicking outside
document.addEventListener('click', function(event) {
  const searchContainer = document.querySelector('.search-container');
  const dropdown = document.getElementById('dropdownMenu');
  if (searchContainer && !searchContainer.contains(event.target)) {
    dropdown.style.display = 'none';
  }
});

// Contact Page Modal Logic
const submitbtn = document.querySelector("#submit");
const closeBtn = document.getElementById("closeBtn");
const modal = document.getElementById("myModal");

if (submitbtn && modal && closeBtn) {
  // Function to open the modal
  submitbtn.onclick = function (e) {
    e.preventDefault();

    const form = submitbtn.closest("form");
    const contactInput = document.getElementById("contact-number");

    // Check specifically for 10 digits if user entered something
    if (contactInput && contactInput.value.length > 0 && contactInput.value.length !== 10) {
      alert("Please enter a valid 10-digit contact number.");
      return;
    }

    // Check if all required fields are filled and valid
    if (form && !form.checkValidity()) {
      form.reportValidity();
      return;
    }

    modal.style.display = "block";
  };

  // Function to close the modal
  closeBtn.onclick = function () {
    modal.style.display = "none";
  };

  // Close modal if user clicks anywhere outside the content box
  window.onclick = function (event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };
}


// making sure the contact page has onlyh 10 number of digits not less not more 

const contactNumberInput = document.getElementById("contact-number");
if (contactNumberInput) {
  contactNumberInput.addEventListener("input", function (event) {
    let value = event.target.value;
    // Remove any non-digit characters
    value = value.replace(/\D/g, "");
    // Limit to 10 digits
    if (value.length > 10) {
      value = value.slice(0, 10);
    }
    event.target.value = value;
  });
}

// Update cart quantity and price dynamically
document.addEventListener("change", async (e) => {
  if (e.target.matches("input[name='quantity']")) {
    const form = e.target.closest("form");
    // Ensure it's a cart update form
    if (form && form.action.includes("/cart/update/")) {
      e.preventDefault();

      const csrfMeta = document.querySelector('meta[name="_csrf"]');
      const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
      const headers = {
        "X-Requested-With": "XMLHttpRequest",
      };
      if (csrfMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.content] = csrfMeta.content;
      }

      try {
        const response = await fetch(form.action, {
          method: "POST",
          body: new FormData(form),
          headers: headers,
        });

        if (response.ok) {
          const data = await response.json();
          
          // Update Grand Total (looks for element with id 'cart-total')
          const totalEl = document.getElementById("cart-total");
          if (totalEl) totalEl.textContent = data.total.toFixed(1);

          // Update Item Subtotal (looks for element with class 'item-total' in the same row)
          const row = e.target.closest("tr") || e.target.closest(".cart-item");
          if (row) {
            const itemTotalEl = row.querySelector(".item-total");
            if (itemTotalEl) itemTotalEl.textContent = data.itemTotal.toFixed(1);
          }
        }
      } catch (error) {
        console.error("Error updating cart", error);
      }
    }
  }
});
