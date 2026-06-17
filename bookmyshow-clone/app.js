// BookMyShow Clone Application Logic

// --- MOCK DATABASE ---
const movies = [
  {
    id: 1,
    title: "Neon Horizons",
    genre: "Sci-Fi",
    subGenres: ["Action", "Cyberpunk"],
    language: "English",
    languages: ["English", "Hindi", "Telugu"],
    rating: 9.3,
    votes: "182K",
    duration: "2h 25m",
    releaseDate: "9 Oct, 2026",
    poster: "assets/poster_1.png",
    banner: "assets/banner_1.png",
    synopsis: "In a neon-drenched dystopian city where humanity and artificial intelligence blur, a renegade netrunner discovers a secret database that could dismantle the megacorporation controlling their minds.",
    cast: [
      { name: "Elara Vance", role: "Aria (Hacker)", initials: "EV" },
      { name: "Akira Tanaka", role: "Jiro (Enforcer)", initials: "AT" },
      { name: "Jessica Chen", role: "Dr. Kaelen", initials: "JC" }
    ]
  },
  {
    id: 2,
    title: "The Shadow Knight",
    genre: "Action",
    subGenres: ["Drama", "Thriller"],
    language: "English",
    languages: ["English", "Hindi"],
    rating: 9.1,
    votes: "240K",
    duration: "2h 45m",
    releaseDate: "23 Oct, 2026",
    poster: "assets/poster_2.png",
    banner: "assets/banner_1.png",
    synopsis: "When a ruthless crime syndicate threatens to plunge the rain-slicked city of Gothica into chaos, a masked vigilante must confront his past to save the only home he has ever known.",
    cast: [
      { name: "Christian Bale", role: "Shadow Knight", initials: "CB" },
      { name: "Gary Oldman", role: "Commissioner Jim", initials: "GO" },
      { name: "Heath Ledger", role: "The Jester", initials: "HL" }
    ]
  },
  {
    id: 3,
    title: "Cosmic Odyssey",
    genre: "Sci-Fi",
    subGenres: ["Adventure", "Mystery"],
    language: "English",
    languages: ["English", "Hindi", "Spanish"],
    rating: 8.8,
    votes: "95K",
    duration: "2h 10m",
    releaseDate: "14 Nov, 2026",
    poster: "assets/poster_3.png",
    banner: "assets/banner_2.png",
    synopsis: "An epic journey beyond the edges of the known galaxy. A team of pioneering astronauts travels through a newly discovered wormhole in search of a habitable planet for humanity.",
    cast: [
      { name: "Matthew McConaughey", role: "Cooper", initials: "MM" },
      { name: "Anne Hathaway", role: "Brand", initials: "AH" },
      { name: "Jessica Chastain", role: "Murph", initials: "JC" }
    ]
  },
  {
    id: 4,
    title: "Love in Autumn",
    genre: "Romance",
    subGenres: ["Drama", "Melodrama"],
    language: "English",
    languages: ["English", "Spanish"],
    rating: 8.9,
    votes: "50K",
    duration: "1h 55m",
    releaseDate: "30 Nov, 2026",
    poster: "assets/poster_4.png",
    banner: "assets/banner_2.png",
    synopsis: "Two polar opposite souls cross paths in a golden park during a crisp autumn afternoon. As the season turns, they explore the complexities of life, love, and what it means to truly connect.",
    cast: [
      { name: "Olivia Harper", role: "Emma", initials: "OH" },
      { name: "Benjamin Clarke", role: "Arthur", initials: "BC" },
      { name: "Eliza Reed", role: "Director", initials: "ER" }
    ]
  }
];

const theaters = [
  { id: 1, name: "PVR: Vegas, Dwarka", distance: "4.2 km away" },
  { id: 2, name: "Cinepolis: DLF Avenue, Saket", distance: "8.5 km away" },
  { id: 3, name: "Carnival Cinemas: Odeon, Connaught Place", distance: "12.1 km away" }
];

const showtimesTemplate = [
  { time: "10:30 AM", format: "IMAX 2D", status: "available" },
  { time: "01:45 PM", format: "IMAX 3D", status: "filling-fast" },
  { time: "05:00 PM", format: "IMAX 2D", status: "available" },
  { time: "08:15 PM", format: "IMAX 3D", status: "filling-fast" },
  { time: "11:30 PM", format: "2D", status: "available" }
];

