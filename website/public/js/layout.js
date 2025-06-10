// Layout functionality for DomTrip documentation

// Mobile menu toggle
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('open');
}

// Table of Contents generation
function generateTOC() {
    const tocNav = document.getElementById('toc-nav');
    const headings = document.querySelectorAll('.content-wrapper h2, .content-wrapper h3, .content-wrapper h4');
    
    if (headings.length === 0) {
        document.getElementById('toc').style.display = 'none';
        return;
    }
    
    tocNav.innerHTML = '';
    
    headings.forEach(function(heading, index) {
        const id = heading.id || 'heading-' + index;
        if (!heading.id) {
            heading.id = id;
        }
        
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = '#' + id;
        a.textContent = heading.textContent;
        a.className = 'toc-' + heading.tagName.toLowerCase();
        
        li.appendChild(a);
        tocNav.appendChild(li);
    });
}

// Highlight current page in sidebar
function highlightCurrentPage() {
    const currentPath = window.location.pathname;
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');
    
    sidebarLinks.forEach(function(link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}



// Smooth scrolling for TOC links
function initSmoothScrolling() {
    document.querySelectorAll('.toc-nav a').forEach(function(link) {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}



// Initialize everything
function initPage() {
    generateTOC();
    highlightCurrentPage();
    initSmoothScrolling();
}

// Run initialization
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initPage);
} else {
    initPage();
}
