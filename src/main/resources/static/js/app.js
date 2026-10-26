// Delete confirmation + "Saving..." feedback on forms.
document.addEventListener('submit', function (event) {
    var form = event.target;

    if (form.classList.contains('delete-form')) {
        var name = form.getAttribute('data-name') || 'this item';
        if (!window.confirm('Delete "' + name + '"? This cannot be undone.')) {
            event.preventDefault();
            return;
        }
    }

    if (form.classList.contains('feedback-form')) {
        var button = form.querySelector('button[type="submit"]');
        if (button) {
            button.disabled = true;
            button.textContent = 'Saving...';
        }
    }
});

// Dark / light mode toggle (choice is remembered in this browser)
(function () {
    var root = document.documentElement;
    var icon = document.getElementById('themeIcon');
    var toggle = document.getElementById('themeToggle');

    function refreshIcon() {
        if (icon) {
            icon.className = root.getAttribute('data-bs-theme') === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
        }
    }
    refreshIcon();

    if (toggle) {
        toggle.addEventListener('click', function () {
            var next = root.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
            root.setAttribute('data-bs-theme', next);
            try { localStorage.setItem('theme', next); } catch (e) { /* ignore */ }
            refreshIcon();
        });
    }
})();

// Shopping list: print and copy
(function () {
    var printBtn = document.getElementById('printList');
    if (printBtn) {
        printBtn.addEventListener('click', function () { window.print(); });
    }
    var copyBtn = document.getElementById('copyList');
    if (copyBtn) {
        copyBtn.addEventListener('click', function () {
            var lines = [];
            document.querySelectorAll('.shop-row').forEach(function (row) {
                lines.push('- ' + row.getAttribute('data-text'));
            });
            var text = 'Shopping list\n' + lines.join('\n');
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text).then(function () {
                    copyBtn.textContent = 'Copied!';
                });
            } else {
                window.prompt('Copy your shopping list:', text);
            }
        });
    }
})();