const fnbItems = [
  { id: 1, name: "Popcorn & Cola Combo", desc: "Large Salted Popcorn + 1 Large Coke (650ml)", price: 320, emoji: "🍿" },
  { id: 2, name: "Nachos with Cheese Combo", desc: "Crispy Nachos + Cheesy dip + 1 Medium Drink", price: 280, emoji: "🧀" },
  { id: 3, name: "Cold Beverages Large", desc: "Large Pepsi / 7Up / Mirinda (650ml)", price: 180, emoji: "🥤" },
  { id: 4, name: "Veg Burger Combo", desc: "Spicy Veg Burger + French Fries + Coke", price: 350, emoji: "🍔" }
];

// --- APP STATE ---
const state = {
  currentView: "view-home",
  selectedMovie: null,
  selectedDate: null,
  selectedShowtime: null,
  selectedSeats: [],
  fnbCart: {},
  currentUser: null,
  filters: {
    language: "all",
    genre: "all",
    search: ""
  },
  postAuthCallback: null // Function to execute after a successful login
};

// --- DOM ELEMENTS ---
const views = {
  home: document.getElementById("view-home"),
  details: document.getElementById("view-details"),
  showtimes: document.getElementById("view-showtimes"),
  seats: document.getElementById("view-seats"),
  ticket: document.getElementById("view-ticket")
};

const searchInput = document.getElementById("search-input");
const moviesContainer = document.getElementById("movies-container");
const carouselBanners = document.getElementById("carousel-banners");
const movieDetailsContainer = document.getElementById("movie-details-container");
const showtimeMovieMeta = document.getElementById("showtime-movie-meta");
const datePickerContainer = document.getElementById("date-picker-container");
const cinemasContainer = document.getElementById("cinemas-container");
const seatsGridContainer = document.getElementById("seats-grid-container");

const checkoutMeta = document.getElementById("checkout-meta");
const summarySeatsList = document.getElementById("summary-seats-list");
const summaryTicketPrice = document.getElementById("summary-ticket-price");
const summaryFnbRow = document.getElementById("summary-fnb-row");
const summaryFnbPrice = document.getElementById("summary-fnb-price");
const summaryFee = document.getElementById("summary-fee");
const summaryTotalPrice = document.getElementById("summary-total-price");
const btnProceedToFnb = document.getElementById("btn-proceed-to-fnb");

// Modals
const fnbModal = document.getElementById("fnb-modal");
const fnbItemsContainer = document.getElementById("fnb-items-container");
const btnConfirmFnb = document.getElementById("btn-confirm-fnb");
const btnSkipFnb = document.getElementById("btn-skip-fnb");
const btnCloseFnb = document.getElementById("btn-close-fnb");

const signinModal = document.getElementById("signin-modal");
const btnHeaderSignin = document.getElementById("btn-header-signin");
const btnCloseSignin = document.getElementById("btn-close-signin");
const btnSubmitSignin = document.getElementById("btn-submit-signin");
const signinEmailInput = document.getElementById("signin-email");
const userDisplay = document.getElementById("user-display");

// Back buttons
const btnBackToHome = document.getElementById("btn-back-to-home");
const btnBackToDetails = document.getElementById("btn-back-to-details");
const btnBackToShowtimes = document.getElementById("btn-back-to-showtimes");
const btnBackHomeDirect = document.getElementById("btn-back-home-direct");
const logoHome = document.getElementById("logo-home");

