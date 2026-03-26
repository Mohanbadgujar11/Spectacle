

const productGrid = document.getElementById("product-grid");
const cardTemplate = document.getElementById("card-template");

function renderProducts() {
  // Clear container just in case
  productGrid.innerHTML = "";

  // Get the category from the HTML element
  const pageCategory = productGrid.getAttribute("data-category");

  // Loop through data and append HTML
  // Filter based on the page category
  products
    .filter((product) => product.category === pageCategory)
    .forEach((product) => {
      // 1. Clone the template
      const clone = cardTemplate.content.cloneNode(true);

      // 2. Fill in the data
      const link = clone.querySelector("a");
      link.href = `/product-details?id=${product.id}`;

      const img = clone.querySelector(".product-img");
      img.src = product.images[0];
      // img.alt = product.name;

      clone.querySelector(".card-title").textContent = product.name;
      clone.querySelector(".product-description").textContent =
        product.description.length > 50
          ? product.description.substring(0, 50) + "..."
          : product.description;
      // prefer discountedPrice then sellingPrice, fallback to price (legacy static data)
      clone.querySelector(".product-price").textContent =
        product.discountedPrice || product.sellingPrice || product.price || "";

      // 3. Add to the page
      productGrid.appendChild(clone);
    });
}

// Run the render function
if (productGrid && cardTemplate) {
  renderProducts();
}
