document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initChatbot();
    initPWA();
    initScrollEffects();
});

function initNavigation() {
    const toggle = document.querySelector('.nav-toggle');
    const nav = document.querySelector('.mobile-nav');
    
    if (toggle && nav) {
        toggle.addEventListener('click', function() {
            toggle.classList.toggle('active');
            nav.classList.toggle('active');
        });
        
        document.querySelectorAll('.mobile-nav a').forEach(link => {
            link.addEventListener('click', function() {
                toggle.classList.remove('active');
                nav.classList.remove('active');
            });
        });
    }
}

function initChatbot() {
    const toggleBtn = document.querySelector('.chatbot-toggle');
    const panel = document.querySelector('.chatbot-panel');
    const input = document.querySelector('.chatbot-input input');
    const sendBtn = document.querySelector('.chatbot-input button');
    const messages = document.querySelector('.chatbot-messages');
    
    if (!toggleBtn || !panel) return;
    
    toggleBtn.addEventListener('click', function() {
        panel.classList.toggle('active');
        if (panel.classList.contains('active') && messages.children.length === 0) {
            addBotMessage('안녕~! 네코루네일 AI야! 💅✨\n\n가격, 예약, 네일 추천 뭐든 물어봐~!');
        }
    });
    
    if (sendBtn && input) {
        sendBtn.addEventListener('click', sendMessage);
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') sendMessage();
        });
    }
    
    function sendMessage() {
        const text = input.value.trim();
        if (!text) return;
        
        addUserMessage(text);
        input.value = '';
        
        const loadingEl = document.createElement('div');
        loadingEl.className = 'chat-message bot loading-message';
        loadingEl.innerHTML = '<div class="loading"><span class="loading-dot"></span><span class="loading-dot"></span><span class="loading-dot"></span></div>';
        messages.appendChild(loadingEl);
        messages.scrollTop = messages.scrollHeight;
        
        fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        })
        .then(res => res.json())
        .then(data => {
            loadingEl.remove();
            addBotMessage(data.response);
        })
        .catch(() => {
            loadingEl.remove();
            addBotMessage('앗 잠깐 문제가 생겼어~! 다시 시도해줘! 💦');
        });
    }
    
    function addUserMessage(text) {
        const el = document.createElement('div');
        el.className = 'chat-message user';
        el.textContent = text;
        messages.appendChild(el);
        messages.scrollTop = messages.scrollHeight;
    }
    
    function addBotMessage(text) {
        const el = document.createElement('div');
        el.className = 'chat-message bot';
        el.innerHTML = text.replace(/\n/g, '<br>');
        messages.appendChild(el);
        messages.scrollTop = messages.scrollHeight;
    }
}

function initPWA() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js')
            .then(reg => console.log('SW registered'))
            .catch(err => console.log('SW registration failed'));
    }
    
    let deferredPrompt;
    const installPrompt = document.querySelector('.install-prompt');
    
    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredPrompt = e;
        
        if (installPrompt && !localStorage.getItem('pwaInstallDismissed')) {
            installPrompt.classList.add('show');
        }
    });
    
    const installBtn = document.querySelector('.install-btn');
    const closeBtn = document.querySelector('.install-close');
    
    if (installBtn) {
        installBtn.addEventListener('click', async () => {
            if (deferredPrompt) {
                deferredPrompt.prompt();
                const { outcome } = await deferredPrompt.userChoice;
                deferredPrompt = null;
                if (installPrompt) installPrompt.classList.remove('show');
            }
        });
    }
    
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            if (installPrompt) installPrompt.classList.remove('show');
            localStorage.setItem('pwaInstallDismissed', 'true');
        });
    }
}

function initScrollEffects() {
    const header = document.querySelector('header');
    
    window.addEventListener('scroll', function() {
        if (window.scrollY > 100) {
            header.style.background = 'rgba(9,8,13,0.98)';
        } else {
            header.style.background = 'linear-gradient(180deg, rgba(9,8,13,1) 0%, rgba(9,8,13,0.95) 100%)';
        }
    });
}

function filterGallery(tag) {
    const items = document.querySelectorAll('.gallery-item');
    const tags = document.querySelectorAll('.color-tag');
    
    tags.forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    
    if (tag === 'all') {
        items.forEach(item => item.style.display = 'block');
        return;
    }
    
    items.forEach(item => {
        const itemTags = item.dataset.tags || '';
        if (itemTags.includes(tag)) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });
}