// --- VIEW NAVIGATION ---
function switchView(viewId) {
  // Hide all views
  Object.values(views).forEach(view => view.classList.remove("active"));
  // Show active view
  views[viewId.replace("view-", "")].classList.add("active");
  state.currentView = viewId;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

// --- HOME VIEW CAROUSEL ---
function initCarousel() {
  carouselBanners.innerHTML = "";
  
  // Use movies for banners (e.g. Movie 1 and Movie 3 have generated banner files)
  const carouselMovies = movies.filter(m => m.id === 1 || m.id === 3);
  
  carouselMovies.forEach((movie, index) => {
    const slide = document.createElement("div");
    slide.className = `carousel-slide ${index === 0 ? "active" : ""}`;
    slide.style.backgroundImage = `url(${movie.banner})`;
    
    slide.innerHTML = `
      <div class="carousel-overlay">
        <div class="carousel-content">
          <span class="carousel-tag">${movie.genre}</span>
          <h1 class="carousel-title">${movie.title}</h1>
          <p class="carousel-desc">${movie.synopsis}</p>
          <button class="carousel-btn" data-id="${movie.id}">
            Book Tickets ➜
          </button>
        </div>
      </div>
    `;
    
    carouselBanners.appendChild(slide);
  });
  
  // Add Dots
  const dotsContainer = document.createElement("div");
  dotsContainer.className = "carousel-dots";
  carouselMovies.forEach((_, index) => {
    const dot = document.createElement("div");
    dot.className = `dot ${index === 0 ? "active" : ""}`;
    dot.addEventListener("click", () => showSlide(index));
    dotsContainer.appendChild(dot);
  });
  carouselBanners.appendChild(dotsContainer);
  
  let currentSlide = 0;
  let slideInterval = setInterval(nextSlide, 4500);
  
  function showSlide(index) {
    const slides = carouselBanners.querySelectorAll(".carousel-slide");
    const dots = carouselBanners.querySelectorAll(".dot");
    
    slides[currentSlide].classList.remove("active");
    dots[currentSlide].classList.remove("active");
    
    currentSlide = index;
    
    slides[currentSlide].classList.add("active");
    dots[currentSlide].classList.add("active");
    
    clearInterval(slideInterval);
    slideInterval = setInterval(nextSlide, 4500);
  }
  
  function nextSlide() {
    const newSlide = (currentSlide + 1) % carouselMovies.length;
    showSlide(newSlide);
  }
  
  // Event delegation for Carousel CTA buttons
  carouselBanners.addEventListener("click", (e) => {
    const btn = e.target.closest(".carousel-btn");
    if (btn) {
      const movieId = parseInt(btn.getAttribute("data-id"));
      const movie = movies.find(m => m.id === movieId);
      if (movie) {
        showMovieDetails(movie);
      }
    }
  });
}

// --- MOVIES RENDER & FILTERING ---
function renderMovies() {
  moviesContainer.innerHTML = "";
  
  const filtered = movies.filter(movie => {
    const matchLanguage = state.filters.language === "all" || movie.languages.includes(state.filters.language);
    const matchGenre = state.filters.genre === "all" || movie.genre === state.filters.genre;
    const matchSearch = movie.title.toLowerCase().includes(state.filters.search.toLowerCase()) || 
                        movie.genre.toLowerCase().includes(state.filters.search.toLowerCase());
    return matchLanguage && matchGenre && matchSearch;
  });
  
  if (filtered.length === 0) {
    moviesContainer.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-secondary);">
        <p style="font-size: 1.2rem; margin-bottom: 10px;">No movies found matching your filters.</p>
        <button class="btn-secondary" style="width: auto;" id="btn-reset-filters">Reset Filters</button>
      </div>
    `;
    
    document.getElementById("btn-reset-filters")?.addEventListener("click", () => {
      resetFilters();
    });
    return;
  }
  
  filtered.forEach(movie => {
    const card = document.createElement("div");
    card.className = "movie-card";
    card.innerHTML = `
      <div class="poster-wrapper">
        <img src="${movie.poster}" alt="${movie.title}">
        <div class="rating-badge">
          <span class="rating-stars">★ ${movie.rating}/10</span>
          <span class="rating-votes">${movie.votes} votes</span>
        </div>
      </div>
      <div class="movie-info">
        <div>
          <h3 class="movie-title" title="${movie.title}">${movie.title}</h3>
          <p class="movie-genre">${movie.genre} / ${movie.subGenres.join(", ")}</p>
        </div>
        <p class="movie-lang">${movie.languages.join(", ")}</p>
      </div>
    `;
    
    card.addEventListener("click", () => {
      showMovieDetails(movie);
    });
    
    moviesContainer.appendChild(card);
  });
}

function resetFilters() {
  state.filters = { language: "all", genre: "all", search: "" };
  searchInput.value = "";
  
  document.querySelectorAll("#filter-languages .filter-btn").forEach(btn => {
    btn.classList.toggle("selected", btn.getAttribute("data-value") === "all");
  });
  
  document.querySelectorAll("#filter-genres .filter-btn").forEach(btn => {
    btn.classList.toggle("selected", btn.getAttribute("data-value") === "all");
  });
  
  renderMovies();
}

function setupFilters() {
  // Languages filter click
  document.querySelectorAll("#filter-languages .filter-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      document.querySelectorAll("#filter-languages .filter-btn").forEach(b => b.classList.remove("selected"));
      btn.classList.add("selected");
      state.filters.language = btn.getAttribute("data-value");
      renderMovies();
    });
  });
  
  // Genres filter click
  document.querySelectorAll("#filter-genres .filter-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      document.querySelectorAll("#filter-genres .filter-btn").forEach(b => b.classList.remove("selected"));
      btn.classList.add("selected");
      state.filters.genre = btn.getAttribute("data-value");
      renderMovies();
    });
  });
  
  // Search bar input
  searchInput.addEventListener("input", (e) => {
    state.filters.search = e.target.value;
    renderMovies();
  });
}

// --- MOVIE DETAILS VIEW ---
function showMovieDetails(movie) {
  state.selectedMovie = movie;
  
  const subgenresHtml = movie.subGenres.map(g => `<span class="details-tag">${g}</span>`).join("");
  const castHtml = movie.cast.map(c => `
    <div class="cast-member">
      <div class="cast-avatar">${c.initials}</div>
      <p class="cast-name">${c.name}</p>
      <p class="cast-role">${c.role}</p>
    </div>
  `).join("");
  
  movieDetailsContainer.innerHTML = `
    <div class="movie-details-hero" style="background-image: url(${movie.banner});">
      <div class="movie-details-overlay">
        <div class="details-poster">
          <img src="${movie.poster}" alt="${movie.title}">
        </div>
        <div class="details-info">
          <div class="details-tags">
            <span class="details-tag" style="background-color: var(--accent-color); color: white;">Trending</span>
            ${subgenresHtml}
          </div>
          <h1 style="font-size: 2.8rem; font-weight: 800; line-height: 1.1; margin-bottom: 10px;">${movie.title}</h1>
          
          <div class="details-rating-box">
            <div class="detail-rating-item">
              <span class="rating-value"><span>★</span> ${movie.rating}/10</span>
              <span class="rating-label">${movie.votes} ratings</span>
            </div>
            <div class="detail-rating-item" style="border-left: 1px solid var(--border-color); padding-left: 30px;">
              <span class="rating-value" style="font-size: 1.1rem; color: #4caf50;">✓ Booking</span>
              <span class="rating-label">In Cinemas now</span>
            </div>
          </div>
          
          <div class="details-meta">
            <span>${movie.duration}</span> • 
            <span>${movie.languages.join(", ")}</span> • 
            <span>UA</span> • 
            <span>Released ${movie.releaseDate}</span>
          </div>
          
          <button class="btn-book" id="btn-book-tickets-trigger">Book Tickets</button>
        </div>
      </div>
    </div>
    
    <div class="details-body">
      <div>
        <div class="details-card">
          <h3>About the Movie</h3>
          <p class="synopsis-text">${movie.synopsis}</p>
        </div>
        
        <div class="details-card">
          <h3>Top Cast</h3>
          <div class="cast-grid">
            ${castHtml}
          </div>
        </div>
      </div>
      
      <div>
        <div class="details-card" style="padding: 20px;">
          <h4 style="margin-bottom: 12px; font-weight: 700;">Offers Applicable</h4>
          <ul style="color: var(--text-secondary); font-size: 0.85rem; padding-left: 20px; display: flex; flex-direction: column; gap: 8px;">
            <li>Get 50% discount up to Rs. 150 on your first booking using code <strong>BMSFIRST</strong>.</li>
            <li>Buy 1 Get 1 Free on Axis Bank Select Credit Cards.</li>
            <li>Get flat Rs. 100 CashBack on paying via Amazon Pay.</li>
          </ul>
        </div>
      </div>
    </div>
  `;
  
  document.getElementById("btn-book-tickets-trigger").addEventListener("click", () => {
    showShowtimes(movie);
  });
  
  switchView("view-details");
}

// --- SHOWTIME SELECTION VIEW ---
function showShowtimes(movie) {
  showtimeMovieMeta.innerHTML = `
    <h2 style="font-size: 1.8rem; font-weight: 800;">${movie.title} - Showtimes</h2>
    <p style="color: var(--text-secondary); margin-top: 5px;">${movie.languages.join(", ")} | ${movie.duration} | ${movie.genre}</p>
  `;
  
  // Date Picker Setup (Next 5 Days)
  datePickerContainer.innerHTML = "";
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  
  const today = new Date();
  for (let i = 0; i < 5; i++) {
    const d = new Date();
    d.setDate(today.getDate() + i);
    
    const dayName = i === 0 ? "Today" : days[d.getDay()];
    const dateNum = d.getDate();
    
    const dateItem = document.createElement("div");
    dateItem.className = `date-item ${i === 0 ? "selected" : ""}`;
    dateItem.setAttribute("data-date", d.toISOString().split("T")[0]);
    
    dateItem.innerHTML = `
      <div class="date-day">${dayName}</div>
      <div class="date-num">${dateNum}</div>
    `;
    
    dateItem.addEventListener("click", () => {
      datePickerContainer.querySelectorAll(".date-item").forEach(item => item.classList.remove("selected"));
      dateItem.classList.add("selected");
      state.selectedDate = dateItem.getAttribute("data-date");
      renderCinemasAndTimes(movie);
    });
    
    datePickerContainer.appendChild(dateItem);
  }
  
  state.selectedDate = today.toISOString().split("T")[0];
  renderCinemasAndTimes(movie);
  switchView("view-showtimes");
}

function renderCinemasAndTimes(movie) {
  cinemasContainer.innerHTML = "";
  
  theaters.forEach(theater => {
    const card = document.createElement("div");
    card.className = "cinema-card";
    
    // Create random filling statuses for showtimes to look real
    let showtimesHtml = showtimesTemplate.map(st => {
      // Deterministic randomness based on cinema name + showtime
      const randomSeed = (theater.name.charCodeAt(0) + st.time.charCodeAt(0)) % 10;
      const statusClass = randomSeed < 4 ? "filling-fast" : "available";
      return `
        <div class="showtime-pill ${statusClass}" data-theater-id="${theater.id}" data-time="${st.time}" data-format="${st.format}">
          <div class="time">${st.time}</div>
          <div class="format">${st.format}</div>
        </div>
      `;
    }).join("");
    
    card.innerHTML = `
      <div class="cinema-info-row">
        <div>
          <span class="cinema-name">${theater.name}</span>
          <p class="cinema-distance">${theater.distance} • Safety Shield Certified</p>
        </div>
        <div style="font-size: 0.8rem; color: #4caf50;">✓ M-Ticket Available</div>
      </div>
      <div class="showtimes-grid">
        ${showtimesHtml}
      </div>
    `;
    
    cinemasContainer.appendChild(card);
  });
  
  // Showtime clicks
  cinemasContainer.querySelectorAll(".showtime-pill").forEach(pill => {
    pill.addEventListener("click", () => {
      const theaterId = parseInt(pill.getAttribute("data-theater-id"));
      const theater = theaters.find(t => t.id === theaterId);
      const time = pill.getAttribute("data-time");
      const format = pill.getAttribute("data-format");
      
      state.selectedShowtime = {
        theater: theater,
        time: time,
        format: format
      };
      
      startSeatBooking();
    });
  });
}

// --- SEAT BOOKING SELECTION ---
function startSeatBooking() {
  state.selectedSeats = [];
  checkoutMeta.innerHTML = `
    <h4 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 4px;">${state.selectedMovie.title}</h4>
    <p style="font-size: 0.8rem; color: var(--text-secondary);">${state.selectedShowtime.theater.name}</p>
    <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">
      Date: ${formatReadableDate(state.selectedDate)} | Time: ${state.selectedShowtime.time} (${state.selectedShowtime.format})
    </p>
  `;
  
  generateSeatGrid();
  updateSeatCheckoutSummary();
  switchView("view-seats");
}

function formatReadableDate(dateStr) {
  const d = new Date(dateStr);
  const options = { weekday: "short", day: "numeric", month: "short" };
  return d.toLocaleDateString("en-US", options);
}

function generateSeatGrid() {
  seatsGridContainer.innerHTML = "";
  
  // Rows: A-J (A-C: Classic Rs 150, D-G: Prime Rs 250, H-J: Recliner Rs 450)
  const rows = [
    { label: "J", type: "Recliner", price: 450 },
    { label: "I", type: "Recliner", price: 450 },
    { label: "H", type: "Prime", price: 250 },
    { label: "G", type: "Prime", price: 250 },
    { label: "F", type: "Prime", price: 250 },
    { label: "E", type: "Prime", price: 250 },
    { label: "D", type: "Classic", price: 150 },
    { label: "C", type: "Classic", price: 150 },
    { label: "B", type: "Classic", price: 150 },
    { label: "A", type: "Classic", price: 150 }
  ];
  
  rows.forEach(row => {
    const rowDiv = document.createElement("div");
    rowDiv.className = "seat-row";
    
    // Header for seat category
    // Let's add category borders/labels dynamically on the left
    const labelSpan = document.createElement("span");
    labelSpan.className = "row-label";
    labelSpan.innerText = row.label;
    rowDiv.appendChild(labelSpan);
    
    // Column loop (1 to 14)
    for (let col = 1; col <= 14; col++) {
      // Add aisle gap after column 3 and 11
      if (col === 4 || col === 12) {
        const spacer = document.createElement("div");
        spacer.className = "seat-spacer";
        rowDiv.appendChild(spacer);
      }
      
      const seatId = `${row.label}-${col}`;
      const seat = document.createElement("div");
      seat.className = "seat-box";
      seat.setAttribute("data-seat-id", seatId);
      seat.setAttribute("data-price", row.price);
      seat.setAttribute("data-category", row.type);
      seat.innerText = col;
      
      // Deterministic randomness for seat reservation based on theater + time + seatId
      const seatHash = (state.selectedShowtime.theater.id * 13 + seatId.charCodeAt(0) * 7 + col) % 5;
      const isSold = seatHash === 0 || seatHash === 2; // ~40% sold
      
      if (isSold) {
        seat.classList.add("sold");
      } else {
        seat.addEventListener("click", () => {
          toggleSeatSelection(seat, seatId, row.price);
        });
      }
      
      rowDiv.appendChild(seat);
    }
    
    // Add row pricing badge at the end of some categories
    seatsGridContainer.appendChild(rowDiv);
  });
}

function toggleSeatSelection(seatElement, seatId, price) {
  if (seatElement.classList.contains("selected")) {
    seatElement.classList.remove("selected");
    state.selectedSeats = state.selectedSeats.filter(s => s.id !== seatId);
  } else {
    // Limit to max 10 seats
    if (state.selectedSeats.length >= 10) {
      alert("You can select a maximum of 10 seats per booking.");
      return;
    }
    seatElement.classList.add("selected");
    state.selectedSeats.push({ id: seatId, price: price });
  }
  
  updateSeatCheckoutSummary();
}

function updateSeatCheckoutSummary() {
  if (state.selectedSeats.length === 0) {
    summarySeatsList.innerText = "None";
    summaryTicketPrice.innerText = "Rs. 0";
    summaryFee.innerText = "Rs. 0";
    summaryTotalPrice.innerText = "Rs. 0";
    btnProceedToFnb.innerText = "Book Seats";
    btnProceedToFnb.disabled = true;
    
    summaryFnbRow.style.display = "none";
    return;
  }
  
  const seatIds = state.selectedSeats.map(s => s.id);
  summarySeatsList.innerText = seatIds.join(", ");
  
  // Calculations
  const ticketSubtotal = state.selectedSeats.reduce((sum, s) => sum + s.price, 0);
  summaryTicketPrice.innerText = `Rs. ${ticketSubtotal}`;
  
  // F&B calculations
  let fnbSubtotal = 0;
  Object.keys(state.fnbCart).forEach(itemId => {
    const qty = state.fnbCart[itemId];
    const item = fnbItems.find(f => f.id === parseInt(itemId));
    if (item && qty > 0) {
      fnbSubtotal += item.price * qty;
    }
  });
  
  if (fnbSubtotal > 0) {
    summaryFnbRow.style.display = "flex";
    summaryFnbPrice.innerText = `Rs. ${fnbSubtotal}`;
  } else {
    summaryFnbRow.style.display = "none";
  }
  
  // Convenience Fee (15% of ticket subtotal)
  const fee = Math.round(ticketSubtotal * 0.15);
  summaryFee.innerText = `Rs. ${fee}`;
  
  const total = ticketSubtotal + fnbSubtotal + fee;
  summaryTotalPrice.innerText = `Rs. ${total}`;
  
  btnProceedToFnb.innerText = `Pay Rs. ${total}`;
  btnProceedToFnb.disabled = false;
}

// --- FOOD AND BEVERAGES ADD-ONS PANEL ---
function openFnbPanel() {
  fnbItemsContainer.innerHTML = "";
  
  fnbItems.forEach(item => {
    const qty = state.fnbCart[item.id] || 0;
    
    const div = document.createElement("div");
    div.className = "fnb-item";
    div.innerHTML = `
      <div class="fnb-item-details">
        <div class="fnb-item-emoji">${item.emoji}</div>
        <div class="fnb-item-text">
          <h4>${item.name}</h4>
          <p>${item.desc}</p>
          <div class="fnb-item-price">Rs. ${item.price}</div>
        </div>
      </div>
      
      <div class="quantity-controls">
        <button class="btn-qty btn-minus" data-id="${item.id}">-</button>
        <span class="qty-val" id="fnb-qty-${item.id}">${qty}</span>
        <button class="btn-qty btn-plus" data-id="${item.id}">+</button>
      </div>
    `;
    
    // Add event listener for plus/minus
    div.querySelector(".btn-minus").addEventListener("click", () => adjustFnbQty(item.id, -1));
    div.querySelector(".btn-plus").addEventListener("click", () => adjustFnbQty(item.id, 1));
    
    fnbItemsContainer.appendChild(div);
  });
  
  fnbModal.classList.add("active");
}

function adjustFnbQty(itemId, adjustment) {
  const currentQty = state.fnbCart[itemId] || 0;
  const newQty = Math.max(0, currentQty + adjustment);
  
  state.fnbCart[itemId] = newQty;
  document.getElementById(`fnb-qty-${itemId}`).innerText = newQty;
}

function closeFnbPanel() {
  fnbModal.classList.remove("active");
}

// --- TICKET GENERATOR AND RECEIPT ---
function processPaymentAndCheckout() {
  // Check if signed in. If not, open Sign In Modal
  if (!state.currentUser) {
    state.postAuthCallback = () => {
      processPaymentAndCheckout();
    };
    openSigninModal();
    return;
  }
  
  // Simulate network delay for payment
  btnProceedToFnb.disabled = true;
  btnProceedToFnb.innerText = "Processing Payment...";
  
  setTimeout(() => {
    generateFinalTicket();
    btnProceedToFnb.disabled = false;
    switchView("view-ticket");
  }, 1200);
}

function generateFinalTicket() {
  const ticketPrintContainer = document.getElementById("movie-ticket-print");
  const randomBookingNum = Math.floor(100000 + Math.random() * 900000);
  const bookingId = `BMS-TX-${randomBookingNum}`;
  
  const seatNumbers = state.selectedSeats.map(s => s.id).join(", ");
  
  // Calculate final summary details
  const ticketSubtotal = state.selectedSeats.reduce((sum, s) => sum + s.price, 0);
  let fnbSubtotal = 0;
  let fnbSummaries = [];
  
  Object.keys(state.fnbCart).forEach(itemId => {
    const qty = state.fnbCart[itemId];
    const item = fnbItems.find(f => f.id === parseInt(itemId));
    if (item && qty > 0) {
      fnbSubtotal += item.price * qty;
      fnbSummaries.push(`${item.name} (${qty})`);
    }
  });
  
  const fee = Math.round(ticketSubtotal * 0.15);
  const total = ticketSubtotal + fnbSubtotal + fee;
  
  const fnbSectionHtml = fnbSummaries.length > 0 
    ? `<div class="ticket-info-item" style="grid-column: 1/-1;">
         <h5>Snacks & Drinks Added</h5>
         <p style="font-size: 0.85rem; color: var(--text-secondary);">${fnbSummaries.join(", ")}</p>
       </div>`
    : "";
    
  ticketPrintContainer.innerHTML = `
    <div class="ticket-top">
      <div class="ticket-movie-title">${state.selectedMovie.title}</div>
      <div class="ticket-movie-meta">${state.selectedMovie.genre} | ${state.selectedMovie.duration}</div>
      
      <div class="ticket-details-grid">
        <div class="ticket-info-item">
          <h5>Cinema / Theater</h5>
          <p>${state.selectedShowtime.theater.name}</p>
        </div>
        <div class="ticket-info-item">
          <h5>Format</h5>
          <p>${state.selectedShowtime.format}</p>
        </div>
        <div class="ticket-info-item">
          <h5>Date & Time</h5>
          <p>${formatReadableDate(state.selectedDate)} • ${state.selectedShowtime.time}</p>
        </div>
        <div class="ticket-info-item">
          <h5>Seats Selected</h5>
          <p style="color: var(--accent-color);">${seatNumbers}</p>
        </div>
        ${fnbSectionHtml}
        <div class="ticket-info-item" style="grid-column: 1/-1;">
          <h5>Amount Paid</h5>
          <p style="color: var(--success-color);">Rs. ${total} <span style="font-size: 0.75rem; font-weight: normal; color: var(--text-secondary);">(Taxes included)</span></p>
        </div>
      </div>
    </div>
    
    <div class="ticket-divider"></div>
    
    <div class="ticket-bottom">
      <div class="qr-code-box">
        <!-- SVG rendering of a mock QR code for premium visuals -->
        <svg width="120" height="120" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="display: block;">
          <rect width="100" height="100" fill="white"/>
          <!-- Corner Position Anchors -->
          <rect x="5" y="5" width="20" height="20" fill="black"/>
          <rect x="8" y="8" width="14" height="14" fill="white"/>
          <rect x="11" y="11" width="8" height="8" fill="black"/>
          
          <rect x="75" y="5" width="20" height="20" fill="black"/>
          <rect x="78" y="8" width="14" height="14" fill="white"/>
          <rect x="81" y="81" width="8" height="8" fill="black"/>
          
          <rect x="5" y="75" width="20" height="20" fill="black"/>
          <rect x="8" y="78" width="14" height="14" fill="white"/>
          <rect x="11" y="81" width="8" height="8" fill="black"/>
          
          <rect x="75" y="75" width="20" height="20" fill="black"/>
          <rect x="78" y="78" width="14" height="14" fill="white"/>
          <rect x="81" y="11" width="8" height="8" fill="black"/>
          
          <!-- Mock QR Data Blocks -->
          <rect x="35" y="10" width="10" height="10" fill="black"/>
          <rect x="55" y="5" width="5" height="15" fill="black"/>
          <rect x="40" y="25" width="25" height="5" fill="black"/>
          <rect x="10" y="35" width="15" height="10" fill="black"/>
          <rect x="30" y="40" width="40" height="15" fill="black"/>
          <rect x="15" y="55" width="10" height="5" fill="black"/>
          <rect x="5" y="65" width="15" height="5" fill="black"/>
          <rect x="80" y="35" width="10" height="15" fill="black"/>
          <rect x="35" y="60" width="10" height="20" fill="black"/>
          <rect x="50" y="65" width="15" height="10" fill="black"/>
          <rect x="70" y="55" width="25" height="10" fill="black"/>
          <rect x="55" y="80" width="15" height="15" fill="black"/>
        </svg>
      </div>
      <div class="booking-id">BOOKING ID: ${bookingId}</div>
      <p style="font-size: 0.7rem; color: var(--text-muted); text-align: center;">Show this QR code at cinema entrance. Screen entry permitted only via mobile/printed ticket.</p>
    </div>
  `;
}

// --- AUTHENTICATION FLOW ---
function openSigninModal() {
  signinModal.classList.add("active");
}

function closeSigninModal() {
  signinModal.classList.remove("active");
}

function handleSignin(e) {
  e.preventDefault();
  const email = signinEmailInput.value || "guest@example.com";
  state.currentUser = email;
  
  // Update header UI
  btnHeaderSignin.style.display = "none";
  userDisplay.style.display = "block";
  userDisplay.innerText = email.split("@")[0].toUpperCase();
  
  closeSigninModal();
  
  // If there was a pending callback (e.g. from checkout payment block)
  if (state.postAuthCallback) {
    const callback = state.postAuthCallback;
    state.postAuthCallback = null;
    callback();
  }
}

// --- SETUP GENERAL INTERACTIVE HANDLERS ---
function init() {
  // Populate elements
  initCarousel();
  renderMovies();
  setupFilters();
  
  // Logo header navigation click (goes back home)
  logoHome.addEventListener("click", () => {
    resetFilters();
    switchView("view-home");
  });
  
  // Back buttons
  btnBackToHome.addEventListener("click", () => {
    switchView("view-home");
  });
  
  btnBackToDetails.addEventListener("click", () => {
    switchView("view-details");
  });
  
  btnBackToShowtimes.addEventListener("click", () => {
    switchView("view-showtimes");
  });
  
  btnBackHomeDirect.addEventListener("click", () => {
    resetFilters();
    switchView("view-home");
  });
  
  // Proceed seat selection button (shows F&B modal first)
  btnProceedToFnb.addEventListener("click", () => {
    openFnbPanel();
  });
  
  // F&B Modal button clicks
  btnConfirmFnb.addEventListener("click", () => {
    closeFnbPanel();
    processPaymentAndCheckout();
  });
  
  btnSkipFnb.addEventListener("click", () => {
    state.fnbCart = {}; // Reset snack selections
    updateSeatCheckoutSummary();
    closeFnbPanel();
    processPaymentAndCheckout();
  });
  
  btnCloseFnb.addEventListener("click", () => {
    closeFnbPanel();
  });
  
  // Auth button clicks
  btnHeaderSignin.addEventListener("click", () => {
    state.postAuthCallback = null; // Standard manual login
    openSigninModal();
  });
  
  btnCloseSignin.addEventListener("click", () => {
    closeSigninModal();
  });
  
  document.getElementById("btn-submit-signin").addEventListener("click", handleSignin);
  
  // Location selector change effect
  document.getElementById("btn-location").addEventListener("click", () => {
    const cities = ["Delhi-NCR", "Mumbai", "Bengaluru", "Hyderabad", "Ahmedabad", "Pune", "Kolkata"];
    const current = document.getElementById("btn-location").innerText.replace(" ▾", "");
    const currentIndex = cities.indexOf(current);
    const nextIndex = (currentIndex + 1) % cities.length;
    document.getElementById("btn-location").innerText = `${cities[nextIndex]} ▾`;
    alert(`City changed to ${cities[nextIndex]}. Showtimes will be refreshed.`);
    if (state.selectedMovie && state.currentView === "view-showtimes") {
      renderCinemasAndTimes(state.selectedMovie);
    }
  });
}

// Run Initialization
window.addEventListener("DOMContentLoaded", init);
